package sv.edu.catolica.taskflow;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout; // <-- Importado

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import sv.edu.catolica.taskflow.api.ApiClient;
import sv.edu.catolica.taskflow.models.ProyectoSimple;
import sv.edu.catolica.taskflow.models.Tarea;
import sv.edu.catolica.taskflow.models.User;

public class CrearTareaActivity extends AppCompatActivity {

    private ApiClient apiClient;
    private Toolbar toolbar;

    // Campos del formulario
    private TextInputEditText etTitulo, etDescripcion, etFecha;
    private AutoCompleteTextView acProyecto, acAsignado;
    private TextInputLayout layoutAsignado; // <-- Layout para deshabilitar
    private Button btnGuardar;

    // Listas para guardar los datos
    private List<ProyectoSimple> listaProyectos;
    private List<User> listaUsuarios;

    // Variables para guardar la selección
    private ProyectoSimple proyectoSeleccionado;
    private User usuarioSeleccionado;
    private String fechaSeleccionada = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_tarea);

        initViews();
        initApiClient();
        setupToolbar();
        setupListeners();

        loadProyectosList(); // Carga solo la lista de proyectos al inicio
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFecha = findViewById(R.id.etFecha);
        acProyecto = findViewById(R.id.acProyecto);
        acAsignado = findViewById(R.id.acAsignado);
        layoutAsignado = findViewById(R.id.layoutAsignado); // <-- Vinculado
        btnGuardar = findViewById(R.id.btnGuardar);
    }

    private void initApiClient() {
        apiClient = new ApiClient(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Crear Nueva Tarea");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupListeners() {
        btnGuardar.setOnClickListener(v -> guardarTarea());
        etFecha.setOnClickListener(v -> setupDatePicker());
    }

    private void setupDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    fechaSeleccionada = String.format(Locale.US, "%d-%02d-%02d", year1, (monthOfYear + 1), dayOfMonth);
                    String fechaUsuario = String.format(Locale.US, "%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year1);
                    etFecha.setText(fechaUsuario);
                }, year, month, day);
        datePickerDialog.show();
    }

    /**
     * Carga solo la lista de Proyectos
     */
    private void loadProyectosList() {
        apiClient.getProyectos(new ApiClient.ApiCallback<List<ProyectoSimple>>() {
            @Override
            public void onSuccess(List<ProyectoSimple> result) {
                listaProyectos = result;
                new Handler(Looper.getMainLooper()).post(() -> {
                    ArrayAdapter<ProyectoSimple> adapter = new ArrayAdapter<>(CrearTareaActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, listaProyectos);
                    acProyecto.setAdapter(adapter);

                    // --- LÓGICA EN CADENA ---
                    acProyecto.setOnItemClickListener((parent, view, position, id) -> {
                        proyectoSeleccionado = (ProyectoSimple) parent.getItemAtPosition(position);
                        // Cargar la lista de usuarios DESPUÉS de seleccionar el proyecto
                        cargarUsuariosDelProyecto(proyectoSeleccionado.getId());
                    });
                });
            }
            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(CrearTareaActivity.this, "Error al cargar proyectos: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /**
     * Carga los usuarios (miembros) de un proyecto específico
     */
    private void cargarUsuariosDelProyecto(int proyectoId) {
        // 1. Limpiar y deshabilitar el spinner de asignado
        usuarioSeleccionado = null;
        acAsignado.setText("", false);
        layoutAsignado.setHint("Cargando usuarios...");
        layoutAsignado.setEnabled(false);

        apiClient.getUsuariosDelProyecto(proyectoId, new ApiClient.ApiCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                // --- FILTRAR ADMINS (de la regla anterior) ---
                List<User> usuariosFiltrados = new ArrayList<>();
                if (result != null) {
                    for (User usuario : result) {
                        if (!usuario.isAdmin()) {
                            usuariosFiltrados.add(usuario);
                        }
                    }
                }
                listaUsuarios = usuariosFiltrados;

                // 2. Poblar y habilitar el spinner
                new Handler(Looper.getMainLooper()).post(() -> {
                    ArrayAdapter<User> adapter = new ArrayAdapter<>(CrearTareaActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, listaUsuarios);
                    acAsignado.setAdapter(adapter);
                    layoutAsignado.setHint("Asignar A (Opcional)");
                    layoutAsignado.setEnabled(true); // Habilitar

                    acAsignado.setOnItemClickListener((parent, view, position, id) -> {
                        usuarioSeleccionado = (User) parent.getItemAtPosition(position);
                    });
                });
            }

            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(CrearTareaActivity.this, "Error al cargar usuarios: " + error, Toast.LENGTH_SHORT).show();
                    layoutAsignado.setHint("Error al cargar usuarios");
                    layoutAsignado.setEnabled(false);
                });
            }
        });
    }

    /**
     * Guarda la nueva tarea
     */
    private void guardarTarea() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        // Validaciones
        if (titulo.isEmpty()) {
            etTitulo.setError("El título es requerido");
            etTitulo.requestFocus();
            return;
        }

        if (proyectoSeleccionado == null) {
            acProyecto.setError("Debes seleccionar un proyecto");
            acProyecto.requestFocus();
            return;
        }

        int proyectoId = proyectoSeleccionado.getId();

        Integer asignadoId = null;
        if (usuarioSeleccionado != null) {
            asignadoId = usuarioSeleccionado.getId();
        }

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        // Llamar al ApiClient
        apiClient.createTask(titulo, descripcion, fechaSeleccionada.isEmpty() ? null : fechaSeleccionada, proyectoId, asignadoId, new ApiClient.ApiCallback<Tarea>() {

            @Override
            public void onSuccess(Tarea tareaCreada) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(CrearTareaActivity.this, "Tarea creada con ID: " + tareaCreada.getId(), Toast.LENGTH_LONG).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Guardar Tarea");
                    Toast.makeText(CrearTareaActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
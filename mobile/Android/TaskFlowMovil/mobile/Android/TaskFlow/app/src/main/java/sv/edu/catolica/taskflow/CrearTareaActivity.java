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

import java.util.ArrayList; // <-- Importante para el filtro
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

        setupToolbar(); // Configura la Toolbar
        apiClient = new ApiClient(this);

        // 1. Vincular las Vistas
        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFecha = findViewById(R.id.etFecha);
        acProyecto = findViewById(R.id.acProyecto);
        acAsignado = findViewById(R.id.acAsignado);
        btnGuardar = findViewById(R.id.btnGuardar);

        // 2. Configurar Listeners
        btnGuardar.setOnClickListener(v -> guardarTarea());
        setupDatePicker();

        // 3. Cargar datos para los Spinners
        loadSpinnersData();
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Crear Nueva Tarea");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupDatePicker() {
        etFecha.setOnClickListener(v -> {
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
        });
    }

    private void loadSpinnersData() {
        // Cargar Proyectos
        apiClient.getProyectos(new ApiClient.ApiCallback<List<ProyectoSimple>>() {
            @Override
            public void onSuccess(List<ProyectoSimple> result) {
                listaProyectos = result;
                new Handler(Looper.getMainLooper()).post(() -> {
                    ArrayAdapter<ProyectoSimple> adapter = new ArrayAdapter<>(CrearTareaActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, listaProyectos);

                    acProyecto.setAdapter(adapter);

                    acProyecto.setOnItemClickListener((parent, view, position, id) -> {
                        proyectoSeleccionado = (ProyectoSimple) parent.getItemAtPosition(position);
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

        // Cargar Usuarios
        apiClient.getUsuarios(new ApiClient.ApiCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {

                // --- INICIO DEL CÓDIGO DE FILTRADO (COMO QUERÍAS) ---
                List<User> usuariosFiltrados = new ArrayList<>();
                if (result != null) {
                    for (User usuario : result) {
                        // Usamos el método isAdmin() de tu modelo User.java
                        if (!usuario.isAdmin()) {
                            usuariosFiltrados.add(usuario);
                        }
                    }
                }
                // --- FIN DEL CÓDIGO DE FILTRADO ---

                // Guardamos la lista filtrada
                listaUsuarios = usuariosFiltrados;

                new Handler(Looper.getMainLooper()).post(() -> {
                    // ¡Usamos la lista 'usuariosFiltrados' en el adaptador!
                    ArrayAdapter<User> adapter = new ArrayAdapter<>(CrearTareaActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, usuariosFiltrados);

                    acAsignado.setAdapter(adapter);

                    acAsignado.setOnItemClickListener((parent, view, position, id) -> {
                        usuarioSeleccionado = (User) parent.getItemAtPosition(position);
                    });
                });
            }
            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(CrearTareaActivity.this, "Error al cargar usuarios: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void guardarTarea() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        // 4. Validaciones
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

        // 5. Obtener IDs
        int proyectoId = proyectoSeleccionado.getId();

        Integer asignadoId = null;
        if (usuarioSeleccionado != null) {
            asignadoId = usuarioSeleccionado.getId();
        }

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        // 6. ¡Llamar al ApiClient!
        // --- AQUÍ ESTABA EL ERROR ---
        // Ahora tiene los 'onSuccess' y 'onError' correctos
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
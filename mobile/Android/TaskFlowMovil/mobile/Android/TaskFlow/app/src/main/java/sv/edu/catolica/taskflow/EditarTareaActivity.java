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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import sv.edu.catolica.taskflow.api.ApiClient;
import sv.edu.catolica.taskflow.models.ProyectoSimple;
import sv.edu.catolica.taskflow.models.Tarea;
import sv.edu.catolica.taskflow.models.User;

public class EditarTareaActivity extends AppCompatActivity {

    public static final String EXTRA_TAREA_ID = "EXTRA_TAREA_ID";

    private ApiClient apiClient;
    private Toolbar toolbar;

    // Vistas del formulario
    private TextInputEditText etTitulo, etDescripcion, etFecha;
    private AutoCompleteTextView acProyecto, acAsignado;
    private Button btnGuardar;

    // Datos
    private List<ProyectoSimple> listaProyectos;
    private List<User> listaUsuarios;
    private Tarea tareaActual;
    private int tareaId;

    // Selección
    private ProyectoSimple proyectoSeleccionado;
    private User usuarioSeleccionado;
    private String fechaSeleccionada = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Usa el NUEVO layout que acabamos de crear
        setContentView(R.layout.activity_editar_tarea);

        // Obtener el ID de la tarea a editar
        tareaId = getIntent().getIntExtra(EXTRA_TAREA_ID, -1);
        if (tareaId == -1) {
            Toast.makeText(this, "Error: No se encontró la tarea", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupToolbar();
        apiClient = new ApiClient(this);
        bindViews();

        btnGuardar.setText("Actualizar Tarea"); // Cambiar texto del botón

        // Iniciar la carga de datos
        loadTaskDetails();
        loadSpinnersData();
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Editar Tarea");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void bindViews() {
        // IDs del layout
        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFecha = findViewById(R.id.etFecha);
        acProyecto = findViewById(R.id.acProyecto);
        acAsignado = findViewById(R.id.acAsignado);
        btnGuardar = findViewById(R.id.btnGuardar);

        // Listeners
        btnGuardar.setOnClickListener(v -> actualizarTarea());
        setupDatePicker();
    }

    private void setupDatePicker() {
        etFecha.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        fechaSeleccionada = String.format(Locale.US, "%d-%02d-%02d", year, (month + 1), day);
                        String fechaUsuario = String.format(Locale.US, "%02d/%02d/%d", day, (month + 1), year);
                        etFecha.setText(fechaUsuario);
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
    }

    // --- Lógica de Carga de Datos ---

    private void loadTaskDetails() {
        apiClient.getTaskDetails(tareaId, new ApiClient.ApiCallback<Tarea>() {
            @Override
            public void onSuccess(Tarea result) {
                tareaActual = result;
                new Handler(Looper.getMainLooper()).post(EditarTareaActivity.this::tryPopulateForm);
            }
            @Override
            public void onError(String error) {
                // ... manejar error ...
            }
        });
    }

    private void loadSpinnersData() {
        // Cargar Proyectos
        apiClient.getProyectos(new ApiClient.ApiCallback<List<ProyectoSimple>>() {
            @Override
            public void onSuccess(List<ProyectoSimple> result) {
                listaProyectos = result;
                new Handler(Looper.getMainLooper()).post(() -> {
                    ArrayAdapter<ProyectoSimple> adapter = new ArrayAdapter<>(EditarTareaActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, listaProyectos);
                    acProyecto.setAdapter(adapter);
                    acProyecto.setOnItemClickListener((p, v, pos, id) -> proyectoSeleccionado = (ProyectoSimple) p.getItemAtPosition(pos));
                    tryPopulateForm(); // Intentar rellenar
                });
            }
            @Override public void onError(String error) { /* ... */ }
        });

        // Cargar Usuarios
        apiClient.getUsuarios(new ApiClient.ApiCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                listaUsuarios = result;
                new Handler(Looper.getMainLooper()).post(() -> {
                    ArrayAdapter<User> adapter = new ArrayAdapter<>(EditarTareaActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, listaUsuarios);
                    acAsignado.setAdapter(adapter);
                    acAsignado.setOnItemClickListener((p, v, pos, id) -> usuarioSeleccionado = (User) p.getItemAtPosition(pos));
                    tryPopulateForm(); // Intentar rellenar
                });
            }
            @Override public void onError(String error) { /* ... */ }
        });
    }

    private synchronized void tryPopulateForm() {
        if (tareaActual == null || listaProyectos == null || listaUsuarios == null) {
            return; // Esperar a que carguen todos los datos
        }

        // Rellenar campos simples
        etTitulo.setText(tareaActual.getTitulo());
        etDescripcion.setText(tareaActual.getDescripcion());

        // Rellenar fecha
        if (tareaActual.getFecha_vencimiento() != null) {
            fechaSeleccionada = tareaActual.getFecha_vencimiento();
            etFecha.setText(formatDateForUser(fechaSeleccionada));
        }

        // Rellenar Proyecto
        for (ProyectoSimple p : listaProyectos) {
            if (p.getId() == tareaActual.getProyecto_id()) {
                proyectoSeleccionado = p;
                acProyecto.setText(p.toString(), false); // false para no filtrar
                break;
            }
        }

        // Rellenar Asignado
        if (tareaActual.getAsignado() != null) {
            for (User u : listaUsuarios) {
                if (u.getId() == tareaActual.getAsignado().getId()) {
                    usuarioSeleccionado = u;
                    acAsignado.setText(u.toString(), false);
                    break;
                }
            }
        }
    }

    // --- Lógica de Guardado ---

    private void actualizarTarea() {
        String titulo = etTitulo.getText().toString().trim();
        if (titulo.isEmpty()) {
            etTitulo.setError("El título es requerido");
            return;
        }
        if (proyectoSeleccionado == null) {
            acProyecto.setError("Debes seleccionar un proyecto");
            return;
        }

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Actualizando...");

        int proyectoId = proyectoSeleccionado.getId();
        Integer asignadoId = (usuarioSeleccionado != null) ? usuarioSeleccionado.getId() : null;

        apiClient.updateTask(
                tareaActual.getId(),
                titulo,
                etDescripcion.getText().toString().trim(),
                fechaSeleccionada.isEmpty() ? null : fechaSeleccionada,
                proyectoId,
                asignadoId,
                new ApiClient.ApiCallback<Tarea>() {
                    @Override
                    public void onSuccess(Tarea result) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(EditarTareaActivity.this, "Tarea actualizada", Toast.LENGTH_LONG).show();
                            finish(); // Cerrar y volver a la lista
                        });
                    }
                    @Override
                    public void onError(String error) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            btnGuardar.setEnabled(true);
                            btnGuardar.setText("Actualizar Tarea");
                            Toast.makeText(EditarTareaActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    // --- Utilidades ---

    private String formatDateForUser(String yyyyMMdd) {
        if(yyyyMMdd == null || yyyyMMdd.isEmpty()) return "";
        try {
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            Date date = inFormat.parse(yyyyMMdd);
            return outFormat.format(date);
        } catch (ParseException e) {
            return yyyyMMdd;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
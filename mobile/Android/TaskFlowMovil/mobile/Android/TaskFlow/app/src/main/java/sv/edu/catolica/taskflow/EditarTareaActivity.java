package sv.edu.catolica.taskflow;

import android.app.DatePickerDialog;
import android.content.SharedPreferences; // <-- Importado
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList; // <-- Importado
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
    private TextInputLayout layoutAsignado; // <-- Vista del layout "Asignar A"
    private Button btnGuardar;

    // Datos
    private List<ProyectoSimple> listaProyectos;
    private List<User> listaUsuarios;
    private Tarea tareaActual;
    private int tareaId;
    private int userId; // ID del usuario logueado

    // Selección
    private ProyectoSimple proyectoSeleccionado;
    private User usuarioSeleccionado;
    private String fechaSeleccionada = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_tarea);

        // Obtener el ID de la tarea a editar
        tareaId = getIntent().getIntExtra(EXTRA_TAREA_ID, -1);
        if (tareaId == -1) {
            Toast.makeText(this, "Error: No se encontró la tarea", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Obtener el ID del usuario actual
        SharedPreferences prefs = getSharedPreferences("TaskFlowPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", 0);

        setupToolbar();
        apiClient = new ApiClient(this);
        bindViews();

        btnGuardar.setText("Actualizar Tarea");

        // ¡Orden de carga modificado!
        loadProyectos();
        loadTaskDetails();
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
        layoutAsignado = findViewById(R.id.layoutAsignado); // <-- Vinculado
        btnGuardar = findViewById(R.id.btnGuardar);

        // Listeners
        btnGuardar.setOnClickListener(v -> actualizarTarea());
        etFecha.setOnClickListener(v -> setupDatePicker());

        // Listener para cargar usuarios cuando se selecciona un proyecto
        acProyecto.setOnItemClickListener((parent, view, position, id) -> {
            proyectoSeleccionado = (ProyectoSimple) parent.getItemAtPosition(position);
            // Limpiar selección de usuario si cambia el proyecto
            usuarioSeleccionado = null;
            acAsignado.setText("", false);
            // Cargar usuarios para el nuevo proyecto seleccionado
            cargarUsuariosDelProyecto(proyectoSeleccionado.getId(), null);
        });
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

                // --- LÓGICA DE VALIDACIÓN (REGLA 1) ---
                boolean isAsignadoAMi = (result.getAsignado() != null && result.getAsignado().getId() == userId);
                boolean isCreadoPorMi = (result.getCreado_por() != null && result.getCreado_por().getId() == userId);

                if (isAsignadoAMi && !isCreadoPorMi) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(EditarTareaActivity.this, "No puedes editar una tarea que te fue asignada por otra persona.", Toast.LENGTH_LONG).show();
                        finish();
                    });
                    return;
                }
                // --- FIN DE VALIDACIÓN ---

                tareaActual = result;
                tryPopulateForm(); // Intentar rellenar
            }
            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(EditarTareaActivity.this, "Error al cargar la tarea: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    /**
     * Carga solo la lista de Proyectos
     */
    private void loadProyectos() {
        apiClient.getProyectos(new ApiClient.ApiCallback<List<ProyectoSimple>>() {
            @Override
            public void onSuccess(List<ProyectoSimple> result) {
                listaProyectos = result;
                new Handler(Looper.getMainLooper()).post(() -> {
                    ArrayAdapter<ProyectoSimple> adapter = new ArrayAdapter<>(EditarTareaActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, listaProyectos);
                    acProyecto.setAdapter(adapter);
                    tryPopulateForm(); // Intentar rellenar
                });
            }
            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(EditarTareaActivity.this, "Error al cargar proyectos: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Carga los usuarios (miembros) de un proyecto específico (REGLA 2 y 3)
     */
    private void cargarUsuariosDelProyecto(int proyectoId, @Nullable Integer usuarioAsignadoId) {
        layoutAsignado.setEnabled(false);
        layoutAsignado.setHint("Cargando usuarios...");

        apiClient.getUsuariosDelProyecto(proyectoId, new ApiClient.ApiCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                // --- FILTRAR ADMINS (REGLA 3) ---
                List<User> usuariosFiltrados = new ArrayList<>();
                if (result != null) {
                    for (User usuario : result) {
                        if (!usuario.isAdmin()) {
                            usuariosFiltrados.add(usuario);
                        }
                    }
                }
                listaUsuarios = usuariosFiltrados; // Guardar la lista filtrada

                new Handler(Looper.getMainLooper()).post(() -> {
                    ArrayAdapter<User> adapter = new ArrayAdapter<>(EditarTareaActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, listaUsuarios);
                    acAsignado.setAdapter(adapter);
                    layoutAsignado.setHint("Asignar A (Opcional)");
                    layoutAsignado.setEnabled(true);

                    // Pre-seleccionar al usuario si se pasó el ID
                    if (usuarioAsignadoId != null) {
                        for (User u : listaUsuarios) {
                            if (u.getId() == usuarioAsignadoId) {
                                usuarioSeleccionado = u;
                                acAsignado.setText(u.toString(), false);
                                break;
                            }
                        }
                    }

                    acAsignado.setOnItemClickListener((parent, view, position, id) -> {
                        usuarioSeleccionado = (User) parent.getItemAtPosition(position);
                    });
                });
            }
            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(EditarTareaActivity.this, "Error al cargar usuarios: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    /**
     * Rellena el formulario una vez que la Tarea y los Proyectos han cargado.
     * Luego, dispara la carga de Usuarios.
     */
    private synchronized void tryPopulateForm() {
        // Espera a que la TAREA y los PROYECTOS carguen
        if (tareaActual == null || listaProyectos == null) {
            return;
        }

        // Rellenar campos simples
        etTitulo.setText(tareaActual.getTitulo());
        etDescripcion.setText(tareaActual.getDescripcion());
        if (tareaActual.getFecha_vencimiento() != null) {
            fechaSeleccionada = tareaActual.getFecha_vencimiento();
            etFecha.setText(formatDateForUser(fechaSeleccionada));
        }

        // Pre-seleccionar Proyecto
        for (ProyectoSimple p : listaProyectos) {
            if (p.getId() == tareaActual.getProyecto_id()) {
                proyectoSeleccionado = p;
                acProyecto.setText(p.toString(), false);
                break;
            }
        }

        // DISPARAR LA CARGA DE USUARIOS
        Integer idUsuarioAsignado = (tareaActual.getAsignado() != null) ? tareaActual.getAsignado().getId() : null;

        if (proyectoSeleccionado != null) {
            cargarUsuariosDelProyecto(proyectoSeleccionado.getId(), idUsuarioAsignado);
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
            // Esto es por si el usuario borra la selección
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
                            finish();
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
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
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
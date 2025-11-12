package sv.edu.catolica.taskflow;

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

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import sv.edu.catolica.taskflow.api.ApiClient;
import sv.edu.catolica.taskflow.models.ProyectoSimple;
import sv.edu.catolica.taskflow.models.User;

public class AsignarUsuarioActivity extends AppCompatActivity {

    private ApiClient apiClient;
    private Toolbar toolbar;
    private AutoCompleteTextView acProyecto, acUsuario;
    private TextInputLayout layoutUsuario;
    private Button btnAsignar;

    private List<ProyectoSimple> listaProyectos;
    private List<User> listaUsuariosNoAsignados;

    private ProyectoSimple proyectoSeleccionado;
    private User usuarioSeleccionado;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asignar_usuario);

        initViews();
        initApiClient();
        setupToolbar();
        setupListeners();

        loadProyectos();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        acProyecto = findViewById(R.id.acProyecto);
        acUsuario = findViewById(R.id.acUsuario);
        layoutUsuario = findViewById(R.id.layoutUsuario);
        btnAsignar = findViewById(R.id.btnAsignar);
    }

    private void initApiClient() {
        apiClient = new ApiClient(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Asignar Usuarios a Proyecto");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupListeners() {
        // 1. Cuando se selecciona un Proyecto
        acProyecto.setOnItemClickListener((parent, view, position, id) -> {
            proyectoSeleccionado = (ProyectoSimple) parent.getItemAtPosition(position);
            // Limpiar y cargar la lista de usuarios
            limpiarCamposDeUsuario();
            cargarUsuariosNoAsignados(proyectoSeleccionado.getId());
        });

        // 2. Cuando se selecciona un Usuario
        acUsuario.setOnItemClickListener((parent, view, position, id) -> {
            usuarioSeleccionado = (User) parent.getItemAtPosition(position);
            btnAsignar.setEnabled(true); // Activar el botón de guardar
        });

        // 3. Al presionar el botón de Asignar
        btnAsignar.setOnClickListener(v -> {
            if (proyectoSeleccionado != null && usuarioSeleccionado != null) {
                asignarUsuario();
            }
        });
    }

    private void loadProyectos() {
        // Carga los proyectos del usuario (los que él creó)
        apiClient.getProyectos(new ApiClient.ApiCallback<List<ProyectoSimple>>() {
            @Override
            public void onSuccess(List<ProyectoSimple> result) {
                listaProyectos = result;
                new Handler(Looper.getMainLooper()).post(() -> {
                    ArrayAdapter<ProyectoSimple> adapter = new ArrayAdapter<>(AsignarUsuarioActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, listaProyectos);
                    acProyecto.setAdapter(adapter);
                });
            }
            @Override
            public void onError(String error) {
                Toast.makeText(AsignarUsuarioActivity.this, "Error al cargar proyectos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarUsuariosNoAsignados(int proyectoId) {
        layoutUsuario.setHint("Cargando usuarios...");
        layoutUsuario.setEnabled(false);

        apiClient.getUsuariosNoAsignados(proyectoId, new ApiClient.ApiCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                // Filtramos a los ADMINs (regla de negocio)
                List<User> usuariosFiltrados = new ArrayList<>();
                if (result != null) {
                    for (User u : result) {
                        if (!u.isAdmin()) {
                            usuariosFiltrados.add(u);
                        }
                    }
                }
                listaUsuariosNoAsignados = usuariosFiltrados;

                new Handler(Looper.getMainLooper()).post(() -> {
                    ArrayAdapter<User> adapter = new ArrayAdapter<>(AsignarUsuarioActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, listaUsuariosNoAsignados);
                    acUsuario.setAdapter(adapter);
                    layoutUsuario.setHint("2. Selecciona un Usuario");
                    layoutUsuario.setEnabled(true);
                });
            }
            @Override
            public void onError(String error) {
                Toast.makeText(AsignarUsuarioActivity.this, "Error al cargar usuarios no asignados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void asignarUsuario() {
        btnAsignar.setEnabled(false);
        btnAsignar.setText("Asignando...");

        // Asignamos con el rol "COLABORADOR" por defecto
        apiClient.asignarUsuarioAProyecto(proyectoSeleccionado.getId(), usuarioSeleccionado.getId(), "COLABORADOR", new ApiClient.ApiCallback<User>() {
            @Override
            public void onSuccess(User result) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(AsignarUsuarioActivity.this, result.getNombre() + " fue asignado a " + proyectoSeleccionado.getTitulo(), Toast.LENGTH_LONG).show();
                    // Recargar la lista de usuarios no asignados (el usuario recién agregado ya no aparecerá)
                    limpiarCamposDeUsuario();
                    cargarUsuariosNoAsignados(proyectoSeleccionado.getId());
                });
            }

            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(AsignarUsuarioActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    btnAsignar.setEnabled(true);
                    btnAsignar.setText("Asignar Usuario al Proyecto");
                });
            }
        });
    }

    private void limpiarCamposDeUsuario() {
        usuarioSeleccionado = null;
        acUsuario.setText("", false);
        btnAsignar.setEnabled(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
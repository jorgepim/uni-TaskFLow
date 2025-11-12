package sv.edu.catolica.taskflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Importa el NUEVO adaptador
import sv.edu.catolica.taskflow.adapters.MisTareasAdapter;
import sv.edu.catolica.taskflow.api.ApiClient;
import sv.edu.catolica.taskflow.models.Tarea;
// Importa el modelo de respuesta para getUserTasks
import sv.edu.catolica.taskflow.models.TareasResponse;


// Implementa la interfaz del NUEVO adaptador
public class MisTareasActivity extends AppCompatActivity implements MisTareasAdapter.OnTareaActionListener {

    private Toolbar toolbar;
    private RecyclerView recyclerViewTareas;
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fabAddTask;

    private MisTareasAdapter misTareasAdapter; // <-- CAMBIO
    private ApiClient apiClient;

    private int userId;
    private boolean isAdmin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_tareas);

        initViews();
        initApiClient();
        loadUserData();
        setupToolbar();
        setupRecyclerView(); // <-- CAMBIO
        setupListeners();

        loadMisTareas(); // <-- CAMBIO
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewTareas = findViewById(R.id.recyclerViewTareas);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        fabAddTask = findViewById(R.id.fabAddTask);
    }

    private void initApiClient() {
        apiClient = new ApiClient(this);
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("TaskFlowPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", 0);
        isAdmin = prefs.getBoolean("is_admin", false);

        if (isAdmin) {
            fabAddTask.setVisibility(View.GONE);
        } else {
            fabAddTask.setVisibility(View.VISIBLE);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mis Tareas");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupRecyclerView() {
        // --- CAMBIO ---
        misTareasAdapter = new MisTareasAdapter(this); // Usa el nuevo adaptador
        misTareasAdapter.setUserId(this.userId);
        recyclerViewTareas.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTareas.setAdapter(misTareasAdapter); // Setea el nuevo adaptador
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadMisTareas); // Llama a la nueva función
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MisTareasActivity.this, CrearTareaActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMisTareas(); // Llama a la nueva función
    }

    /**
     * ¡LÓGICA DE CARGA REVERTIDA!
     * Llama a 'getUserTasks' para obtener una lista plana que SÍ funciona.
     */
    private void loadMisTareas() {
        swipeRefresh.setRefreshing(true);

        // Llama al endpoint original que SÍ te da todas tus tareas
        apiClient.getUserTasks(userId, null, null, null, new ApiClient.ApiCallback<TareasResponse>() {
            @Override
            public void onSuccess(TareasResponse result) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    swipeRefresh.setRefreshing(false);

                    List<Tarea> tareasAsignadas = new ArrayList<>();
                    if (result != null && result.getTareas() != null) {
                        tareasAsignadas = result.getTareas();
                    }

                    // Pasa la lista plana al nuevo adaptador
                    misTareasAdapter.setTareas(tareasAsignadas);

                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(String.format(Locale.US, "Mis Tareas (%d)", tareasAsignadas.size()));
                    }
                });
            }

            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(MisTareasActivity.this, "Error al cargar mis tareas: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    public void onEstadoChanged(Tarea tarea, String nuevoEstado) {
        apiClient.updateTaskStatus(tarea.getId(), nuevoEstado, new ApiClient.ApiCallback<Tarea>() {
            @Override
            public void onSuccess(Tarea result) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(MisTareasActivity.this, "Estado actualizado", Toast.LENGTH_SHORT).show();
                    loadMisTareas(); // Recarga la lista plana
                });
            }
            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(MisTareasActivity.this, "Error al actualizar: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    public void onEditClicked(Tarea tarea) {
        String estado = tarea.getEstado();
        if ("PENDIENTE".equals(estado)) {
            Intent intent = new Intent(this, EditarTareaActivity.class);
            intent.putExtra(EditarTareaActivity.EXTRA_TAREA_ID, tarea.getId());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Solo se pueden editar tareas en estado 'PENDIENTE'", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
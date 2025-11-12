package sv.edu.catolica.taskflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem; // <-- IMPORTA ESTE
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle; // <-- IMPORTA ESTE
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat; // <-- IMPORTA ESTE
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import sv.edu.catolica.taskflow.adapters.TareasAdapter;
import sv.edu.catolica.taskflow.api.ApiClient;
import sv.edu.catolica.taskflow.models.Proyecto;
import sv.edu.catolica.taskflow.models.ProyectoSimple;
import sv.edu.catolica.taskflow.models.Tarea;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, TareasAdapter.OnTareaActionListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private RecyclerView recyclerViewTareas;
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fabAddTask;

    private TareasAdapter tareasAdapter;
    private ApiClient apiClient;

    private int userId;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initApiClient();
        loadUserData(); // Carga userId y isAdmin
        setupToolbar();
        setupNavigationDrawer();
        setupRecyclerView();
        setupListeners();

        loadProyectosYTareas();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
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
            navigationView.getMenu().findItem(R.id.nav_stats).setVisible(true);
            navigationView.getMenu().findItem(R.id.nav_crear_proyecto).setVisible(false);
            fabAddTask.setVisibility(View.GONE);
        } else {
            navigationView.getMenu().findItem(R.id.nav_stats).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_crear_proyecto).setVisible(true);
            fabAddTask.setVisibility(View.VISIBLE);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Todas las Tareas");
        }
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_todas_tareas);
    }

    private void setupRecyclerView() {
        tareasAdapter = new TareasAdapter(this);
        tareasAdapter.setUserId(this.userId);
        recyclerViewTareas.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTareas.setAdapter(tareasAdapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadProyectosYTareas);
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CrearTareaActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProyectosYTareas();
    }

    /**
     * Lógica de carga ("Todas las Tareas" - Vista de Supervisor)
     */
    private void loadProyectosYTareas() {
        swipeRefresh.setRefreshing(true);

        apiClient.getProyectos(new ApiClient.ApiCallback<List<ProyectoSimple>>() {
            @Override
            public void onSuccess(List<ProyectoSimple> proyectosSimples) {
                if (proyectosSimples == null || proyectosSimples.isEmpty()) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        swipeRefresh.setRefreshing(false);
                        tareasAdapter.setProyectos(new ArrayList<>());
                        if (getSupportActionBar() != null) {
                            // Título corregido
                            getSupportActionBar().setTitle("Todas las Tareas (0)");
                        }
                    });
                    return;
                }

                List<Proyecto> proyectosCompletos = Collections.synchronizedList(new ArrayList<>());
                AtomicInteger counter = new AtomicInteger(proyectosSimples.size());

                for (ProyectoSimple pSimple : proyectosSimples) {
                    apiClient.getProjectDetails(pSimple.getId(), new ApiClient.ApiCallback<Proyecto>() {
                        @Override
                        public void onSuccess(Proyecto pCompleto) {
                            List<Tarea> tasksToShow = new ArrayList<>();
                            boolean isCreator = (pCompleto.getCreado_por() != null && pCompleto.getCreado_por().getId() == userId);

                            if (pCompleto.getTareas() != null) {
                                if (isCreator) {
                                    // Regla: El Creador ve TODAS las tareas
                                    tasksToShow.addAll(pCompleto.getTareas());
                                } else {
                                    // Regla: Colaborador solo ve tareas asignadas a él
                                    for (Tarea tarea : pCompleto.getTareas()) {
                                        boolean isAsignado = (tarea.getAsignado() != null && tarea.getAsignado().getId() == userId);
                                        if (isAsignado) {
                                            tasksToShow.add(tarea);
                                        }
                                    }
                                }
                            }

                            pCompleto.setTareas(tasksToShow);
                            if (!tasksToShow.isEmpty()) {
                                proyectosCompletos.add(pCompleto);
                            }

                            if (counter.decrementAndGet() == 0) {
                                updateAdapterInMainThread(proyectosCompletos);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            if (counter.decrementAndGet() == 0) {
                                updateAdapterInMainThread(proyectosCompletos);
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(MainActivity.this, "Error al cargar proyectos: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void updateAdapterInMainThread(List<Proyecto> proyectosCompletos) {
        new Handler(Looper.getMainLooper()).post(() -> {
            swipeRefresh.setRefreshing(false);

            int totalTareas = 0;
            for (Proyecto p : proyectosCompletos) {
                totalTareas += p.getTareas().size();
            }

            tareasAdapter.setProyectos(proyectosCompletos);

            if (getSupportActionBar() != null) {
                // --- CAMBIO AQUÍ: Título corregido ---
                getSupportActionBar().setTitle(String.format(Locale.US, "Todas las Tareas (%d)", totalTareas));
            }
        });
    }

    // --- Métodos de la Interfaz (sin cambios) ---

    @Override
    public void onEstadoChanged(Tarea tarea, String nuevoEstado) {
        apiClient.updateTaskStatus(tarea.getId(), nuevoEstado, new ApiClient.ApiCallback<Tarea>() {
            @Override
            public void onSuccess(Tarea result) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(MainActivity.this, "Estado actualizado correctamente", Toast.LENGTH_SHORT).show();
                    loadProyectosYTareas();
                });
            }

            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(MainActivity.this, "Error al actualizar: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    public void onEditClicked(Tarea tarea) {
        String estado = tarea.getEstado();
        if ("PENDIENTE".equals(estado)) {
            Intent intent = new Intent(MainActivity.this, EditarTareaActivity.class);
            intent.putExtra(EditarTareaActivity.EXTRA_TAREA_ID, tarea.getId());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Solo se pueden editar tareas en estado 'PENDIENTE'", Toast.LENGTH_SHORT).show();
        }
    }

    // --- MÉTODO ARREGLADO ---
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_todas_tareas) {
            // Ya estamos aquí, solo cierra el menú

        } else if (id == R.id.nav_mis_tareas) {
            Intent intent = new Intent(this, MisTareasActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_crear_proyecto) {
            // Este código faltaba
            Intent intent = new Intent(this, CrearProyectoActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_stats && isAdmin) {
            // Este código faltaba
            Intent intent = new Intent(this, StatsActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_profile) {
            // Este código faltaba
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_logout) {
            // Este código faltaba
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        apiClient.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
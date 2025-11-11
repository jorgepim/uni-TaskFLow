package sv.edu.catolica.taskflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.navigation.NavigationView;
import sv.edu.catolica.taskflow.adapters.TareasAdapter;
import sv.edu.catolica.taskflow.api.ApiClient;
import sv.edu.catolica.taskflow.models.Tarea;
import sv.edu.catolica.taskflow.models.TareasResponse;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, TareasAdapter.OnTareaActionListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private RecyclerView recyclerViewTareas;
    private SwipeRefreshLayout swipeRefresh;
    private Spinner spinnerEstado;
    private Button btnFilter;

    private TareasAdapter tareasAdapter;
    private ApiClient apiClient;

    private int userId;
    private boolean isAdmin;
    private String currentFilter = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initApiClient();
        loadUserData();
        setupToolbar();
        setupNavigationDrawer();
        setupRecyclerView();
        setupSpinner();
        setupListeners();

        loadTareas();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        recyclerViewTareas = findViewById(R.id.recyclerViewTareas);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        btnFilter = findViewById(R.id.btnFilter);
    }

    private void initApiClient() {
        apiClient = new ApiClient(this);
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("TaskFlowPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", 0);
        isAdmin = prefs.getBoolean("is_admin", false);

        // Mostrar estadísticas solo para admin
        if (isAdmin) {
            navigationView.getMenu().findItem(R.id.nav_stats).setVisible(true);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mis Tareas");
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
    }

    private void setupRecyclerView() {
        tareasAdapter = new TareasAdapter(this);
        tareasAdapter.setCanUpdateStatus(true); // Los usuarios pueden cambiar el estado de sus tareas
        recyclerViewTareas.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTareas.setAdapter(tareasAdapter);
    }

    private void setupSpinner() {
        String[] estados = {"Todos", "PENDIENTE", "PROGRESO", "COMPLETADA"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, estados);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadTareas);

        btnFilter.setOnClickListener(v -> {
            String selectedEstado = spinnerEstado.getSelectedItem().toString();
            currentFilter = "Todos".equals(selectedEstado) ? "" : selectedEstado;
            loadTareas();
        });
    }

    private void loadTareas() {
        swipeRefresh.setRefreshing(true);

        apiClient.getUserTasks(userId, currentFilter, null, null, new ApiClient.ApiCallback<TareasResponse>() {
            @Override
            public void onSuccess(TareasResponse result) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    swipeRefresh.setRefreshing(false);
                    tareasAdapter.setTareas(result.getTareas());

                    // Actualizar título con estadísticas
                    if (result.getEstadisticas() != null) {
                        String titulo = String.format("Mis Tareas (%d)", result.getEstadisticas().getTotal());
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(titulo);
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MainActivity.this, "Estado actualizado correctamente", Toast.LENGTH_SHORT).show();
                    loadTareas(); // Recargar tareas
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_tareas) {
            // Ya estamos en tareas
        } else if (id == R.id.nav_stats && isAdmin) {
            Intent intent = new Intent(this, StatsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
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

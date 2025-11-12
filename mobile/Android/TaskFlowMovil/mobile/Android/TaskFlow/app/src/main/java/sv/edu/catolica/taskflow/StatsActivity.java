package sv.edu.catolica.taskflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull; // <-- AÑADIDO
import androidx.annotation.Nullable; // <-- AÑADIDO
import androidx.appcompat.app.ActionBarDrawerToggle; // <-- AÑADIDO
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat; // <-- AÑADIDO
import androidx.drawerlayout.widget.DrawerLayout; // <-- AÑADIDO
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView; // <-- AÑADIDO
import sv.edu.catolica.taskflow.adapters.ProyectosStatsAdapter;
import sv.edu.catolica.taskflow.api.ApiClient;
import sv.edu.catolica.taskflow.models.ProjectStatsResponse;

import java.util.Map;

// --- CAMBIO: Implementa la interfaz de navegación ---
public class StatsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // --- AÑADIDOS: Vistas de Navegación ---
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar; // Movido aquí

    // Vistas de Estadísticas
    private TextView tvTotalProyectos, tvTotalTareas, tvPorcentajeCompletado;
    private LinearLayout layoutEstados;
    private RecyclerView recyclerViewProyectos;
    private ProgressBar progressBar;

    private ApiClient apiClient;
    private ProyectosStatsAdapter proyectosAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats); // Asume que el XML tiene el DrawerLayout

        initViews();
        initApiClient();
        setupToolbar();
        setupNavigationDrawer(); // <-- AÑADIDO
        loadAdminMenuLogic(); // <-- AÑADIDO
        setupRecyclerView();

        loadStats();
    }

    private void initViews() {
        // --- Vistas de Navegación AÑADIDAS ---
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        // Vistas de Estadísticas (tu código original)
        tvTotalProyectos = findViewById(R.id.tvTotalProyectos);
        tvTotalTareas = findViewById(R.id.tvTotalTareas);
        tvPorcentajeCompletado = findViewById(R.id.tvPorcentajeCompletado);
        layoutEstados = findViewById(R.id.layoutEstados);
        recyclerViewProyectos = findViewById(R.id.recyclerViewProyectos);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initApiClient() {
        apiClient = new ApiClient(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Estadísticas Globales");
            // La flecha de regreso se reemplaza por el "hamburger icon" del menú
        }
    }

    // --- AÑADIDO: Configuración del Menú Lateral ---
    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Marca "Estadísticas" como la pantalla seleccionada
        navigationView.setCheckedItem(R.id.nav_stats);
    }

    // --- AÑADIDO: Lógica de Menú para ADMIN ---
    private void loadAdminMenuLogic() {
        // SÍ ve Estadísticas
        navigationView.getMenu().findItem(R.id.nav_stats).setVisible(true);

        // ¡TU REGLA APLICADA!
        // OCULTAR todo lo demás
        navigationView.getMenu().findItem(R.id.nav_todas_tareas).setVisible(false);
        navigationView.getMenu().findItem(R.id.nav_mis_tareas).setVisible(false);
        navigationView.getMenu().findItem(R.id.nav_crear_proyecto).setVisible(false);
    }

    private void setupRecyclerView() {
        proyectosAdapter = new ProyectosStatsAdapter();
        recyclerViewProyectos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProyectos.setAdapter(proyectosAdapter);
    }

    private void loadStats() {
        progressBar.setVisibility(View.VISIBLE);
        apiClient.getProjectStats(new ApiClient.ApiCallback<ProjectStatsResponse>() {
            @Override
            public void onSuccess(ProjectStatsResponse result) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressBar.setVisibility(View.GONE);
                    displayStats(result);
                });
            }
            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(StatsActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void displayStats(ProjectStatsResponse stats) {
        tvTotalProyectos.setText(String.valueOf(stats.getTotal_projects()));
        tvTotalTareas.setText(String.valueOf(stats.getTotal_tasks()));
        tvPorcentajeCompletado.setText(String.format("%.1f%%", stats.getPercent_completed_overall()));
        displayEstados(stats.getTasks_by_estado());
        proyectosAdapter.setProyectos(stats.getTop_projects_by_completion());
    }

    private void displayEstados(Map<String, Integer> tasksByEstado) {
        layoutEstados.removeAllViews();
        if (tasksByEstado != null) {
            for (Map.Entry<String, Integer> entry : tasksByEstado.entrySet()) {
                View estadoView = createEstadoView(entry.getKey(), entry.getValue());
                layoutEstados.addView(estadoView);
            }
        }
    }

    private View createEstadoView(String estado, int cantidad) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_estado_stat, layoutEstados, false);
        TextView tvEstado = view.findViewById(R.id.tvEstado);
        TextView tvCantidad = view.findViewById(R.id.tvCantidad);
        View indicatorEstado = view.findViewById(R.id.indicatorEstado);
        tvEstado.setText(estado);
        tvCantidad.setText(String.valueOf(cantidad));
        int color = getColorForEstado(estado);
        indicatorEstado.setBackgroundColor(color);
        return view;
    }

    private int getColorForEstado(String estado) {
        switch (estado) {
            case "PENDIENTE":
                return getResources().getColor(android.R.color.holo_orange_dark);
            case "PROGRESO":
                return getResources().getColor(android.R.color.holo_blue_dark);
            case "COMPLETADA":
                return getResources().getColor(android.R.color.holo_green_dark);
            default:
                return getResources().getColor(android.R.color.darker_gray);
        }
    }

    // --- MANEJO DE NAVEGACIÓN (AÑADIDO) ---

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_stats) {
            // Ya estamos aquí
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            logout();
        }
        // Ignoramos los clics en las vistas de tareas (están ocultas)

        drawerLayout.closeDrawer(GravityCompat.START);
        return true; // <-- Importante
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
        // Cierra el menú si está abierto, en lugar de cerrar la app
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Si el admin está en su "home" (Stats) y presiona atrás,
            // cierra la app (comportamiento normal de "home").
            super.onBackPressed();
        }
    }

    // Se elimina onOptionsItemSelected (reemplazado por el drawer)
}
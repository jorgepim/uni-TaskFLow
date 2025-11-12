package sv.edu.catolica.taskflow;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import sv.edu.catolica.taskflow.adapters.ProyectosStatsAdapter;
import sv.edu.catolica.taskflow.api.ApiClient;
import sv.edu.catolica.taskflow.models.ProjectStatsResponse;

import java.util.Map;

public class StatsActivity extends AppCompatActivity {

    private TextView tvTotalProyectos, tvTotalTareas, tvPorcentajeCompletado;
    private LinearLayout layoutEstados;
    private RecyclerView recyclerViewProyectos;
    private ProgressBar progressBar;

    private ApiClient apiClient;
    private ProyectosStatsAdapter proyectosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        initViews();
        initApiClient();
        setupToolbar();
        setupRecyclerView();

        loadStats();
    }

    private void initViews() {
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Estadísticas");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
        // Resumen general
        tvTotalProyectos.setText(String.valueOf(stats.getTotal_projects()));
        tvTotalTareas.setText(String.valueOf(stats.getTotal_tasks()));
        tvPorcentajeCompletado.setText(String.format("%.1f%%", stats.getPercent_completed_overall()));

        // Estados de tareas
        displayEstados(stats.getTasks_by_estado());

        // Lista de proyectos
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

        // Color del indicador según el estado
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

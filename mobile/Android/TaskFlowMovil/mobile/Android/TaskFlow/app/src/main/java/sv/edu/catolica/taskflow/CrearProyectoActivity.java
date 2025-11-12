package sv.edu.catolica.taskflow;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;

// Importa los modelos necesarios
import sv.edu.catolica.taskflow.api.ApiClient;
import sv.edu.catolica.taskflow.models.Proyecto;

public class CrearProyectoActivity extends AppCompatActivity {

    private ApiClient apiClient;
    private Toolbar toolbar;
    private TextInputEditText etTituloProyecto, etDescripcionProyecto;
    private Button btnGuardarProyecto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_proyecto);

        apiClient = new ApiClient(this);
        setupToolbar();
        bindViews();
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Crear Nuevo Proyecto");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void bindViews() {
        etTituloProyecto = findViewById(R.id.etTituloProyecto);
        etDescripcionProyecto = findViewById(R.id.etDescripcionProyecto);
        btnGuardarProyecto = findViewById(R.id.btnGuardarProyecto);

        btnGuardarProyecto.setOnClickListener(v -> guardarProyecto());
    }

    private void guardarProyecto() {
        String titulo = etTituloProyecto.getText().toString().trim();
        String descripcion = etDescripcionProyecto.getText().toString().trim();

        // Validación
        if (titulo.isEmpty()) {
            etTituloProyecto.setError("El nombre del proyecto es requerido");
            etTituloProyecto.requestFocus();
            return;
        }

        btnGuardarProyecto.setEnabled(false);
        btnGuardarProyecto.setText("Guardando...");

        // Llamar al nuevo método del API Client
        apiClient.createProyecto(titulo, descripcion, new ApiClient.ApiCallback<Proyecto>() {
            @Override
            public void onSuccess(Proyecto result) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(CrearProyectoActivity.this, "Proyecto creado: " + result.getTitulo(), Toast.LENGTH_LONG).show();
                    finish(); // Cerrar la actividad y volver al menú
                });
            }

            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    btnGuardarProyecto.setEnabled(true);
                    btnGuardarProyecto.setText("Guardar Proyecto");
                    Toast.makeText(CrearProyectoActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
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
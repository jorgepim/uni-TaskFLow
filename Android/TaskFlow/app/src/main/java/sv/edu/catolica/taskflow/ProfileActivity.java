package sv.edu.catolica.taskflow;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvNombre, tvEmail, tvRol, tvFechaCreacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupToolbar();
        loadUserData();
    }

    private void initViews() {
        tvNombre = findViewById(R.id.tvNombre);
        tvEmail = findViewById(R.id.tvEmail);
        tvRol = findViewById(R.id.tvRol);
        tvFechaCreacion = findViewById(R.id.tvFechaCreacion);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mi Perfil");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("TaskFlowPrefs", MODE_PRIVATE);

        String nombre = prefs.getString("user_name", "Usuario");
        String email = prefs.getString("user_email", "email@example.com");
        boolean isAdmin = prefs.getBoolean("is_admin", false);

        tvNombre.setText(nombre);
        tvEmail.setText(email);
        tvRol.setText(isAdmin ? "ADMIN" : "USER");

        // Fecha actual como fecha de creación (simplificado)
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaActual = format.format(new Date());
        tvFechaCreacion.setText("Miembro desde: " + fechaActual);
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

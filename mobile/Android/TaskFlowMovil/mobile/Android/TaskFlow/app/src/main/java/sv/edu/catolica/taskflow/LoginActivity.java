package sv.edu.catolica.taskflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import sv.edu.catolica.taskflow.api.ApiClient;
import sv.edu.catolica.taskflow.models.LoginResponse;
import sv.edu.catolica.taskflow.models.User;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si ya está logueado
        SharedPreferences prefs = getSharedPreferences("TaskFlowPrefs", MODE_PRIVATE);
        String token = prefs.getString("auth_token", "");
        if (!token.isEmpty()) {
            startMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        initViews();
        initApiClient();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initApiClient() {
        apiClient = new ApiClient(this);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email es requerido");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Contraseña es requerida");
            return;
        }

        setLoading(true);

        apiClient.login(email, password, new ApiClient.ApiCallback<LoginResponse>() {

            // En LoginActivity.java, dentro de tu llamada a apiClient.login(...)

            @Override
            public void onSuccess(LoginResponse result) {
                // 1. Obtenemos el usuario y su rol
                User user = result.getUser();
                boolean isAdmin = user.isAdmin();

                // 2. Guardamos los datos
                SharedPreferences prefs = getSharedPreferences("TaskFlowPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("auth_token", result.getToken());
                editor.putInt("user_id", user.getId());
                editor.putBoolean("is_admin", isAdmin);
                editor.apply();

                // 3. --- ¡LA LÓGICA DE REDIRECCIÓN! ---
                Intent intent;
                if (isAdmin) {
                    // Si es Admin, la pantalla principal es Estadísticas
                    intent = new Intent(LoginActivity.this, StatsActivity.class);
                } else {
                    // Si es User, la pantalla principal es la lista de tareas
                    intent = new Intent(LoginActivity.this, MainActivity.class);
                }

                // 4. Lanzamos la actividad y limpiamos el historial
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Cierra LoginActivity
            }

            @Override
            public void onError(String error) {
                // Tu código de error existente
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void saveUserData(LoginResponse loginResponse) {
        SharedPreferences prefs = getSharedPreferences("TaskFlowPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("auth_token", loginResponse.getToken());
        editor.putInt("user_id", loginResponse.getUser().getId());
        editor.putString("user_name", loginResponse.getUser().getNombre());
        editor.putString("user_email", loginResponse.getUser().getEmail());
        editor.putBoolean("is_admin", loginResponse.getUser().isAdmin());
        editor.apply();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }
}

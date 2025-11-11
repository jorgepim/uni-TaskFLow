package sv.edu.catolica.taskflow.api;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import sv.edu.catolica.taskflow.models.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    // Configuraciones de URL para diferentes escenarios
    private static final String BASE_URL_WIFI = "http://192.168.0.76:8000/api"; // IP WiFi del servidor
    private static final String BASE_URL_EMULATOR = "http://10.0.2.2:8000/api"; // Para emulador Android Studio
    private static final String BASE_URL_LOCALHOST = "http://127.0.0.1:8000/api"; // Para testing local

    // URL activa - cambiar según tu configuración
    private static final String BASE_URL = BASE_URL_WIFI;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;
    private Gson gson;
    private Context context;

    public ApiClient(Context context) {
        this.context = context;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    private String getAuthToken() {
        SharedPreferences prefs = context.getSharedPreferences("TaskFlowPrefs", Context.MODE_PRIVATE);
        return prefs.getString("auth_token", "");
    }

    private Request.Builder getRequestBuilder() {
        Request.Builder builder = new Request.Builder();
        String token = getAuthToken();
        if (!token.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + token);
        }
        return builder;
    }

    // Callback interfaces
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    // Login
    public void login(String email, String password, ApiCallback<LoginResponse> callback) {
        String json = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/login")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Error de conexión: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    Type type = new TypeToken<ApiResponse<LoginResponse>>(){}.getType();
                    ApiResponse<LoginResponse> apiResponse = gson.fromJson(responseBody, type);
                    if (apiResponse.isSuccessful()) {
                        // Guardar token
                        SharedPreferences prefs = context.getSharedPreferences("TaskFlowPrefs", Context.MODE_PRIVATE);
                        prefs.edit().putString("auth_token", apiResponse.getData().getToken()).apply();

                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Error del servidor: " + response.code());
                }
            }
        });
    }

    // Register
    public void register(String nombre, String email, String password, ApiCallback<LoginResponse> callback) {
        String json = String.format("{\"nombre\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"activo\":true}",
                                   nombre, email, password);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/register")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String errorMsg = "Error de conexión al servidor. Verifica que:\n" +
                               "• El servidor esté ejecutándose en 192.168.0.76:8000\n" +
                               "• Estés conectado a la misma red WiFi\n" +
                               "• El servidor acepte conexiones externas\n\n" +
                               "Error técnico: " + e.getMessage();
                callback.onError(errorMsg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    Type type = new TypeToken<ApiResponse<LoginResponse>>(){}.getType();
                    ApiResponse<LoginResponse> apiResponse = gson.fromJson(responseBody, type);
                    if (apiResponse.isSuccessful()) {
                        // Guardar token
                        SharedPreferences prefs = context.getSharedPreferences("TaskFlowPrefs", Context.MODE_PRIVATE);
                        prefs.edit().putString("auth_token", apiResponse.getData().getToken()).apply();

                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Error del servidor: " + response.code());
                }
            }
        });
    }

    // Get User Tasks
    public void getUserTasks(int userId, String estado, String proyectoId, String vencimientoBefore, ApiCallback<TareasResponse> callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/usuarios/" + userId + "/tareas").newBuilder();
        if (estado != null && !estado.isEmpty()) {
            urlBuilder.addQueryParameter("estado", estado);
        }
        if (proyectoId != null && !proyectoId.isEmpty()) {
            urlBuilder.addQueryParameter("proyecto_id", proyectoId);
        }
        if (vencimientoBefore != null && !vencimientoBefore.isEmpty()) {
            urlBuilder.addQueryParameter("vencimiento_before", vencimientoBefore);
        }

        Request request = getRequestBuilder()
                .url(urlBuilder.build())
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Error de conexión: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    Type type = new TypeToken<ApiResponse<TareasResponse>>(){}.getType();
                    ApiResponse<TareasResponse> apiResponse = gson.fromJson(responseBody, type);
                    if (apiResponse.isSuccessful()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Error del servidor: " + response.code());
                }
            }
        });
    }

    // Update Task Status
    public void updateTaskStatus(int taskId, String estado, ApiCallback<Tarea> callback) {
        String json = String.format("{\"estado\":\"%s\"}", estado);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = getRequestBuilder()
                .url(BASE_URL + "/tareas/" + taskId + "/estado")
                .patch(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Error de conexión: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    Type type = new TypeToken<ApiResponse<Tarea>>(){}.getType();
                    ApiResponse<Tarea> apiResponse = gson.fromJson(responseBody, type);
                    if (apiResponse.isSuccessful()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Error del servidor: " + response.code());
                }
            }
        });
    }

    // Get Project Stats (Admin only)
    public void getProjectStats(ApiCallback<ProjectStatsResponse> callback) {
        Request request = getRequestBuilder()
                .url(BASE_URL + "/stats/proyectos")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Error de conexión: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    Type type = new TypeToken<ApiResponse<ProjectStatsResponse>>(){}.getType();
                    ApiResponse<ProjectStatsResponse> apiResponse = gson.fromJson(responseBody, type);
                    if (apiResponse.isSuccessful()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Error del servidor: " + response.code());
                }
            }
        });
    }

    public void logout() {
        SharedPreferences prefs = context.getSharedPreferences("TaskFlowPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}

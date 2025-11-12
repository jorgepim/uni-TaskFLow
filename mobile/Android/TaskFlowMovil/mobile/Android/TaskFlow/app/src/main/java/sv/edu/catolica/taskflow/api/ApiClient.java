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
    private static final String BASE_URL_WIFI = "http://192.168.18.31:8000/api"; // IP WiFi del servidor
    private static final String BASE_URL_EMULATOR = "http://10.0.2.2:8000/api";// Para emulador Android Studio
    private static final String BASE_URL_LOCALHOST = "http://127.0.0.1:8000/api"; // Para testing local

    // URL activa - cambiar según tu configuración
    private static final String BASE_URL = BASE_URL_EMULATOR;

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

    /**
     * Crea una nueva tarea en la API.
     * Basado en la prueba de Insomnia "Crear Tarea Uso".
     */
    public void createTask(
            String titulo,
            String descripcion,
            String fechaVencimiento, // Formato "YYYY-MM-DD"
            int proyectoId,
            Integer asignadoA, // Usar Integer (objeto) para permitir 'null'
            ApiCallback<Tarea> callback
    ) {
        // 1. Crear el objeto de datos (mucho más seguro que String.format)
        // Usaremos un Map para que Gson lo serialice
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("titulo", titulo);
        data.put("proyecto_id", proyectoId);

        // Agregar campos opcionales solo si no son nulos
        if (descripcion != null && !descripcion.isEmpty()) {
            data.put("descripcion", descripcion);
        }
        if (fechaVencimiento != null && !fechaVencimiento.isEmpty()) {
            data.put("fecha_vencimiento", fechaVencimiento);
        }
        if (asignadoA != null) {
            data.put("asignado_a", asignadoA);
        }
        // Nota: El 'estado' y 'creado_por' los maneja tu backend.

        // 2. Convertir a JSON usando Gson (ya lo tienes en tu clase)
        String json = gson.toJson(data);
        RequestBody body = RequestBody.create(json, JSON);

        // 3. Construir la petición (POST a /tareas)
        Request request = getRequestBuilder() // Esto ya incluye tu token
                .url(BASE_URL + "/tareas")
                .post(body)
                .build();

        // 4. Ejecutar la llamada
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Error de conexión: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();

                // Tu HAR  indica un 201 Created
                if (response.isSuccessful()) { // Abarca 200, 201, etc.
                    Type type = new TypeToken<ApiResponse<Tarea>>(){}.getType();
                    ApiResponse<Tarea> apiResponse = gson.fromJson(responseBody, type);

                    if (apiResponse.isSuccessful()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    // Manejar errores de validación (422) o otros (403, 500)
                    callback.onError("Error del servidor: " + response.code() + " - " + responseBody);
                }
            }
        });
    }


    public void getProyectos(ApiCallback<List<ProyectoSimple>> callback) {
        Request request = getRequestBuilder()
                // CAMBIA ESTA LÍNEA:
                .url(BASE_URL + "/usuarios/me/proyectos") // Antes era "/proyectos" [cite: 18]
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
                    // Asumimos que la respuesta es ApiResponse<List<ProyectoSimple>>
                    Type type = new TypeToken<ApiResponse<List<ProyectoSimple>>>(){}.getType();
                    ApiResponse<List<ProyectoSimple>> apiResponse = gson.fromJson(responseBody, type);
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

    /**
     * Obtiene la lista de todos los usuarios para el Spinner.
     * Basado en la respuesta de Insomnia: { data: { usuarios: [...] } }
     */
    public void getUsuarios(ApiCallback<List<User>> callback) {
        Request request = getRequestBuilder()
                .url(BASE_URL + "/usuarios")
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
                    // El JSON de /usuarios tiene una estructura anidada
                    try {
                        // Parseo manual para { data: { usuarios: [...] } }
                        com.google.gson.JsonObject jsonObject = gson.fromJson(responseBody, com.google.gson.JsonObject.class);
                        com.google.gson.JsonObject dataObject = jsonObject.getAsJsonObject("data");
                        com.google.gson.JsonArray usuariosArray = dataObject.getAsJsonArray("usuarios");

                        // Convertir el array de JSON a List<User>
                        Type listType = new TypeToken<List<User>>(){}.getType();
                        List<User> usuarios = gson.fromJson(usuariosArray, listType);

                        callback.onSuccess(usuarios);

                    } catch (Exception e) {
                        callback.onError("Error al parsear usuarios: " + e.getMessage());
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

    /**
     * Obtiene los detalles completos de un solo proyecto,
     * incluyendo su lista de tareas.
     * Basado en la prueba de Insomnia "Proyecto Id Uso"
     */
    public void getProjectDetails(int projectId, ApiCallback<Proyecto> callback) {
        Request request = getRequestBuilder()
                .url(BASE_URL + "/proyectos/" + projectId)
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
                    // La respuesta es ApiResponse<Proyecto>
                    Type type = new TypeToken<ApiResponse<Proyecto>>(){}.getType();
                    ApiResponse<Proyecto> apiResponse = gson.fromJson(responseBody, type);

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

    public void getTaskDetails(int taskId, ApiCallback<Tarea> callback) {
        Request request = getRequestBuilder()
                .url(BASE_URL + "/tareas/" + taskId)
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
    /**
     * Actualiza una tarea existente.
     * Basado en la prueba de Insomnia "Editar Tarea Uso"
     */
    public void updateTask(
            int taskId,
            String titulo,
            String descripcion,
            String fechaVencimiento, // Formato "YYYY-MM-DD"
            Integer proyectoId,
            Integer asignadoA,
            ApiCallback<Tarea> callback
    ) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("titulo", titulo);
        data.put("proyecto_id", proyectoId);

        if (descripcion != null) {
            data.put("descripcion", descripcion);
        }
        if (fechaVencimiento != null) {
            data.put("fecha_vencimiento", fechaVencimiento);
        }
        if (asignadoA != null) {
            data.put("asignado_a", asignadoA);
        }

        String json = gson.toJson(data);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = getRequestBuilder()
                .url(BASE_URL + "/tareas/" + taskId)
                .patch(body) // <-- Usamos PATCH para actualizar
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
                    callback.onError("Error del servidor: " + response.code() + " - " + responseBody);
                }
            }
        });
    }

    public void createProyecto(String titulo, String descripcion, ApiCallback<Proyecto> callback) {
        // 1. Crear el objeto de datos
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("titulo", titulo);
        if (descripcion != null && !descripcion.isEmpty()) {
            data.put("descripcion", descripcion);
        }

        // 2. Convertir a JSON
        String json = gson.toJson(data);
        RequestBody body = RequestBody.create(json, JSON);

        // 3. Construir la petición (POST a /proyectos)
        Request request = getRequestBuilder()
                .url(BASE_URL + "/proyectos")
                .post(body)
                .build();

        // 4. Ejecutar la llamada
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Error de conexión: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.isSuccessful()) { // Espera un 201 Created
                    Type type = new TypeToken<ApiResponse<Proyecto>>(){}.getType();
                    ApiResponse<Proyecto> apiResponse = gson.fromJson(responseBody, type);

                    if (apiResponse.isSuccessful()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Error del servidor: " + response.code() + " - " + responseBody);
                }
            }
        });
    }

    public void logout() {
        SharedPreferences prefs = context.getSharedPreferences("TaskFlowPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}

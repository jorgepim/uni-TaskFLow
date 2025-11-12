package sv.edu.catolica.taskflow.api;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonParser; // Importado para el parseo manual
import com.google.gson.JsonArray; // Importado para el parseo manual
import com.google.gson.JsonObject; // Importado para el parseo manual
import okhttp3.*;
import sv.edu.catolica.taskflow.models.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    // Configuraciones de URL para diferentes escenarios
    private static final String BASE_URL_WIFI = "http://192.168.1.25:8080/api";
    private static final String BASE_URL_EMULATOR = "http://10.0.2.2:8000/api";
    private static final String BASE_URL_LOCALHOST = "http://127.0.0.1:8000/api";

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

    // --- Métodos de Autenticación y Tareas Básicas ---

    // Login (conservado)
    public void login(String email, String password, ApiCallback<LoginResponse> callback) {
        String json = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder().url(BASE_URL + "/login").post(body).build();

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

    // Register (conservado)
    public void register(String nombre, String email, String password, ApiCallback<LoginResponse> callback) {
        String json = String.format("{\"nombre\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"activo\":true}",
                nombre, email, password);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder().url(BASE_URL + "/register").post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Error técnico: " + e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    Type type = new TypeToken<ApiResponse<LoginResponse>>(){}.getType();
                    ApiResponse<LoginResponse> apiResponse = gson.fromJson(responseBody, type);
                    if (apiResponse.isSuccessful()) {
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

    // Get User Tasks (conservado)
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

        Request request = getRequestBuilder().url(urlBuilder.build()).get().build();

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

    // Update Task Status (conservado)
    public void updateTaskStatus(int taskId, String estado, ApiCallback<Tarea> callback) {
        String json = String.format("{\"estado\":\"%s\"}", estado);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = getRequestBuilder().url(BASE_URL + "/tareas/" + taskId + "/estado").patch(body).build();

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

    // Create Task (conservado)
    public void createTask(
            String titulo,
            String descripcion,
            String fechaVencimiento,
            int proyectoId,
            Integer asignadoA,
            ApiCallback<Tarea> callback
    ) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("titulo", titulo);
        data.put("proyecto_id", proyectoId);

        if (descripcion != null && !descripcion.isEmpty()) {
            data.put("descripcion", descripcion);
        }
        if (fechaVencimiento != null && !fechaVencimiento.isEmpty()) {
            data.put("fecha_vencimiento", fechaVencimiento);
        }
        if (asignadoA != null) {
            data.put("asignado_a", asignadoA);
        }

        String json = gson.toJson(data);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = getRequestBuilder().url(BASE_URL + "/tareas").post(body).build();

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

    // --- Métodos de Proyectos y Usuarios ---

    // Get Projects (para Spinner, usa /usuarios/me/proyectos)
    public void getProyectos(ApiCallback<List<ProyectoSimple>> callback) {
        Request request = getRequestBuilder()
                .url(BASE_URL + "/usuarios/me/proyectos")
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

    // Get All Users (para filtrar Admins)
    public void getUsuarios(ApiCallback<List<User>> callback) {
        Request request = getRequestBuilder().url(BASE_URL + "/usuarios").get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Error de conexión: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    // Parseo manual para la estructura anidada: { data: { usuarios: [...] } }
                    try {
                        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                        JsonObject dataObject = jsonObject.getAsJsonObject("data");
                        JsonArray usuariosArray = dataObject.getAsJsonArray("usuarios");

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

    // Get Project Members (para asignar tareas, usa /proyectos/{id}/usuarios)
    public void getUsuariosDelProyecto(int proyectoId, ApiCallback<List<User>> callback) {
        Request request = getRequestBuilder()
                .url(BASE_URL + "/proyectos/" + proyectoId + "/usuarios")
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
                    Type type = new TypeToken<ApiResponse<List<User>>>(){}.getType();
                    ApiResponse<List<User>> apiResponse = gson.fromJson(responseBody, type);

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

    // Get Not Assigned Users (para asignar miembros al proyecto, usa /proyectos/{id}/usuarios/no-asignados)
    public void getUsuariosNoAsignados(int proyectoId, ApiCallback<List<User>> callback) {
        Request request = getRequestBuilder()
                .url(BASE_URL + "/proyectos/" + proyectoId + "/usuarios/no-asignados")
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
                    Type type = new TypeToken<ApiResponse<List<User>>>(){}.getType();
                    ApiResponse<List<User>> apiResponse = gson.fromJson(responseBody, type);

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

    // Get Project Details (para la vista principal)
    public void getProjectDetails(int projectId, ApiCallback<Proyecto> callback) {
        Request request = getRequestBuilder().url(BASE_URL + "/proyectos/" + projectId).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Error de conexión: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
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

    // Update Full Task (para la vista de edición)
    public void updateTask(
            int taskId,
            String titulo,
            String descripcion,
            String fechaVencimiento,
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

        Request request = getRequestBuilder().url(BASE_URL + "/tareas/" + taskId).patch(body).build();

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

    // Create Project (para la vista de crear proyecto)
    public void createProyecto(String titulo, String descripcion, ApiCallback<Proyecto> callback) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("titulo", titulo);
        if (descripcion != null && !descripcion.isEmpty()) {
            data.put("descripcion", descripcion);
        }

        String json = gson.toJson(data);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = getRequestBuilder().url(BASE_URL + "/proyectos").post(body).build();

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

    // Assign User to Project (para la gestión de miembros)
    public void asignarUsuarioAProyecto(int proyectoId, int usuarioId, String rol, ApiCallback<User> callback) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("usuario_id", usuarioId);
        data.put("rol_proyecto", rol); // "COLABORADOR"

        String json = gson.toJson(data);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = getRequestBuilder()
                .url(BASE_URL + "/proyectos/" + proyectoId + "/usuarios")
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
                    Type type = new TypeToken<ApiResponse<User>>(){}.getType();
                    ApiResponse<User> apiResponse = gson.fromJson(responseBody, type);

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

    // Get Project Stats (conservado)
    public void getProjectStats(ApiCallback<ProjectStatsResponse> callback) {
        Request request = getRequestBuilder().url(BASE_URL + "/stats/proyectos").get().build();

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

    // Logout
    public void logout() {
        SharedPreferences prefs = context.getSharedPreferences("TaskFlowPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
# TaskFlow - Aplicación Android

Una aplicación Android para la gestión de tareas que conecta con la API de TaskFlow.

## Características

### Funcionalidades Principales
- **Autenticación**: Login y registro de usuarios
- **Gestión de Tareas**: Ver, filtrar y actualizar el estado de las tareas asignadas
- **Roles de Usuario**: Soporte para roles USER y ADMIN
- **Estadísticas (Solo Admin)**: Vista de estadísticas de proyectos y tareas

### Endpoints Implementados
- `POST /api/login` - Inicio de sesión
- `POST /api/register` - Registro de usuarios
- `GET /api/usuarios/{id}/tareas` - Obtener tareas del usuario con filtros
- `PATCH /api/tareas/{id}/estado` - Actualizar estado de tarea
- `GET /api/stats/proyectos` - Estadísticas de proyectos (solo admin)

## Estructura del Proyecto

```
app/src/main/java/sv/edu/catolica/taskflow/
├── adapters/
│   ├── TareasAdapter.java          # Adaptador para lista de tareas
│   └── ProyectosStatsAdapter.java  # Adaptador para estadísticas de proyectos
├── api/
│   └── ApiClient.java              # Cliente HTTP para comunicación con API
├── models/
│   ├── User.java                   # Modelo de usuario
│   ├── Tarea.java                  # Modelo de tarea
│   ├── Proyecto.java               # Modelo de proyecto
│   ├── ApiResponse.java            # Respuesta genérica de API
│   └── ...                        # Otros modelos
├── utils/
│   └── SessionManager.java         # Gestión de sesión de usuario
├── LoginActivity.java              # Pantalla de inicio de sesión
├── RegisterActivity.java           # Pantalla de registro
├── MainActivity.java               # Pantalla principal con lista de tareas
├── StatsActivity.java             # Pantalla de estadísticas (admin)
└── ProfileActivity.java           # Pantalla de perfil de usuario
```

## Configuración

### Requisitos
- Android SDK 33+
- Java 11+
- API de TaskFlow ejecutándose en `http://127.0.0.1:8000`

### Instalación
1. Clona el repositorio
2. Abre el proyecto en Android Studio
3. Sincroniza las dependencias con Gradle
4. Asegúrate de que la API esté ejecutándose
5. Ejecuta la aplicación en un emulador o dispositivo físico

### Configuración de Red

⚠️ **IMPORTANTE**: Para que la aplicación funcione correctamente, necesitas configurar tanto el servidor como la aplicación.

#### 1. Configurar el Servidor Laravel
Tu servidor debe aceptar conexiones desde la red. Ejecuta:

```bash
# Detén el servidor actual si está corriendo
php artisan serve --host=192.168.0.76 --port=8000
```

O para escuchar en todas las interfaces:
```bash
php artisan serve --host=0.0.0.0 --port=8000
```

#### 2. Configurar la Aplicación Android
En `ApiClient.java` hay tres URLs preconfiguradas:

```java
private static final String BASE_URL_WIFI = "http://192.168.0.76:8000/api"; // IP WiFi
private static final String BASE_URL_EMULATOR = "http://10.0.2.2:8000/api"; // Emulador
private static final String BASE_URL_LOCALHOST = "http://127.0.0.1:8000/api"; // Local

// Cambia esta línea según tu caso:
private static final String BASE_URL = BASE_URL_WIFI; // ← Configuración activa
```

#### 3. Verificar Conectividad
Antes de usar la app, abre el navegador en tu dispositivo Android y ve a:
`http://192.168.0.76:8000`

Si no puedes acceder, revisa el archivo `CONFIGURACION_SERVIDOR.md` para troubleshooting detallado.

## Uso de la Aplicación

### 1. Registro/Login
- Al abrir la aplicación, verás la pantalla de login
- Si no tienes cuenta, toca "¿No tienes cuenta? Regístrate"
- Completa el formulario de registro con tu información
- Una vez registrado, serás redirigido automáticamente a la pantalla principal

### 2. Vista Principal (Tareas)
- Muestra todas las tareas asignadas al usuario actual
- Permite filtrar por estado: Todos, PENDIENTE, PROGRESO, COMPLETADA
- Cada tarea muestra:
  - Título y descripción
  - Estado actual (con código de colores)
  - Usuario asignado
  - Fecha de vencimiento (si aplica)
  - Botones para cambiar estado (si no está completada)

### 3. Cambio de Estado de Tareas
Los usuarios pueden actualizar el estado de sus tareas:
- **PENDIENTE** → **PROGRESO** o **COMPLETADA**
- **PROGRESO** → **COMPLETADA**

### 4. Menú de Navegación
Accesible desde el botón hamburguesa:
- **Mis Tareas**: Vista principal de tareas
- **Estadísticas**: Solo visible para administradores
- **Perfil**: Información del usuario actual
- **Cerrar Sesión**: Cierra la sesión y regresa al login

### 5. Estadísticas (Solo Admin)
Los usuarios con rol ADMIN pueden ver:
- Resumen general: total de proyectos, tareas y porcentaje completado
- Distribución de tareas por estado
- Lista de proyectos ordenados por desempeño

## Dependencias Principales

- **OkHttp**: Cliente HTTP para comunicación con API
- **Gson**: Serialización/deserialización JSON
- **Material Design**: Componentes de UI modernos
- **SwipeRefreshLayout**: Actualización por deslizamiento
- **RecyclerView**: Listas eficientes

## Estados de Tarea

- 🟠 **PENDIENTE**: Tarea creada pero no iniciada
- 🔵 **PROGRESO**: Tarea en desarrollo
- 🟢 **COMPLETADA**: Tarea finalizada

## Roles de Usuario

- **USER**: Puede ver y actualizar sus tareas asignadas
- **ADMIN**: Todas las funciones de USER + acceso a estadísticas globales

## Características Técnicas

- **Arquitectura**: Patrón MVC simplificado
- **Almacenamiento Local**: SharedPreferences para sesión
- **Red**: Comunicación asíncrona con callbacks
- **UI**: Material Design con CardViews y RecyclerViews
- **Navegación**: Navigation Drawer con múltiples destinos

## Troubleshooting

### Error de Conexión ("Failed to connect")
1. **Verifica el servidor Laravel**:
   ```bash
   php artisan serve --host=192.168.0.76 --port=8000
   ```

2. **Verifica la IP de tu WiFi**:
   ```bash
   ipconfig
   ```
   Busca tu adaptador inalámbrico y confirma la IP.

3. **Prueba desde el navegador del móvil**:
   Ve a `http://192.168.0.76:8000` en el navegador de tu dispositivo.

4. **Verifica el firewall de Windows**:
   Asegúrate de que no esté bloqueando el puerto 8000.

5. **Si usas emulador de Android Studio**:
   Cambia `BASE_URL` a `BASE_URL_EMULATOR` en `ApiClient.java`

### Errores de Autenticación
- Verifica que las credenciales sean correctas
- El token se guarda automáticamente tras login exitoso
- Si persiste, cierra sesión y vuelve a iniciar

### Tareas No Se Cargan
- Verifica que el usuario tenga tareas asignadas en la API
- Usa "Deslizar para actualizar" en la lista de tareas
- Revisa los logs de Android Studio para errores específicos

## Próximas Mejoras

- [ ] Notificaciones push
- [ ] Modo offline con sincronización
- [ ] Filtros avanzados de fecha
- [ ] Creación de nuevas tareas
- [ ] Chat en tareas
- [ ] Configuración de perfil
- [ ] Tema oscuro

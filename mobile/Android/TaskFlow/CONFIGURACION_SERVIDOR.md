# Configuración del Servidor Laravel para Conexiones de Red

## Problema
La aplicación Android no puede conectarse al servidor Laravel porque por defecto Laravel solo acepta conexiones desde localhost (127.0.0.1).

## Solución

### Paso 1: Detener el servidor actual
Si tienes el servidor corriendo, deténlo con `Ctrl+C`.

### Paso 2: Iniciar el servidor para aceptar conexiones externas

#### Opción A: Escuchar en todas las interfaces
```bash
php artisan serve --host=0.0.0.0 --port=8000
```

#### Opción B: Escuchar específicamente en tu IP WiFi
```bash
php artisan serve --host=192.168.0.76 --port=8000
```

### Paso 3: Verificar que el servidor esté corriendo
Deberías ver algo como:
```
INFO  Server running on [http://192.168.0.76:8000].
```

### Paso 4: Probar desde el navegador
Abre tu navegador y ve a: `http://192.168.0.76:8000`
Deberías ver tu aplicación Laravel.

### Paso 5: Probar la API desde el navegador
Ve a: `http://192.168.0.76:8000/api/usuarios`
Deberías ver una respuesta JSON (aunque puede requerir autenticación).

## Verificaciones Adicionales

### 1. Firewall de Windows
Asegúrate de que el firewall de Windows no esté bloqueando el puerto 8000:
- Ve a "Configuración de Windows" > "Red e Internet" > "Firewall de Windows Defender"
- Haz clic en "Configuración avanzada"
- En "Reglas de entrada", busca si hay alguna regla para PHP o el puerto 8000
- Si no existe, crea una nueva regla para permitir el puerto 8000

### 2. Verificar la IP de tu WiFi
Para confirmar tu IP de WiFi:
```bash
ipconfig
```
Busca tu adaptador de red inalámbrica y confirma que sea 192.168.0.76.

### 3. Probar conectividad desde Android
Antes de probar la app, puedes usar el navegador del dispositivo Android para ir a:
`http://192.168.0.76:8000`

## Comandos de Troubleshooting

### Ver qué procesos están usando el puerto 8000:
```bash
netstat -ano | findstr :8000
```

### Matar un proceso que esté usando el puerto:
```bash
taskkill /PID <PID_NUMBER> /F
```

## Si Sigue Sin Funcionar

### Alternativa 1: Usar ngrok (Túnel público)
```bash
ngrok http 8000
```
Esto te dará una URL pública que puedes usar en lugar de la IP local.

### Alternativa 2: Verificar configuración de CORS
En tu archivo `config/cors.php` de Laravel, asegúrate de que esté configurado para permitir conexiones desde Android:

```php
'allowed_origins' => ['*'],
'allowed_methods' => ['*'],
'allowed_headers' => ['*'],
```

### Alternativa 3: Usar IP 10.0.2.2 para emulador
Si estás usando el emulador de Android Studio, cambia la IP en ApiClient.java a:
```java
private static final String BASE_URL = "http://10.0.2.2:8000/api";
```

## Nota Importante
Recuerda que cada vez que reinicies tu router o tu computadora, la IP podría cambiar. Si eso pasa, necesitarás actualizar la IP en el ApiClient.java.

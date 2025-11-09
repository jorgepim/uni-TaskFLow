<?php // app/Exceptions/Handler.php
namespace App\Exceptions;

use App\Http\Traits\ApiResponseTrait; // Importa el Trait
use Illuminate\Foundation\Exceptions\Handler as ExceptionHandler;
use Illuminate\Validation\ValidationException; // Para 400
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException; // Para 404
use Illuminate\Auth\AuthenticationException;
use Symfony\Component\Routing\Exception\RouteNotFoundException;
use Throwable;

class Handler extends ExceptionHandler
{
  use ApiResponseTrait; // Usa el Trait

  // ... (otras propiedades)

  public function register(): void
  {

    // Manejar excepciones de autenticación para API (devolver 401 en vez de intentar redirect)
    $this->renderable(function (AuthenticationException $e, $request) {
      if ($request->is('api/*')) {
        return $this->errorResponse('No autenticado.', 401);
      }
    });

    // Cuando la middleware intenta redirigir a una ruta 'login' inexistente, se lanza
    // RouteNotFoundException; interceptamos para devolver 401 en peticiones API.
    $this->renderable(function (RouteNotFoundException $e, $request) {
      if ($request->is('api/*') && str_contains($e->getMessage(), 'login')) {
        return $this->errorResponse('No autenticado.', 401);
      }
    });

    // 1. Maneja Errores de Validación (400)
    $this->renderable(function (ValidationException $e, $request) {
      if ($request->is('api/*')) { // Solo si es una petición de API
        $errors = $e->validator->errors()->all();
        return $this->errorResponse(
          'Error de validación: ' . implode(', ', $errors),
          400 // Bad Request
        );
      }
    });

    // 2. Maneja Errores "No Encontrado" (404)
    $this->renderable(function (NotFoundHttpException $e, $request) {
      if ($request->is('api/*')) {
        return $this->errorResponse(
          'Recurso no encontrado.',
          404 // Not Found
        );
      }
    });

    // 3. Maneja Errores Generales (500)
    $this->renderable(function (Throwable $e, $request) {
      if ($request->is('api/*')) {
        // (No mostrar $e->getMessage() en producción por seguridad)
        return $this->errorResponse(
          'Error interno del servidor.',
          500 // Internal Server Error
        );
      }
    });
  }
}

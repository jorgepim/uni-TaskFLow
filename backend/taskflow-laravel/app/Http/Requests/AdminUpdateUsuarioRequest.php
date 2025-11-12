<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use App\Models\Usuario;

/**
 * Mueve validaciones específicas de negocio aquí:
 * - Si se solicita rol=ADMIN, se comprueba que el usuario objetivo no tenga
 *   tareas abiertas y que no sea CREADOR en la tabla pivot usuario_proyecto.
 *
 * En caso de que no cumpla, se marcará en los atributos de la request
 * para que el controlador omita el cambio de rol pero aplique los demás
 * campos.
 */

class AdminUpdateUsuarioRequest extends FormRequest
{
  public function authorize(): bool
  {
    // Authorization will be handled in the controller (only ADMIN)
    return true;
  }

  public function rules(): array
  {
    $usuarioId = $this->route('usuario') ? $this->route('usuario')->id : null;

    return [
      'nombre' => 'sometimes|required|string|max:150',
      'email' => 'sometimes|required|email|unique:usuarios,email,' . $usuarioId,
      'password' => 'nullable|string|min:6',
      'activo' => 'sometimes|boolean',
      'rol' => 'sometimes|string|exists:roles,nombre',
    ];
  }

  protected function passedValidation()
  {
    // Si no se solicitó cambio de rol, no hay nada que hacer
    $requestedRole = $this->input('rol');
    if (! $requestedRole) {
      return;
    }

    // Solo aplicamos la regla especial cuando se pide ADMIN
    if ($requestedRole === 'ADMIN') {
      $usuario = $this->route('usuario');
      if (! $usuario || ! ($usuario instanceof Usuario)) {
        return;
      }

      $hasOpenTasks = $usuario->tareasAsignadas()->where('estado', '!=', 'COMPLETADA')->exists();
      $isCreador = $usuario->proyectos()->wherePivot('rol_proyecto', 'CREADOR')->exists();

      if ($hasOpenTasks || $isCreador) {
        // Indicamos al controlador que debe omitir el cambio de rol pero
        // dejar pasar la petición para que se apliquen los demás cambios.
        $this->attributes->set('roleChangeSkipped', true);
        // También limpiamos el campo rol para que validated() devuelva null/omitido
        $this->merge(['rol' => null]);
      }
    }
  }
}

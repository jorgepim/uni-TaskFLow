<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

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
}

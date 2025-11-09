<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class UpdateUsuarioRequest extends FormRequest
{
  public function authorize(): bool
  {
    return true;
  }

  public function rules(): array
  {
    $usuarioId = $this->route('usuario')?->id ?? null;

    return [
      'nombre' => 'sometimes|required|string|max:100',
      'email' => [
        'sometimes',
        'required',
        'email',
        'max:120',
        Rule::unique('usuarios', 'email')->ignore($usuarioId),
      ],
      'password' => 'nullable|string|min:6',
      'activo' => 'sometimes|boolean',
    ];
  }
}

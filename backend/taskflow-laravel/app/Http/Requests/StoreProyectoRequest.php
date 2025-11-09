<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class StoreProyectoRequest extends FormRequest
{
  public function authorize(): bool
  {
    return true;
  }

  public function rules(): array
  {
    return [
      'titulo' => 'required|string|max:150',
      'descripcion' => 'nullable|string',
      // 'creado_por' se asigna desde el token del usuario autenticado; no es obligatorio en el payload
      'creado_por' => 'sometimes|integer|exists:usuarios,id',
    ];
  }
}

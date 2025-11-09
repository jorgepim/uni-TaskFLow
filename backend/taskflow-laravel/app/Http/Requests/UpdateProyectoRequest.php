<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpdateProyectoRequest extends FormRequest
{
  public function authorize(): bool
  {
    return true;
  }

  public function rules(): array
  {
    return [
      'titulo' => 'sometimes|required|string|max:150',
      'descripcion' => 'nullable|string',
      'creado_por' => 'sometimes|required|integer|exists:usuarios,id',
    ];
  }
}

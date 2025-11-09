<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpdateComentarioRequest extends FormRequest
{
  public function authorize(): bool
  {
    return true;
  }

  public function rules(): array
  {
    return [
      'texto' => 'sometimes|required|string',
      'fecha' => 'nullable|date',
      'tarea_id' => 'sometimes|required|integer|exists:tareas,id',
      'usuario_id' => 'sometimes|required|integer|exists:usuarios,id',
    ];
  }
}

<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class StoreComentarioRequest extends FormRequest
{
  public function authorize(): bool
  {
    return true;
  }

  public function rules(): array
  {
    return [
      'texto' => 'required|string',
      'fecha' => 'nullable|date',
      'tarea_id' => 'required|integer|exists:tareas,id',
      'usuario_id' => 'required|integer|exists:usuarios,id',
    ];
  }
}

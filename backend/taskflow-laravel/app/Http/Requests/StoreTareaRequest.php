<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class StoreTareaRequest extends FormRequest
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
      'fecha_vencimiento' => 'nullable|date',
      'estado' => 'nullable|in:PENDIENTE,PROGRESO,COMPLETADA',
      'proyecto_id' => 'required|integer|exists:proyectos,id',
      'asignado_a' => 'nullable|integer|exists:usuarios,id',
      'creado_por' => 'required|integer|exists:usuarios,id',
    ];
  }
}

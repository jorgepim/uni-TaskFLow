<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpdateTareaRequest extends FormRequest
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
      // fecha_vencimiento debe ser hoy o en el futuro (no se permiten fechas pasadas)
      'fecha_vencimiento' => 'nullable|date|after_or_equal:today',
      'estado' => 'nullable|in:PENDIENTE,PROGRESO,COMPLETADA',
      'proyecto_id' => 'sometimes|required|integer|exists:proyectos,id',
      'asignado_a' => 'nullable|integer|exists:usuarios,id',
    ];
  }

  public function messages(): array
  {
    return [
      'fecha_vencimiento.date' => 'fecha_vencimiento inválida, usar YYYY-MM-DD',
      'fecha_vencimiento.after_or_equal' => 'fecha_vencimiento no puede ser anterior a hoy',
    ];
  }
}

<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class AssignUsuarioProyectoRequest extends FormRequest
{
  public function authorize()
  {
    // La autorización se comprobará en el controlador (necesitamos acceso al proyecto)
    return true;
  }

  public function rules()
  {
    return [
      'usuario_id' => ['required', 'integer', 'exists:usuarios,id'],
      'rol_proyecto' => ['nullable', 'string', 'in:CREADOR,COLABORADOR'],
    ];
  }
}

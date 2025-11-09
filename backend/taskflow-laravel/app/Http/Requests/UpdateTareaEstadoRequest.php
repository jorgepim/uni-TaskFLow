<?php

namespace App\Http\Requests;

use App\Models\Tarea;
use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class UpdateTareaEstadoRequest extends FormRequest
{
  public function authorize()
  {
    // La autorización la maneja el controlador (necesitamos el usuario y la tarea)
    return true;
  }

  public function rules()
  {
    return [
      'estado' => ['required', 'string', Rule::in(Tarea::ESTADOS)],
    ];
  }
}

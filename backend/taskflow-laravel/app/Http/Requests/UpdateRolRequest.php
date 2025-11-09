<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class UpdateRolRequest extends FormRequest
{
  public function authorize(): bool
  {
    return true;
  }

  public function rules(): array
  {
    $rolId = $this->route('rol')?->id ?? null;

    return [
      'nombre' => [
        'sometimes',
        'required',
        'string',
        'max:50',
        Rule::unique('roles', 'nombre')->ignore($rolId),
      ],
    ];
  }
}

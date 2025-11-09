<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class StoreUsuarioRequest extends FormRequest
{
  public function authorize(): bool
  {
    return true;
  }

  public function rules(): array
  {
    return [
      'nombre' => 'required|string|max:100',
      'email' => 'required|email|max:120|unique:usuarios,email',
      'password' => 'required|string|min:6',
      'activo' => 'sometimes|boolean',
    ];
  }
}

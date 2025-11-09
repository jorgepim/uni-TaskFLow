<?php

namespace App\Http\Resources;

use Illuminate\Http\Resources\Json\JsonResource;

class UsuarioResource extends JsonResource
{
  public function toArray($request)
  {
    return [
      'id' => $this->id,
      'nombre' => $this->nombre,
      'email' => $this->email,
      'activo' => (bool) $this->activo,
      'fecha_creacion' => $this->fecha_creacion,
      // Incluir roles si la relación fue carregada (por ejemplo en el login)
      'roles' => $this->whenLoaded('roles', function () {
        return $this->roles->map(function ($r) {
          return [
            'id' => $r->id,
            'nombre' => $r->nombre,
          ];
        })->values();
      }),
    ];
  }
}

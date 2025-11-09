<?php

namespace App\Http\Resources;

use Illuminate\Http\Resources\Json\JsonResource;

class ProyectoResource extends JsonResource
{
  public function toArray($request)
  {
    return [
      'id' => $this->id,
      'titulo' => $this->titulo,
      'descripcion' => $this->descripcion,
      'fecha_creacion' => $this->fecha_creacion,
      'creado_por' => $this->creado_por,
      // Incluir tareas asociadas (solo campos requeridos) cuando estén cargadas
      'tareas' => $this->whenLoaded('tareas', function () {
        return $this->tareas->map(function ($t) {
          return [
            'id' => $t->id,
            'titulo' => $t->titulo,
            'fecha_vencimiento' => $t->fecha_vencimiento,
            'estado' => $t->estado,
            'asignado' => $t->relationLoaded('asignadoA') && $t->asignadoA ? [
              'id' => $t->asignadoA->id,
              'nombre' => $t->asignadoA->nombre,
            ] : null,
          ];
        })->values();
      }),
    ];
  }
}

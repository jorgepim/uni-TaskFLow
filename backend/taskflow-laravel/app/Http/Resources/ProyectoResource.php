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
      // Devolver creador como objeto si la relación fue cargada, si no devolver el id
      'creado_por' => $this->whenLoaded('creadoPor', function () {
        return [
          'id' => $this->creadoPor->id,
          'nombre' => $this->creadoPor->nombre,
        ];
      }, $this->creado_por),
      // Rol del usuario dentro de este proyecto (desde el pivot usuario_proyecto)
      'rol_proyecto' => isset($this->pivot) && isset($this->pivot->rol_proyecto) ? $this->pivot->rol_proyecto : null,
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

<?php

namespace App\Http\Resources;

use Illuminate\Http\Resources\Json\JsonResource;

class TareaResource extends JsonResource
{
  public function toArray($request)
  {
    return [
      'id' => $this->id,
      'titulo' => $this->titulo,
      'descripcion' => $this->descripcion,
      'fecha_vencimiento' => $this->fecha_vencimiento,
      'estado' => $this->estado,
      'proyecto_id' => $this->proyecto_id,
      // Incluimos información del usuario asignado (id + nombre) cuando la relación está cargada
      'asignado' => $this->whenLoaded('asignadoA', function () {
        if (! $this->asignadoA) return null;
        return [
          'id' => $this->asignadoA->id,
          'nombre' => $this->asignadoA->nombre,
        ];
      }),

      // Mostrar creador como objeto {id, nombre} cuando la relación esté cargada
      'creado_por' => $this->whenLoaded('creador', function () {
        if (! $this->creador) return null;
        return [
          'id' => $this->creador->id,
          'nombre' => $this->creador->nombre,
        ];
      }),
      'fecha_creacion' => $this->fecha_creacion,
      // Incluir comentarios con usuario (id + nombre)
      'comentarios' => $this->whenLoaded('comentarios', function () {
        return $this->comentarios->map(function ($c) {
          return [
            'id' => $c->id,
            'texto' => $c->texto,
            'fecha' => $c->fecha,
            'usuario' => $c->relationLoaded('usuario') && $c->usuario ? [
              'id' => $c->usuario->id,
              'nombre' => $c->usuario->nombre,
            ] : null,
          ];
        })->values();
      }),
    ];
  }
}

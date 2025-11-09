<?php

namespace App\Http\Resources;

use Illuminate\Http\Resources\Json\JsonResource;

class ComentarioResource extends JsonResource
{
  public function toArray($request)
  {
    return [
      'id' => $this->id,
      'texto' => $this->texto,
      'fecha' => $this->fecha,
      'tarea_id' => $this->tarea_id,
      'usuario_id' => $this->usuario_id,
    ];
  }
}

<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Relations\Pivot;

class UsuarioProyecto extends Pivot
{
  protected $table = 'usuario_proyecto';

  public $incrementing = false;

  public $timestamps = false;

  protected $fillable = [
    'usuario_id',
    'proyecto_id',
    'rol_proyecto',
  ];
}

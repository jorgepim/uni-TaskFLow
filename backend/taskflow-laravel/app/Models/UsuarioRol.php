<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Relations\Pivot;

class UsuarioRol extends Pivot
{
  protected $table = 'usuario_rol';

  public $incrementing = false;

  public $timestamps = false;

  protected $fillable = [
    'usuario_id',
    'rol_id',
  ];
}

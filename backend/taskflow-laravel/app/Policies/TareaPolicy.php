<?php

namespace App\Policies;

use App\Models\Usuario;
use App\Models\Tarea;

class TareaPolicy
{
  public function update(Usuario $user, Tarea $tarea)
  {
    // Creador de la tarea
    if ($tarea->creado_por == $user->id) {
      return true;
    }

    // Usuario asignado puede actualizar (por ejemplo cambiar estado)
    if ($tarea->asignado_a == $user->id) {
      return true;
    }

    // Rol global ADMIN
    if ($user->roles->pluck('nombre')->contains('ADMIN')) {
      return true;
    }

    return false;
  }

  public function delete(Usuario $user, Tarea $tarea)
  {
    // Solo creador o admin
    return $tarea->creado_por == $user->id || $user->roles->pluck('nombre')->contains('ADMIN');
  }
}

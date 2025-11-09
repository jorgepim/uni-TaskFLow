<?php

namespace App\Policies;

use App\Models\Usuario;
use App\Models\Proyecto;

class ProyectoPolicy
{
  public function update(Usuario $user, Proyecto $proyecto)
  {
    // Creador del proyecto
    if ($proyecto->creado_por == $user->id) {
      return true;
    }

    // Rol global ADMIN
    if ($user->roles->pluck('nombre')->contains('ADMIN')) {
      return true;
    }

    // Rol en el pivot (CREADOR)
    $pivot = $proyecto->usuarios()->where('usuarios.id', $user->id)->first();
    if ($pivot && ($pivot->pivot->rol_proyecto === 'CREADOR')) {
      return true;
    }

    return false;
  }

  public function delete(Usuario $user, Proyecto $proyecto)
  {
    return $this->update($user, $proyecto);
  }
}

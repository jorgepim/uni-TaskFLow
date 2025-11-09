<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use App\Models\Proyecto;

class EnsureProjectRole
{
  /**
   * Handle an incoming request.
   * $requiredRoles: comma separated list like 'CREADOR,COLABORADOR'
   */
  public function handle(Request $request, Closure $next, string $requiredRoles)
  {
    $user = $request->user();

    if (! $user) {
      return response()->json(['status' => 'error', 'message' => 'No autenticado'], 401);
    }

    $project = $request->route('proyecto');
    if (! $project instanceof Proyecto) {
      return response()->json(['status' => 'error', 'message' => 'Proyecto no encontrado'], 404);
    }

    $expected = array_map('trim', explode(',', $requiredRoles));

    $pivotUser = $project->usuarios()->where('usuarios.id', $user->id)->first();

    if (! $pivotUser) {
      return response()->json(['status' => 'error', 'message' => 'No participante del proyecto'], 403);
    }

    $rolProyecto = $pivotUser->pivot->rol_proyecto ?? null;

    if (in_array($rolProyecto, $expected)) {
      return $next($request);
    }

    return response()->json(['status' => 'error', 'message' => 'No autorizado en este proyecto'], 403);
  }
}

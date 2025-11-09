<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

class EnsureRole
{
  /**
   * Handle an incoming request.
   * $roles: comma separated list like 'ADMIN,MODERATOR'
   */
  public function handle(Request $request, Closure $next, string $roles)
  {
    $user = $request->user();

    if (! $user) {
      return response()->json(['status' => 'error', 'message' => 'No autenticado'], 401);
    }

    $expected = array_map('trim', explode(',', $roles));

    $userRoles = $user->roles->pluck('nombre')->all();

    foreach ($expected as $r) {
      if (in_array($r, $userRoles)) {
        return $next($request);
      }
    }

    return response()->json(['status' => 'error', 'message' => 'Acceso no autorizado'], 403);
  }
}

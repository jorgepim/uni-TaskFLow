<?php

namespace App\Http\Controllers;

use App\Http\Traits\ApiResponseTrait;
use App\Models\Proyecto;
use App\Models\Tarea;
use App\Models\Usuario;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class StatsController extends Controller
{
  use ApiResponseTrait;

  /**
   * Estadísticas y datos para gráficos sobre proyectos.
   * Acceso: ADMIN only.
   */
  public function proyectos(Request $request)
  {
    $actor = $request->user();
    if (! $actor || ! method_exists($actor, 'hasRole') || ! $actor->hasRole('ADMIN')) {
      return $this->errorResponse('No autorizado', 403);
    }

    // Totales generales
    $totalProjects = Proyecto::count();
    $totalTasks = Tarea::count();

    $tasksByEstado = Tarea::select('estado', DB::raw('count(*) as count'))
      ->groupBy('estado')
      ->get()
      ->pluck('count', 'estado')
      ->toArray();

    $completed = $tasksByEstado['COMPLETADA'] ?? 0;
    $percentCompletedOverall = $totalTasks > 0 ? round($completed / $totalTasks * 100, 2) : 0;

    // Projects detailed: for charts (top projects by completion, tasks per project)
    $projects = Proyecto::withCount(['tareas as total_tareas', 'usuarios as assigned_users_count'])->get();

    $projectsData = $projects->map(function ($p) {
      $counts = Tarea::where('proyecto_id', $p->id)
        ->select('estado', DB::raw('count(*) as c'))
        ->groupBy('estado')
        ->pluck('c', 'estado')
        ->toArray();

      $total = $p->total_tareas ?? 0;
      $completed = $counts['COMPLETADA'] ?? 0;
      $percent = $total > 0 ? round($completed / $total * 100, 2) : 0;

      return [
        'id' => $p->id,
        'titulo' => $p->titulo,
        'total_tareas' => $total,
        'tasks_by_estado' => $counts,
        'percent_completed' => $percent,
        'assigned_users_count' => $p->assigned_users_count ?? 0,
        'fecha_creacion' => $p->fecha_creacion,
      ];
    })->values();

    // Top 5 proyectos por porcentaje completado (con al menos 1 tarea)
    $topProjects = $projectsData->filter(function ($p) {
      return $p['total_tareas'] > 0;
    })->sortByDesc('percent_completed')->take(5)->values();

    // Tasks per month (últimos 6 meses)
    $tasksPerMonth = Tarea::select(DB::raw("DATE_FORMAT(fecha_creacion, '%Y-%m') as ym"), DB::raw('count(*) as c'))
      ->where('fecha_creacion', '>=', now()->subMonths(6))
      ->groupBy('ym')
      ->orderBy('ym')
      ->get()
      ->mapWithKeys(function ($row) {
        return [$row->ym => (int) $row->c];
      });

    return $this->successResponse('Estadísticas de proyectos', [
      'total_projects' => $totalProjects,
      'total_tasks' => $totalTasks,
      'tasks_by_estado' => $tasksByEstado,
      'percent_completed_overall' => $percentCompletedOverall,
      'projects' => $projectsData,
      'top_projects_by_completion' => $topProjects,
      'tasks_per_month' => $tasksPerMonth,
    ]);
  }

  /**
   * Estadísticas y datos para gráficos sobre tareas y rendimiento por usuario.
   * Acceso: ADMIN only.
   */
  public function tareas(Request $request)
  {
    $actor = $request->user();
    if (! $actor || ! method_exists($actor, 'hasRole') || ! $actor->hasRole('ADMIN')) {
      return $this->errorResponse('No autorizado', 403);
    }

    $totalTasks = Tarea::count();
    $tasksByEstado = Tarea::select('estado', DB::raw('count(*) as count'))
      ->groupBy('estado')
      ->get()
      ->pluck('count', 'estado')
      ->toArray();

    $overdue = Tarea::whereNotNull('fecha_vencimiento')
      ->where('fecha_vencimiento', '<', now()->toDateString())
      ->where('estado', '!=', 'COMPLETADA')
      ->count();

    // Rendimiento por usuario: assigned, completadas, porcentaje
    $users = Usuario::where('activo', 1)->get();

    $userPerf = $users->map(function ($u) {
      $assigned = Tarea::where('asignado_a', $u->id)->count();
      $completed = Tarea::where('asignado_a', $u->id)->where('estado', 'COMPLETADA')->count();
      $overdue = Tarea::where('asignado_a', $u->id)
        ->whereNotNull('fecha_vencimiento')
        ->where('fecha_vencimiento', '<', now()->toDateString())
        ->where('estado', '!=', 'COMPLETADA')
        ->count();

      $percent = $assigned > 0 ? round($completed / $assigned * 100, 2) : 0;

      return [
        'usuario_id' => $u->id,
        'nombre' => $u->nombre,
        'assigned_count' => $assigned,
        'completed_count' => $completed,
        'overdue_count' => $overdue,
        'completion_rate' => $percent,
      ];
    })->sortByDesc('assigned_count')->values();

    // Top performers (por completion_rate, al menos 1 tarea asignada)
    $topPerformers = $userPerf->filter(function ($u) {
      return $u['assigned_count'] > 0;
    })->sortByDesc('completion_rate')->take(10)->values();

    return $this->successResponse('Estadísticas de tareas', [
      'total_tasks' => $totalTasks,
      'tasks_by_estado' => $tasksByEstado,
      'overdue_tasks' => $overdue,
      'user_performance' => $userPerf,
      'top_performers' => $topPerformers,
    ]);
  }
}

<?php

namespace App\Http\Controllers;

use App\Http\Traits\ApiResponseTrait;
use App\Http\Requests\StoreUsuarioRequest;
use App\Http\Requests\UpdateUsuarioRequest;
use App\Http\Resources\ProyectoResource;
use App\Http\Resources\UsuarioResource;
use App\Models\Usuario;
use App\Services\UsuarioService;
use Illuminate\Http\Request;
use Carbon\Carbon;
use App\Models\Tarea;
use App\Http\Resources\TareaResource;

class UsuarioController extends Controller
{
    use ApiResponseTrait;

    protected $service;

    public function __construct(UsuarioService $service)
    {
        $this->service = $service;
    }
    /**
     * Display a listing of the resource.
     */
    public function index(Request $request)
    {
        // Construir query con filtros opcionales
        $query = \App\Models\Usuario::with('roles');

        if ($request->filled('nombre')) {
            $nombre = $request->get('nombre');
            $query->where('nombre', 'like', '%' . $nombre . '%');
        }

        if ($request->filled('email')) {
            $email = $request->get('email');
            $query->where('email', 'like', '%' . $email . '%');
        }

        if ($request->filled('rol')) {
            $rol = $request->get('rol');
            $query->whereHas('roles', function ($q) use ($rol) {
                $q->where('nombre', $rol);
            });
        }

        // Obtener usuarios filtrados
        $usuarios = $query->get();

        // Estadísticas basadas en el conjunto filtrado
        $total = $usuarios->count();
        $activos = $usuarios->where('activo', 1)->count();
        $inactivos = $total - $activos;

        // Conteo por rol dentro del conjunto filtrado
        $countsPorRol = $usuarios->flatMap(function ($u) {
            return $u->roles->pluck('nombre');
        })->countBy()->toArray();

        $estadisticas = [
            'total' => $total,
            'activos' => $activos,
            'inactivos' => $inactivos,
            'por_rol' => $countsPorRol,
        ];

        return $this->successResponse('Usuarios obtenidos', [
            'usuarios' => UsuarioResource::collection($usuarios),
            'estadisticas' => $estadisticas,
        ]);
    }

    /**
     * Show the form for creating a new resource.
     */
    public function create()
    {
        //
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(Request $request)
    {
        $data = ($request instanceof StoreUsuarioRequest) ? $request->validated() : $request->all();
        $usuario = $this->service->create($data);
        return $this->successResponse('Usuario creado', new UsuarioResource($usuario), 201);
    }

    /**
     * Display the specified resource.
     */
    public function show(Usuario $usuario)
    {
        // Cargar roles para que UsuarioResource devuelva el rol del usuario
        $usuario->load('roles');
        return $this->successResponse('Usuario encontrado', new UsuarioResource($usuario));
    }

    /**
     * Cambiar el rol principal de un usuario (ADMIN only).
     * Se espera payload: { "rol": "ADMIN" } o { "rol": "USER" }
     */
    public function cambiarRol(\Illuminate\Http\Request $request, Usuario $usuario)
    {
        $actor = $request->user();
        if (! $actor || ! method_exists($actor, 'hasRole') || ! $actor->hasRole('ADMIN')) {
            return $this->errorResponse('No autorizado para cambiar roles', 403);
        }

        $rolNombre = $request->get('rol');
        if (! $rolNombre) {
            return $this->errorResponse('Se requiere el campo rol', 422);
        }

        // Validar que el rol exista
        $rol = \App\Models\Rol::where('nombre', $rolNombre)->first();
        if (! $rol) {
            return $this->errorResponse('Rol inválido', 422);
        }

        // Reemplazar roles del usuario por el rol indicado
        $usuario->roles()->sync([$rol->id]);

        $usuario->load('roles');
        return $this->successResponse('Rol del usuario actualizado', new UsuarioResource($usuario));
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(Usuario $usuario)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, Usuario $usuario)
    {
        $data = ($request instanceof UpdateUsuarioRequest) ? $request->validated() : $request->all();
        // Si la contraseña viene nula/empty, eliminarla del payload para no sobreescribir
        if (array_key_exists('password', $data) && empty($data['password'])) {
            unset($data['password']);
        }
        $usuario = $this->service->update($usuario, $data);
        return $this->successResponse('Usuario actualizado', new UsuarioResource($usuario));
    }

    /**
     * ADMIN: Actualizar cualquier campo de un usuario, incluyendo rol.
     * Se espera opcionally payload: nombre, email, password, activo, rol
     */
    public function adminUpdate(\App\Http\Requests\AdminUpdateUsuarioRequest $request, Usuario $usuario)
    {
        $actor = $request->user();
        if (! $actor || ! method_exists($actor, 'hasRole') || ! $actor->hasRole('ADMIN')) {
            return $this->errorResponse('No autorizado', 403);
        }

        $data = $request->validated();

        // Si password viene vacío o nulo, no sobreescribir
        if (array_key_exists('password', $data) && empty($data['password'])) {
            unset($data['password']);
        }

        // Actualizar campos básicos
        $usuario = $this->service->update($usuario, $data);

        // Si se proporcionó 'rol', sincronizar roles (reemplaza roles existentes)
        if (array_key_exists('rol', $data) && $data['rol']) {
            $rol = \App\Models\Rol::where('nombre', $data['rol'])->first();
            if ($rol) {
                $usuario->roles()->sync([$rol->id]);
            }
        }

        $usuario->load('roles');
        return $this->successResponse('Usuario actualizado por ADMIN', new UsuarioResource($usuario));
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(Usuario $usuario)
    {
        // Si el usuario está asignado a algún proyecto, no se elimina: se marca como inactivo
        $asignadoProyectos = $usuario->proyectos()->exists();

        if ($asignadoProyectos) {
            $usuario->activo = 0;
            $usuario->save();
            return $this->successResponse('Usuario marcado como inactivo porque está asignado a proyectos', null);
        }

        // Si no está asignado a proyectos, se elimina realmente
        $this->service->delete($usuario);
        return $this->successResponse('Usuario eliminado', null);
    }


    // public function misProyectos(Request $request)
    // {
    //     $user = $request->user();
    //     $proyectos = $user->proyectos()->with('tareas')->get();

    //     return $this->successResponse('Proyectos del usuario', ProyectoResource::collection($proyectos));
    // }
    /**
     * Obtener los proyectos en los que participa el usuario autenticado (desde token)
     */
    public function misProyectos(Request $request)
    {
        $user = $request->user();

        // Query base: proyectos del usuario
        $query = $user->proyectos();

        // Filtros opcionales: titulo (buscar con LIKE), rango de fechas sobre fecha_creacion
        if ($request->filled('titulo')) {
            $titulo = $request->get('titulo');
            $query->where('titulo', 'like', '%' . $titulo . '%');
        }

        // Fecha inicio y fin (esperadas en formato YYYY-MM-DD)
        $fechaInicio = $request->get('fecha_inicio');
        $fechaFin = $request->get('fecha_fin');

        if ($fechaInicio) {
            try {
                $start = Carbon::parse($fechaInicio)->startOfDay();
                $query->where('fecha_creacion', '>=', $start);
            } catch (\Exception $e) {
                // ignorar filtro si la fecha es inválida
            }
        }

        if ($fechaFin) {
            try {
                $end = Carbon::parse($fechaFin)->endOfDay();
                $query->where('fecha_creacion', '<=', $end);
            } catch (\Exception $e) {
                // ignorar filtro si la fecha es inválida
            }
        }

        // Obtener solo la información de proyectos (sin cargar tareas)
        $proyectos = $query->get();

        return $this->successResponse('Proyectos del usuario', ProyectoResource::collection($proyectos));
    }

    /**
     * Listar todas las tareas asignadas a un usuario específico junto con estadísticas.
     * Permisos: el actor debe ser ADMIN o el mismo usuario consultado.
     */
    public function tareasAsignadas(Request $request, Usuario $usuario)
    {
        $actor = $request->user();
        if (! $actor) {
            return $this->errorResponse('No autorizado', 401);
        }

        if (! (method_exists($actor, 'hasRole') && $actor->hasRole('ADMIN')) && $actor->id !== $usuario->id) {
            return $this->errorResponse('No tiene permiso para ver las tareas de este usuario', 403);
        }

        // Cargar tareas asignadas con relaciones necesarias
        $tareasQuery = $usuario->tareasAsignadas()->with(['proyecto', 'creador', 'asignadoA', 'comentarios.usuario']);

        // Soportar filtros opcionales: estado, proyecto_id, fecha_vencimiento antes/después
        if ($request->filled('estado')) {
            $tareasQuery->where('estado', $request->get('estado'));
        }

        if ($request->filled('proyecto_id')) {
            $tareasQuery->where('proyecto_id', $request->get('proyecto_id'));
        }

        if ($request->filled('vencimiento_before')) {
            try {
                $tareasQuery->where('fecha_vencimiento', '<=', Carbon::parse($request->get('vencimiento_before')));
            } catch (\Exception $e) {
                // ignorar
            }
        }

        if ($request->filled('vencimiento_after')) {
            try {
                $tareasQuery->where('fecha_vencimiento', '>=', Carbon::parse($request->get('vencimiento_after')));
            } catch (\Exception $e) {
                // ignorar
            }
        }

        $tareas = $tareasQuery->get();

        $total = $tareas->count();
        $counts = [
            'PENDIENTE' => $tareas->where('estado', 'PENDIENTE')->count(),
            'PROGRESO' => $tareas->where('estado', 'PROGRESO')->count(),
            'COMPLETADA' => $tareas->where('estado', 'COMPLETADA')->count(),
        ];

        $porcentajeCompletadas = $total > 0 ? round(($counts['COMPLETADA'] / $total) * 100, 2) : 0;

        // Progreso ponderado: PENDIENTE=0, PROGRESO=50, COMPLETADA=100
        $sumaPonderada = $counts['PROGRESO'] * 50 + $counts['COMPLETADA'] * 100;
        $porcentajePonderado = $total > 0 ? round($sumaPonderada / $total, 2) : 0;

        $estadisticas = [
            'total' => $total,
            'counts' => $counts,
            'porcentaje_completadas' => $porcentajeCompletadas,
            'porcentaje_progreso_ponderado' => $porcentajePonderado,
        ];

        return $this->successResponse('Tareas asignadas obtenidas', [
            'tareas' => TareaResource::collection($tareas),
            'estadisticas' => $estadisticas,
        ]);
    }
}

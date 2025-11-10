<?php

namespace App\Http\Controllers;

use App\Http\Traits\ApiResponseTrait;
use App\Http\Requests\StoreProyectoRequest;
use App\Http\Requests\UpdateProyectoRequest;
use App\Http\Resources\ProyectoResource;
use App\Http\Resources\UsuarioResource;
use App\Models\Proyecto;
use App\Models\Usuario;
use App\Models\Tarea;
use App\Http\Requests\AssignUsuarioProyectoRequest;
use App\Services\ProyectoService;
use Illuminate\Http\Request;

class ProyectoController extends Controller
{
    use ApiResponseTrait;

    protected $service;

    public function __construct(ProyectoService $service)
    {
        $this->service = $service;
    }
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        $proyectos = $this->service->all();
        return $this->successResponse('Proyectos obtenidos', ProyectoResource::collection($proyectos));
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
        $data = ($request instanceof StoreProyectoRequest) ? $request->validated() : $request->all();

        // Asignar creado_por desde el usuario autenticado (token)
        $user = $request->user();
        if ($user) {
            // Solo usuarios con rol 'USER' pueden crear proyectos
            if (! method_exists($user, 'hasRole') || ! $user->hasRole('USER')) {
                return $this->errorResponse('Solo usuarios con rol USER pueden crear proyectos', 403);
            }

            $data['creado_por'] = $user->id;
        }

        $proyecto = $this->service->create($data);

        // Asegurar que el creador quede en el pivot usuario_proyecto con rol CREADOR
        if ($user) {
            $proyecto->usuarios()->syncWithoutDetaching([
                $user->id => ['rol_proyecto' => 'CREADOR'],
            ]);
        }

        return $this->successResponse('Proyecto creado', new ProyectoResource($proyecto), 201);
    }

    /**
     * Display the specified resource.
     */
    public function show(Request $request, Proyecto $proyecto)
    {
        // Cargar tareas y la relación del usuario asignado para cada tarea
        $proyecto->load(['tareas.asignadoA', 'creadoPor']);

        // Si hay un usuario autenticado, intentar obtener el rol_proyecto desde el pivot
        $user = $request->user();
        if ($user) {
            $relation = $user->proyectos()->where('proyectos.id', $proyecto->id)->first();
            if ($relation && isset($relation->pivot)) {
                // Adjuntar el pivot al modelo para que ProyectoResource lo lea
                $proyecto->pivot = $relation->pivot;
            }
        }

        return $this->successResponse('Proyecto encontrado', new ProyectoResource($proyecto));
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(Proyecto $proyecto)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, Proyecto $proyecto)
    {
        $data = ($request instanceof UpdateProyectoRequest) ? $request->validated() : $request->all();
        $proyecto = $this->service->update($proyecto, $data);
        return $this->successResponse('Proyecto actualizado', new ProyectoResource($proyecto));
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(Proyecto $proyecto)
    {
        $this->service->delete($proyecto);
        return $this->successResponse('Proyecto eliminado', null);
    }

    /**
     * Listar usuarios con rol 'USER' que NO están asignados a este proyecto.
     * Permite filtrar por nombre y email mediante query params `nombre` y `email`.
     */
    public function usuariosNoAsignados(Request $request, Proyecto $proyecto)
    {
        $query = Usuario::whereHas('roles', function ($q) {
            $q->where('nombre', 'USER');
        })->whereDoesntHave('proyectos', function ($q) use ($proyecto) {
            $q->where('proyectos.id', $proyecto->id);
        })->where('activo', 1);

        if ($request->filled('nombre')) {
            $query->where('nombre', 'like', '%' . $request->get('nombre') . '%');
        }

        if ($request->filled('email')) {
            $query->where('email', 'like', '%' . $request->get('email') . '%');
        }

        $usuarios = $query->get();

        return $this->successResponse('Usuarios no asignados al proyecto', UsuarioResource::collection($usuarios));
    }

    /**
     * Asignar un usuario a un proyecto con un rol en el proyecto (CREADOR o COLABORADOR).
     * Reglas de autorización: sólo un ADMIN global o el CREADOR del proyecto pueden asignar usuarios.
     */
    public function asignarUsuario(AssignUsuarioProyectoRequest $request, Proyecto $proyecto)
    {
        $user = $request->user();

        // Verificar permisos: ADMIN global
        // $isAdmin = $user && method_exists($user, 'hasRole') && $user->hasRole('ADMIN');

        // Verificar si es CREADOR del proyecto
        $isCreador = false;
        if ($user) {
            $relation = $user->proyectos()->where('proyectos.id', $proyecto->id)->first();
            if ($relation && isset($relation->pivot) && isset($relation->pivot->rol_proyecto)) {
                $isCreador = $relation->pivot->rol_proyecto === 'CREADOR';
            }
        }

        if (! $isCreador) {
            return $this->errorResponse('No autorizado para asignar usuarios a este proyecto', 403);
        }

        $data = $request->validated();
        $target = Usuario::findOrFail($data['usuario_id']);

        // Asegurar que el usuario a asignar tenga rol 'USER' (opcional: permitir admins)
        if (! $target->roles->pluck('nombre')->contains('USER')) {
            return $this->errorResponse('Solo se pueden asignar usuarios con rol USER', 422);
        }

        $rolProyecto = $data['rol_proyecto'] ?? 'COLABORADOR';

        // Asignar en la tabla pivot
        $proyecto->usuarios()->syncWithoutDetaching([
            $target->id => ['rol_proyecto' => $rolProyecto],
        ]);

        return $this->successResponse('Usuario asignado al proyecto', new UsuarioResource($target));
    }

    /**
     * Devuelve los usuarios asignados a un proyecto junto con su rol en el mismo (rol_proyecto desde pivot).
     */
    public function usuariosAsignados(Request $request, Proyecto $proyecto)
    {
        // Base query: usuarios relacionados (incluye pivot)
        $query = $proyecto->usuarios()->with('roles')->where('activo', 1);

        // Filtrado opcional por nombre (LIKE)
        if ($request->filled('nombre')) {
            $query->where('nombre', 'like', '%' . $request->get('nombre') . '%');
        }

        $usuarios = $query->get();

        // Mapear para incluir rol_proyecto en cada elemento devuelto
        $data = $usuarios->map(function ($u) {
            return [
                'id' => $u->id,
                'nombre' => $u->nombre,
                'email' => $u->email,
                'activo' => (bool) $u->activo,
                'fecha_creacion' => $u->fecha_creacion,
                'roles' => $u->relationLoaded('roles') ? $u->roles->map(function ($r) {
                    return ['id' => $r->id, 'nombre' => $r->nombre];
                })->values() : null,
                'rol_proyecto' => isset($u->pivot) && isset($u->pivot->rol_proyecto) ? $u->pivot->rol_proyecto : null,
            ];
        })->values();

        return $this->successResponse('Usuarios asignados al proyecto', $data);
    }

    /**
     * Quitar la asignación de un usuario a un proyecto.
     * Sólo ADMIN global o CREADOR del proyecto pueden quitar asignaciones.
     * La asignación solo puede eliminarse si el usuario no tiene tareas asignadas en ese proyecto.
     */
    public function quitarUsuario(Request $request, Proyecto $proyecto, Usuario $usuario)
    {
        $actor = $request->user();

        // Permisos
        $isAdmin = $actor && method_exists($actor, 'hasRole') && $actor->hasRole('ADMIN');
        $isCreador = false;
        if ($actor) {
            $relation = $actor->proyectos()->where('proyectos.id', $proyecto->id)->first();
            if ($relation && isset($relation->pivot) && isset($relation->pivot->rol_proyecto)) {
                $isCreador = $relation->pivot->rol_proyecto === 'CREADOR';
            }
        }

        if (! $isAdmin && ! $isCreador) {
            return $this->errorResponse('No autorizado para quitar asignaciones en este proyecto', 403);
        }

        // Verificar si el usuario tiene tareas asignadas en este proyecto
        $tareasCount = Tarea::where('proyecto_id', $proyecto->id)
            ->where('asignado_a', $usuario->id)
            ->count();

        if ($tareasCount > 0) {
            return $this->errorResponse('El usuario tiene tareas asignadas en este proyecto. Para eliminar la asignación primero quite o reasigne sus tareas.', 422);
        }

        // Eliminar pivot
        $proyecto->usuarios()->detach($usuario->id);

        return $this->successResponse('Asignación eliminada', null);
    }
}

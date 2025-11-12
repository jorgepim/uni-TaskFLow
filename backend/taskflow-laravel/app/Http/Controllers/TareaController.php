<?php

namespace App\Http\Controllers;

use App\Http\Traits\ApiResponseTrait;
use App\Http\Requests\StoreTareaRequest;
use App\Http\Requests\UpdateTareaRequest;
use App\Http\Resources\TareaResource;
use App\Models\Tarea;
use App\Models\Usuario;
use App\Services\TareaService;
use Illuminate\Http\Request;
use App\Http\Requests\UpdateTareaEstadoRequest;

class TareaController extends Controller
{
    use ApiResponseTrait;

    protected $service;

    public function __construct(TareaService $service)
    {
        $this->service = $service;
    }
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        // Eager load la relación del usuario asignado para incluir su nombre en la respuesta
        $tareas = $this->service->all(['asignadoA']);
        return $this->successResponse('Tareas obtenidas', TareaResource::collection($tareas));
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
    public function store(StoreTareaRequest $request)
    {
        // Usar datos validados por el FormRequest
        $data = $request->validated();

        // Asignar creado_por desde el usuario autenticado (token)
        $user = $request->user();
        if ($user) {
            $data['creado_por'] = $user->id;
        }

        // Validar que el usuario asignado (si se proporciona) esté ACTIVO y no sea ADMIN
        if (array_key_exists('asignado_a', $data) && $data['asignado_a']) {
            $asignado = Usuario::with('roles')->find($data['asignado_a']);
            if (! $asignado) {
                return $this->errorResponse('Usuario asignado no existe', 422);
            }
            if ($asignado->activo == 0) {
                return $this->errorResponse('No se puede asignar una tarea a un usuario inactivo', 422);
            }
            if ($asignado->hasRole('ADMIN')) {
                return $this->errorResponse('No se puede asignar una tarea a un usuario con rol ADMIN', 422);
            }
        }

        $tarea = $this->service->create($data);
        return $this->successResponse('Tarea creada', new TareaResource($tarea), 201);
    }

    /**
     * Display the specified resource.
     */
    public function show(Tarea $tarea)
    {
        // Cargar creador y comentarios + usuario de cada comentario
        $tarea->load(['creador', 'asignadoA', 'comentarios.usuario']);
        return $this->successResponse('Tarea encontrada', new TareaResource($tarea));
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(Tarea $tarea)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(UpdateTareaRequest $request, Tarea $tarea)
    {
        // Usar datos validados por el FormRequest
        $data = $request->validated();
        // Si se intenta reasignar la tarea, validar que el nuevo usuario esté activo y no sea ADMIN
        if (array_key_exists('asignado_a', $data) && $data['asignado_a']) {
            $asignado = Usuario::with('roles')->find($data['asignado_a']);
            if (! $asignado) {
                return $this->errorResponse('Usuario asignado no existe', 422);
            }
            if ($asignado->activo == 0) {
                return $this->errorResponse('No se puede asignar una tarea a un usuario inactivo', 422);
            }
            if ($asignado->hasRole('ADMIN')) {
                return $this->errorResponse('No se puede asignar una tarea a un usuario con rol ADMIN', 422);
            }
        }
        $tarea = $this->service->update($tarea, $data);
        return $this->successResponse('Tarea actualizada', new TareaResource($tarea));
    }

    /**
     * Actualizar únicamente el estado de una tarea.
     * Solo puede hacerlo el usuario asignado a la tarea o el usuario que sea CREADOR del proyecto.
     */
    public function updateEstado(UpdateTareaEstadoRequest $request, Tarea $tarea)
    {
        $user = $request->user();

        // Verificar si es el usuario asignado
        $isAsignado = $user && $tarea->asignado_a && $user->id === (int) $tarea->asignado_a;

        // Verificar si el usuario es CREADOR en el proyecto (buscar pivot)
        $isCreador = false;
        if ($user) {
            $relation = $user->proyectos()->where('proyectos.id', $tarea->proyecto_id)->first();
            if ($relation && isset($relation->pivot) && isset($relation->pivot->rol_proyecto)) {
                $isCreador = $relation->pivot->rol_proyecto === 'CREADOR';
            }
        }

        if (! $isAsignado && ! $isCreador) {
            return $this->errorResponse('No autorizado para cambiar el estado de esta tarea', 403);
        }

        $data = $request->validated();
        $tarea = $this->service->update($tarea, ['estado' => $data['estado']]);

        return $this->successResponse('Estado de tarea actualizado', new TareaResource($tarea));
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(Tarea $tarea)
    {
        $this->service->delete($tarea);
        return $this->successResponse('Tarea eliminada', null);
    }
}

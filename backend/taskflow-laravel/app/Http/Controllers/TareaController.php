<?php

namespace App\Http\Controllers;

use App\Http\Traits\ApiResponseTrait;
use App\Http\Requests\StoreTareaRequest;
use App\Http\Requests\UpdateTareaRequest;
use App\Http\Resources\TareaResource;
use App\Models\Tarea;
use App\Services\TareaService;
use Illuminate\Http\Request;

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
    public function store(Request $request)
    {
        $data = ($request instanceof StoreTareaRequest) ? $request->validated() : $request->all();

        // Asignar creado_por desde el usuario autenticado (token)
        $user = $request->user();
        if ($user) {
            $data['creado_por'] = $user->id;
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
        $tarea->load(['creador', 'comentarios.usuario']);
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
    public function update(Request $request, Tarea $tarea)
    {
        $data = ($request instanceof UpdateTareaRequest) ? $request->validated() : $request->all();
        $tarea = $this->service->update($tarea, $data);
        return $this->successResponse('Tarea actualizada', new TareaResource($tarea));
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

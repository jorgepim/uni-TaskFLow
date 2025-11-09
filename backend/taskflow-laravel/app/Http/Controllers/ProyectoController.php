<?php

namespace App\Http\Controllers;

use App\Http\Traits\ApiResponseTrait;
use App\Http\Requests\StoreProyectoRequest;
use App\Http\Requests\UpdateProyectoRequest;
use App\Http\Resources\ProyectoResource;
use App\Models\Proyecto;
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
    public function show(Proyecto $proyecto)
    {
        // Cargar tareas y la relación del usuario asignado para cada tarea
        $proyecto->load(['tareas.asignadoA']);
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
}

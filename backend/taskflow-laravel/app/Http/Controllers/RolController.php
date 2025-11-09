<?php

namespace App\Http\Controllers;

use App\Http\Traits\ApiResponseTrait;
use App\Http\Requests\StoreRolRequest;
use App\Http\Requests\UpdateRolRequest;
use App\Http\Resources\RolResource;
use App\Models\Rol;
use App\Services\RolService;
use Illuminate\Http\Request;

class RolController extends Controller
{
    use ApiResponseTrait;

    protected $service;

    public function __construct(RolService $service)
    {
        $this->service = $service;
    }
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        $roles = $this->service->all();
        return $this->successResponse('Roles obtenidos', RolResource::collection($roles));
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
        $data = ($request instanceof StoreRolRequest) ? $request->validated() : $request->all();
        $rol = $this->service->create($data);
        return $this->successResponse('Rol creado', new RolResource($rol), 201);
    }

    /**
     * Display the specified resource.
     */
    public function show(Rol $rol)
    {
        return $this->successResponse('Rol encontrado', new RolResource($rol));
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(Rol $rol)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, Rol $rol)
    {
        $data = ($request instanceof UpdateRolRequest) ? $request->validated() : $request->all();
        $rol = $this->service->update($rol, $data);
        return $this->successResponse('Rol actualizado', new RolResource($rol));
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(Rol $rol)
    {
        $this->service->delete($rol);
        return $this->successResponse('Rol eliminado', null);
    }
}

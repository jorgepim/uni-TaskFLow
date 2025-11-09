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
    public function index()
    {
        $usuarios = $this->service->all();
        return $this->successResponse('Usuarios obtenidos', UsuarioResource::collection($usuarios));
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
        return $this->successResponse('Usuario encontrado', new UsuarioResource($usuario));
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
     * Remove the specified resource from storage.
     */
    public function destroy(Usuario $usuario)
    {
        $this->service->delete($usuario);
        return $this->successResponse('Usuario eliminado', null);
    }

    /**
     * Obtener los proyectos en los que participa el usuario autenticado (desde token)
     */
    public function misProyectos(Request $request)
    {
        $user = $request->user();
        $proyectos = $user->proyectos()->with('tareas')->get();

        return $this->successResponse('Proyectos del usuario', ProyectoResource::collection($proyectos));
    }
}

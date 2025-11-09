<?php

namespace App\Http\Controllers;

use App\Http\Traits\ApiResponseTrait;
use App\Http\Requests\StoreComentarioRequest;
use App\Http\Requests\UpdateComentarioRequest;
use App\Http\Resources\ComentarioResource;
use App\Models\Comentario;
use App\Services\ComentarioService;
use Illuminate\Http\Request;

class ComentarioController extends Controller
{
    use ApiResponseTrait;

    protected $service;

    public function __construct(ComentarioService $service)
    {
        $this->service = $service;
    }
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        $comentarios = $this->service->all();
        return $this->successResponse('Comentarios obtenidos', ComentarioResource::collection($comentarios));
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
        $data = ($request instanceof StoreComentarioRequest) ? $request->validated() : $request->all();

        // Asignar usuario_id desde el usuario autenticado (token) si está disponible
        $user = $request->user();
        if ($user) {
            $data['usuario_id'] = $user->id;
        }

        $comentario = $this->service->create($data);
        return $this->successResponse('Comentario creado', new ComentarioResource($comentario), 201);
    }

    /**
     * Display the specified resource.
     */
    public function show(Comentario $comentario)
    {
        return $this->successResponse('Comentario encontrado', new ComentarioResource($comentario));
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(Comentario $comentario)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, Comentario $comentario)
    {
        $data = ($request instanceof UpdateComentarioRequest) ? $request->validated() : $request->all();
        $comentario = $this->service->update($comentario, $data);
        return $this->successResponse('Comentario actualizado', new ComentarioResource($comentario));
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(Comentario $comentario)
    {
        $this->service->delete($comentario);
        return $this->successResponse('Comentario eliminado', null);
    }
}

<?php

namespace App\Repositories;

use App\Models\Usuario;

class UsuarioRepository
{
  // Aquí van los métodos para interactuar con el modelo Usuario
  protected $model;
  public function __construct(Usuario $model = null)
  {
    $this->model = $model ?? new Usuario();
  }

  public function all(array $relations = [])
  {
    return $this->model->with($relations)->get();
  }

  public function find($id, array $relations = [])
  {
    return $this->model->with($relations)->findOrFail($id);
  }

  public function create(array $data)
  {
    return $this->model->create($data);
  }

  public function update(Usuario $usuario, array $data)
  {
    $usuario->update($data);
    return $usuario;
  }

  public function delete(Usuario $usuario)
  {
    return $usuario->delete();
  }
}

<?php

namespace App\Repositories;

use App\Models\Comentario;

class ComentarioRepository
{
  // Aquí van los métodos para interactuar con el modelo Comentario
  protected $model;

  public function __construct(Comentario $model = null)
  {
    $this->model = $model ?? new Comentario();
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

  public function update(Comentario $comentario, array $data)
  {
    $comentario->update($data);
    return $comentario;
  }

  public function delete(Comentario $comentario)
  {
    return $comentario->delete();
  }
}

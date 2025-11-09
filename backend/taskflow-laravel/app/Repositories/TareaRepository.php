<?php

namespace App\Repositories;

use App\Models\Tarea;

class TareaRepository
{
  // Aquí van los métodos para interactuar con el modelo Tarea
  protected $model;

  public function __construct(Tarea $model = null)
  {
    $this->model = $model ?? new Tarea();
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

  public function update(Tarea $tarea, array $data)
  {
    $tarea->update($data);
    return $tarea;
  }

  public function delete(Tarea $tarea)
  {
    return $tarea->delete();
  }
}

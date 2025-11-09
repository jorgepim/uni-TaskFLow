<?php

namespace App\Repositories;

use App\Models\Proyecto;

class ProyectoRepository
{
  // Aquí van los métodos para interactuar con el modelo Proyecto
  protected $model;

  public function __construct(Proyecto $model = null)
  {
    $this->model = $model ?? new Proyecto();
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

  public function update(Proyecto $proyecto, array $data)
  {
    $proyecto->update($data);
    return $proyecto;
  }

  public function delete(Proyecto $proyecto)
  {
    return $proyecto->delete();
  }
}

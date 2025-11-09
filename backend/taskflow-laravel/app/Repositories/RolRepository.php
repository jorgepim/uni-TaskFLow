<?php

namespace App\Repositories;

use App\Models\Rol;

class RolRepository
{
  protected $model;

  public function __construct(Rol $model = null)
  {
    $this->model = $model ?? new Rol();
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

  public function update(Rol $rol, array $data)
  {
    $rol->update($data);
    return $rol;
  }

  public function delete(Rol $rol)
  {
    return $rol->delete();
  }
}

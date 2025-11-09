<?php

namespace App\Services;

use App\Repositories\RolRepository;
use App\Models\Rol;

class RolService
{
  protected $repo;

  public function __construct(RolRepository $repo)
  {
    $this->repo = $repo;
  }

  public function all(array $relations = [])
  {
    return $this->repo->all($relations);
  }

  public function find($id, array $relations = [])
  {
    return $this->repo->find($id, $relations);
  }

  public function create(array $data)
  {
    return $this->repo->create($data);
  }

  public function update(Rol $rol, array $data)
  {
    return $this->repo->update($rol, $data);
  }

  public function delete(Rol $rol)
  {
    return $this->repo->delete($rol);
  }
}

<?php

namespace App\Services;

use App\Repositories\ProyectoRepository;
use App\Models\Proyecto;

class ProyectoService
{
  protected $repo;

  public function __construct(ProyectoRepository $repo)
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

  public function update(Proyecto $proyecto, array $data)
  {
    return $this->repo->update($proyecto, $data);
  }

  public function delete(Proyecto $proyecto)
  {
    return $this->repo->delete($proyecto);
  }
}

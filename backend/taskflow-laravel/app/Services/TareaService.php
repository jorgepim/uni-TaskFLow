<?php

namespace App\Services;

use App\Repositories\TareaRepository;
use App\Models\Tarea;

class TareaService
{
  protected $repo;

  public function __construct(TareaRepository $repo)
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

  public function update(Tarea $tarea, array $data)
  {
    return $this->repo->update($tarea, $data);
  }

  public function delete(Tarea $tarea)
  {
    return $this->repo->delete($tarea);
  }
}

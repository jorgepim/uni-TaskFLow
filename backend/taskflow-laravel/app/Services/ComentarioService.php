<?php

namespace App\Services;

use App\Repositories\ComentarioRepository;
use App\Models\Comentario;

class ComentarioService
{
  protected $repo;

  public function __construct(ComentarioRepository $repo)
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

  public function update(Comentario $comentario, array $data)
  {
    return $this->repo->update($comentario, $data);
  }

  public function delete(Comentario $comentario)
  {
    return $this->repo->delete($comentario);
  }
}

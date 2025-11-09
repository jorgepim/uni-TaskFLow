<?php

namespace App\Services;

use App\Repositories\UsuarioRepository;
use App\Models\Usuario;

class UsuarioService
{
  protected $repo;

  public function __construct(UsuarioRepository $repo)
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

  public function update(Usuario $usuario, array $data)
  {
    return $this->repo->update($usuario, $data);
  }

  public function delete(Usuario $usuario)
  {
    return $this->repo->delete($usuario);
  }
}

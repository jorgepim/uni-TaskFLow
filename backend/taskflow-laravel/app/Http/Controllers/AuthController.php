<?php

namespace App\Http\Controllers;

use App\Http\Traits\ApiResponseTrait;
use App\Http\Requests\LoginRequest;
use App\Http\Requests\StoreUsuarioRequest;
use App\Http\Resources\UsuarioResource;
use App\Models\Usuario;
use App\Models\Rol;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

class AuthController extends Controller
{
  use ApiResponseTrait;

  public function register(StoreUsuarioRequest $request)
  {
    $data = $request->validated();

    // Crear usuario
    $usuario = Usuario::create($data);

    // Crear token personal
    $token = $usuario->createToken('api-token')->plainTextToken;

    // Asignar rol por defecto 'USER'
    $defaultRole = Rol::firstOrCreate(['nombre' => 'USER']);
    $usuario->roles()->syncWithoutDetaching([$defaultRole->id]);

    // Cargar roles para incluirlos en la respuesta
    $usuario->load('roles');

    return $this->successResponse('Usuario registrado', [
      'user' => new UsuarioResource($usuario),
      'token' => $token,
      'roles' => $usuario->roles->pluck('nombre')->all(),
    ], 201);
  }

  public function login(LoginRequest $request)
  {
    $data = $request->validated();

    $usuario = Usuario::where('email', $data['email'])->first();

    if (! $usuario || ! Hash::check($data['password'], $usuario->password)) {
      return $this->errorResponse('Credenciales inválidas', 401);
    }

    // Eliminar tokens antiguos opcional (por ejemplo single-session)
    // $usuario->tokens()->delete();

    $token = $usuario->createToken('api-token')->plainTextToken;

    // Cargar roles para que el frontend pueda decidir el dashboard
    $usuario->load('roles');

    return $this->successResponse('Login exitoso', [
      'user' => new UsuarioResource($usuario),
      'token' => $token,
      // Devolver también un arreglo simple de nombres de rol para uso directo en frontend
      'roles' => $usuario->roles->pluck('nombre')->all(),
    ]);
  }

  public function logout(Request $request)
  {
    $user = $request->user();
    if ($user) {
      // Revoca el token actual
      $request->user()->currentAccessToken()->delete();
    }

    return $this->successResponse('Logout exitoso', null);
  }
}

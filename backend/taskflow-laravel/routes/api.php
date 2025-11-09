<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\UsuarioController;
use App\Http\Controllers\RolController;
use App\Http\Controllers\ProyectoController;
use App\Http\Controllers\TareaController;
use App\Http\Controllers\ComentarioController;
use App\Http\Controllers\AuthController;

Route::get('/user', function (Request $request) {
    return $request->user();
})->middleware('auth:sanctum');

// API resource routes para probar los controladores
// Rutas públicas (register/login) ya definidas más arriba

// Rutas protegidas por token
Route::middleware('auth:sanctum')->group(function () {
    // Rutas que solo ADMIN puede gestionar
    Route::middleware('role:ADMIN')->group(function () {
        Route::apiResource('roles', RolController::class);
    });

    // Rutas accesibles para usuarios autenticados (podrás usar policies/middleware adicionales)
    Route::apiResource('usuarios', UsuarioController::class);
    // Obtener proyectos del usuario autenticado
    Route::get('usuarios/me/proyectos', [UsuarioController::class, 'misProyectos']);
    Route::apiResource('proyectos', ProyectoController::class);
    Route::apiResource('tareas', TareaController::class);
    // Solo actualizar el estado de una tarea
    Route::patch('tareas/{tarea}/estado', [TareaController::class, 'updateEstado']);
    Route::apiResource('comentarios', ComentarioController::class);
});

// Auth
Route::post('register', [AuthController::class, 'register']);
Route::post('login', [AuthController::class, 'login']);
Route::post('logout', [AuthController::class, 'logout'])->middleware('auth:sanctum');

<?php

namespace Database\Seeders;

use App\Models\Comentario;
use App\Models\Proyecto;
use App\Models\Rol;
use App\Models\Tarea;
use App\Models\Usuario;
use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;

class DatabaseSeeder extends Seeder
{
    use WithoutModelEvents;

    /**
     * Seed the application's database.
     */
    public function run(): void
    {
        // Roles
        $adminRole = Rol::firstOrCreate(['nombre' => 'ADMIN']);
        $userRole = Rol::firstOrCreate(['nombre' => 'USER']);

        // Usuarios
        $admin = Usuario::firstOrCreate([
            'email' => 'admin@example.com',
        ], [
            'nombre' => 'Admin',
            'password' => 'password',
            'activo' => true,
        ]);

        // Crear 7 usuarios con rol USER
        $users = [];
        for ($i = 1; $i <= 7; $i++) {
            $u = Usuario::firstOrCreate([
                'email' => "user{$i}@example.com",
            ], [
                'nombre' => "User {$i}",
                'password' => 'password',
                'activo' => true,
            ]);
            $u->roles()->syncWithoutDetaching([$userRole->id]);
            $users[] = $u;
        }

        // Asignar rol ADMIN al admin (solo uno)
        $admin->roles()->syncWithoutDetaching([$adminRole->id]);

        // Proyectos
        // Crear proyectos creados por usuarios (rol USER)
        $proyectoA = Proyecto::create([
            'titulo' => 'Proyecto A',
            'descripcion' => 'Proyecto de ejemplo A',
            'creado_por' => $users[0]->id,
        ]);

        $proyectoB = Proyecto::create([
            'titulo' => 'Proyecto B',
            'descripcion' => 'Proyecto de ejemplo B',
            'creado_por' => $users[2]->id,
        ]);

        // Vincular usuarios (solo USERS) a proyectos (pivot rol_proyecto)
        // Proyecto A: creator users[0], collaborator users[1]
        $proyectoA->usuarios()->syncWithoutDetaching([
            $users[0]->id => ['rol_proyecto' => 'CREADOR'],
            $users[1]->id => ['rol_proyecto' => 'COLABORADOR'],
        ]);

        // Proyecto B: creator users[2], collaborator users[3]
        $proyectoB->usuarios()->syncWithoutDetaching([
            $users[2]->id => ['rol_proyecto' => 'CREADOR'],
            $users[3]->id => ['rol_proyecto' => 'COLABORADOR'],
        ]);

        // Tareas: 2 tareas en proyectoA, 1 tarea en proyectoB

        $tarea1 = Tarea::create([
            'titulo' => 'Tarea 1 - Proyecto A',
            'descripcion' => 'Primera tarea del proyecto A',
            'proyecto_id' => $proyectoA->id,
            'asignado_a' => $users[0]->id,
            'creado_por' => $users[0]->id,
        ]);


        $tarea2 = Tarea::create([
            'titulo' => 'Tarea 2 - Proyecto A',
            'descripcion' => 'Segunda tarea del proyecto A',
            'proyecto_id' => $proyectoA->id,
            'asignado_a' => $users[1]->id,
            'creado_por' => $users[0]->id,
        ]);

        $tarea3 = Tarea::create([
            'titulo' => 'Tarea 1 - Proyecto B',
            'descripcion' => 'Primera tarea del proyecto B',
            'proyecto_id' => $proyectoB->id,
            'asignado_a' => $users[2]->id,
            'creado_por' => $users[2]->id,
        ]);

        // Comentario en una tarea (tarea1)
        Comentario::create([
            'texto' => 'Este es un comentario de ejemplo en la tarea 1',
            'tarea_id' => $tarea1->id,
            'usuario_id' => $users[0]->id,
        ]);
    }
}

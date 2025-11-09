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
            'email' => 'jorgepimentel162@gmail.com',
        ], [
            'nombre' => 'Admin',
            'password' => '123321',
            'activo' => true,
        ]);

        $user = Usuario::firstOrCreate([
            'email' => 'willygetta4@gmail.com',
        ], [
            'nombre' => 'Normal User',
            'password' => '123321',
            'activo' => true,
        ]);

        // Asignar roles
        $admin->roles()->syncWithoutDetaching([$adminRole->id]);
        $user->roles()->syncWithoutDetaching([$userRole->id]);

        // Proyectos
        $proyectoA = Proyecto::create([
            'titulo' => 'Proyecto A',
            'descripcion' => 'Proyecto de ejemplo A',
            'creado_por' => $admin->id,
        ]);

        $proyectoB = Proyecto::create([
            'titulo' => 'Proyecto B',
            'descripcion' => 'Proyecto de ejemplo B',
            'creado_por' => $admin->id,
        ]);

        // Vincular usuarios a proyectos (pivot rol_proyecto)
        $proyectoA->usuarios()->syncWithoutDetaching([
            $admin->id => ['rol_proyecto' => 'CREADOR'],
            $user->id => ['rol_proyecto' => 'COLABORADOR'],
        ]);

        $proyectoB->usuarios()->syncWithoutDetaching([
            $admin->id => ['rol_proyecto' => 'CREADOR'],
            $user->id => ['rol_proyecto' => 'COLABORADOR'],
        ]);

        // Tareas: 2 tareas en proyectoA, 1 tarea en proyectoB
        $tarea1 = Tarea::create([
            'titulo' => 'Tarea 1 - Proyecto A',
            'descripcion' => 'Primera tarea del proyecto A',
            'proyecto_id' => $proyectoA->id,
            'asignado_a' => $user->id,
            'creado_por' => $admin->id,
        ]);

        $tarea2 = Tarea::create([
            'titulo' => 'Tarea 2 - Proyecto A',
            'descripcion' => 'Segunda tarea del proyecto A',
            'proyecto_id' => $proyectoA->id,
            'asignado_a' => $admin->id,
            'creado_por' => $admin->id,
        ]);

        $tarea3 = Tarea::create([
            'titulo' => 'Tarea 1 - Proyecto B',
            'descripcion' => 'Primera tarea del proyecto B',
            'proyecto_id' => $proyectoB->id,
            'asignado_a' => $user->id,
            'creado_por' => $admin->id,
        ]);

        // Comentario en una tarea (tarea1)
        Comentario::create([
            'texto' => 'Este es un comentario de ejemplo en la tarea 1',
            'tarea_id' => $tarea1->id,
            'usuario_id' => $user->id,
        ]);
    }
}

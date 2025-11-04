<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Support\Facades\Hash;

class Usuario extends Model
{
    use HasFactory;

    protected $table = 'usuarios';

    protected $fillable = [
        'nombre',
        'email',
        'password',
        'activo',
    ];

    protected $hidden = [
        'password',
    ];

    public $timestamps = false; // manejamos fecha_creacion en la migración

    /**
     * Asegura que la contraseña siempre se guarde hasheada.
     */
    public function setPasswordAttribute($value)
    {
        if ($value === null) {
            $this->attributes['password'] = null;
            return;
        }

        // Evita re-hashear si ya está en formato bcrypt (empieza por $2y$ o $2a$)
        if (is_string($value) && (str_starts_with($value, '$2y$') || str_starts_with($value, '$2a$'))) {
            $this->attributes['password'] = $value;
            return;
        }

        $this->attributes['password'] = Hash::make($value);
    }

    /* Relaciones */

    public function roles()
    {
        return $this->belongsToMany(Rol::class, 'usuario_rol', 'usuario_id', 'rol_id')
            ->using(UsuarioRol::class);
    }

    public function proyectos()
    {
        // Proyectos en los que participa (pivot contiene rol_proyecto)
        return $this->belongsToMany(Proyecto::class, 'usuario_proyecto', 'usuario_id', 'proyecto_id')
            ->using(UsuarioProyecto::class)
            ->withPivot('rol_proyecto');
    }

    public function proyectosCreados()
    {
        return $this->hasMany(Proyecto::class, 'creado_por');
    }

    public function tareasAsignadas()
    {
        return $this->hasMany(Tarea::class, 'asignado_a');
    }

    public function tareasCreadas()
    {
        return $this->hasMany(Tarea::class, 'creado_por');
    }

    public function comentarios()
    {
        return $this->hasMany(Comentario::class, 'usuario_id');
    }
}

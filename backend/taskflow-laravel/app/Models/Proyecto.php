<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Proyecto extends Model
{
    use HasFactory;

    protected $table = 'proyectos';

    protected $fillable = [
        'titulo',
        'descripcion',
        'creado_por',
    ];

    public $timestamps = false;

    public function creadoPor()
    {
        return $this->belongsTo(Usuario::class, 'creado_por');
    }

    public function usuarios()
    {
        return $this->belongsToMany(Usuario::class, 'usuario_proyecto', 'proyecto_id', 'usuario_id')
            ->using(UsuarioProyecto::class)
            ->withPivot('rol_proyecto');
    }

    public function tareas()
    {
        return $this->hasMany(Tarea::class, 'proyecto_id');
    }
}

<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Tarea extends Model
{
    use HasFactory;

    protected $table = 'tareas';

    protected $fillable = [
        'titulo',
        'descripcion',
        'fecha_vencimiento',
        'estado',
        'proyecto_id',
        'asignado_a',
        'creado_por',
    ];

    public $timestamps = false;

    public const ESTADOS = [
        'PENDIENTE',
        'PROGRESO',
        'COMPLETADA',
    ];

    public function proyecto()
    {
        return $this->belongsTo(Proyecto::class, 'proyecto_id');
    }

    public function asignadoA()
    {
        return $this->belongsTo(Usuario::class, 'asignado_a');
    }

    public function creador()
    {
        return $this->belongsTo(Usuario::class, 'creado_por');
    }

    public function comentarios()
    {
        return $this->hasMany(Comentario::class, 'tarea_id');
    }
}

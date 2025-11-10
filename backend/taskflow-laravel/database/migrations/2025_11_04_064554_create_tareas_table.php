<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('tareas', function (Blueprint $table) {
            $table->id();
            $table->string('titulo', 150);
            $table->text('descripcion')->nullable();
            $table->date('fecha_vencimiento')->nullable();
            $table->enum('estado', ['PENDIENTE', 'PROGRESO', 'COMPLETADA'])->default('PENDIENTE');
            $table->foreignId('proyecto_id')->constrained('proyectos')->onDelete('cascade');
            $table->foreignId('asignado_a')->nullable()->constrained('usuarios')->nullOnDelete();
            $table->foreignId('creado_por')->constrained('usuarios')->onDelete('cascade');
            $table->timestamp('fecha_creacion')->useCurrent();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('tareas');
    }
};

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
    Schema::create('usuario_proyecto', function (Blueprint $table) {
      $table->unsignedBigInteger('usuario_id');
      $table->unsignedBigInteger('proyecto_id');
      $table->enum('rol_proyecto', ['CREADOR', 'COLABORADOR'])->default('COLABORADOR');

      $table->primary(['usuario_id', 'proyecto_id']);

      $table->foreign('usuario_id')->references('id')->on('usuarios')->onDelete('cascade');
      $table->foreign('proyecto_id')->references('id')->on('proyectos')->onDelete('cascade');
    });
  }

  /**
   * Reverse the migrations.
   */
  public function down(): void
  {
    Schema::dropIfExists('usuario_proyecto');
  }
};

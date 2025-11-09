<?php

namespace App\Providers;

use Illuminate\Foundation\Support\Providers\AuthServiceProvider as ServiceProvider;
use Illuminate\Support\Facades\Gate;

class AuthServiceProvider extends ServiceProvider
{
  /**
   * The policy mappings for the application.
   *
   * @var array<class-string, class-string>
   */
  protected $policies = [
    \App\Models\Proyecto::class => \App\Policies\ProyectoPolicy::class,
    \App\Models\Tarea::class => \App\Policies\TareaPolicy::class,
  ];

  /**
   * Register any authentication / authorization services.
   */
  public function boot(): void
  {
    $this->registerPolicies();
  }
}

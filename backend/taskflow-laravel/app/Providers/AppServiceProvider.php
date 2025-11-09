<?php

namespace App\Providers;

use Illuminate\Support\ServiceProvider;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     */
    public function register(): void
    {
        //
    }

    /**
     * Bootstrap any application services.
     */
    public function boot(): void
    {
        // Registrar aliases de middleware de rutas (role y project.role)
        $router = $this->app->make(\Illuminate\Routing\Router::class);
        $router->aliasMiddleware('role', \App\Http\Middleware\EnsureRole::class);
        $router->aliasMiddleware('project.role', \App\Http\Middleware\EnsureProjectRole::class);
    }
}

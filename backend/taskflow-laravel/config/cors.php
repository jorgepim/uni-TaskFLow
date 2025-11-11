<?php

return [
  /*
    |--------------------------------------------------------------------------
    | Cross-Origin Resource Sharing (CORS) Configuration
    |--------------------------------------------------------------------------
    |
    | Here you may configure your settings for cross-origin resource sharing
    | or "CORS". This determines what cross-origin operations may execute
    | in web browsers. Feel free to adjust these settings as needed.
    |
    */

  'paths' => ['api/*', 'sanctum/csrf-cookie'],

  // Allow any HTTP method
  'allowed_methods' => ['*'],

  // Allow requests from any origin. IMPORTANT: if you use cookie-based
  // authentication (Sanctum SPA), DO NOT set this to ['*'] and enable
  // 'supports_credentials' = true. Instead list specific origins.
  'allowed_origins' => ['*'],

  'allowed_origins_patterns' => [],

  // Allow any header
  'allowed_headers' => ['*'],

  // Expose no additional headers
  'exposed_headers' => [],

  // Cache preflight response for 0 seconds (no cache)
  'max_age' => 0,

  // Do not support credentials when using wildcard origins. Use tokens
  // (Authorization: Bearer) rather than cookie/session auth if you enable '*'.
  'supports_credentials' => false,
];

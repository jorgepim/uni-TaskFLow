<?php // app/Http/Traits/ApiResponseTrait.php
namespace App\Http\Traits;

trait ApiResponseTrait
{
  protected function successResponse($message, $data, $code = 200)
  {
    return response()->json([
      'status' => 'success',
      'message' => $message,
      'timestamp' => now(),
      'data' => $data,
    ], $code);
  }

  protected function errorResponse($message, $code = 400)
  {
    return response()->json([
      'status' => 'error',
      'message' => $message,
      'timestamp' => now(),
    ], $code);
  }
}

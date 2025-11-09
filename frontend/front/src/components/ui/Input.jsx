import React from "react";

/**
 * Componente Input simple y reutilizable.
 * Pasa todas las props al elemento <input> y permite añadir clases adicionales.
 */
export default function Input({ className = "", ...props }) {
  return (
    <input
      {...props}
      className={`bg-bg-light text-text border border-gray-300 dark:border-gray-700 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary ${className}`}
    />
  );
}

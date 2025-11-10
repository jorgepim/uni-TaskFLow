import React from "react";
import { createPortal } from "react-dom";

export default function Modal({ isOpen, onClose, title, children }) {
  if (!isOpen) return null;

  // Close when clicking on backdrop (but not when clicking inside modal content)
  const onBackdropClick = (e) => {
    if (e.target === e.currentTarget) onClose && onClose();
  };

  const modal = (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/70"
      onMouseDown={onBackdropClick}
    >
      <div
        className="w-full max-w-2xl p-6 bg-white dark:bg-gray-800 text-text rounded-lg shadow-lg"
        role="dialog"
        aria-modal="true"
        onMouseDown={(e) => e.stopPropagation()}
      >
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-semibold text-text">{title}</h3>
          <button
            onClick={onClose}
            className="text-text hover:text-text-light"
            aria-label="Cerrar"
          >
            ✕
          </button>
        </div>

        <div>{children}</div>
      </div>
    </div>
  );

  // Render in document.body to avoid stacking context issues
  return typeof document !== "undefined"
    ? createPortal(modal, document.body)
    : modal;
}

// Modal que renderiza mediante portal, cierra al hacer click en el backdrop y evita problemas
// de stacking context/overflow del contenedor padre.

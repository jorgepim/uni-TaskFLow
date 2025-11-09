import React, { useContext } from "react";
import { LoadingContext } from "../../context/LoadingContext";

export default function LoadingOverlay() {
  const { loading } = useContext(LoadingContext);

  if (!loading) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30">
      <div className="bg-white/90 p-6 rounded-xl shadow-lg flex items-center gap-3">
        <div className="animate-spin rounded-full h-8 w-8 border-4 border-indigo-500 border-t-transparent" />
        <div className="text-sm font-medium text-gray-700">Cargando...</div>
      </div>
    </div>
  );
}

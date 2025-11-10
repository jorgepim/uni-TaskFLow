import React, { useEffect, useState, useCallback } from "react";
import Navbar from "../../components/layout/Navbar";
import {
  getMyTareas,
  getUsuarioTareas,
  updateTareaEstado,
} from "../../api/endpoints/tareasApi";
import { getMyProyectos } from "../../api/endpoints/proyectosApi";
import Modal from "../../components/ui/Modal";

export default function Tasks() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [tareas, setTareas] = useState([]);
  const [estadisticas, setEstadisticas] = useState(null);

  const [estadoFilter, setEstadoFilter] = useState("");
  const [proyectoFilter, setProyectoFilter] = useState("");
  const [vencimientoBefore, setVencimientoBefore] = useState("");

  const [proyectos, setProyectos] = useState([]);

  const [isOpen, setIsOpen] = useState(false);
  const [updatingTaskId, setUpdatingTaskId] = useState(null);

  const currentUser = (() => {
    try {
      return JSON.parse(localStorage.getItem("user") || "{}");
    } catch {
      return {};
    }
  })();
  const userName = currentUser?.nombre || currentUser?.name || "Usuario";
  const userId = currentUser?.id;

  const loadProyectos = useCallback(async () => {
    try {
      const res = await getMyProyectos();
      setProyectos(res.data?.data || []);
    } catch (err) {
      void err;
      setProyectos([]);
    }
  }, []);

  const load = useCallback(
    async (params = {}) => {
      setLoading(true);
      setError(null);
      try {
        // prefer explicit usuario endpoint (allows passing user id)
        const res = userId
          ? await getUsuarioTareas(userId, params)
          : await getMyTareas(params);
        const data = res.data?.data || {};
        setTareas(data.tareas || []);
        setEstadisticas(data.estadisticas || null);
      } catch (err) {
        setError(
          err?.response?.data?.message || err.message || "Error cargando tareas"
        );
        setTareas([]);
        setEstadisticas(null);
      } finally {
        setLoading(false);
      }
    },
    [userId]
  );

  const handleChangeEstado = async (tareaId, nuevoEstado) => {
    setUpdatingTaskId(tareaId);
    setError(null);
    try {
      // backend expects { estado }
      await updateTareaEstado(tareaId, nuevoEstado);
      // reload tareas using the usuario endpoint
      await load();
    } catch (err) {
      setError(
        err?.response?.data?.message ||
          err.message ||
          "Error actualizando estado"
      );
    } finally {
      setUpdatingTaskId(null);
    }
  };

  // load on mount
  useEffect(() => {
    void load();
    void loadProyectos();
  }, [load, loadProyectos]);

  const handleFilter = async (e) => {
    e?.preventDefault();
    const params = {};
    if (estadoFilter) params.estado = estadoFilter;
    if (proyectoFilter) params.proyecto_id = proyectoFilter;
    if (vencimientoBefore) params.vencimiento_before = vencimientoBefore;
    await load(params);
  };

  const handleClear = async () => {
    setEstadoFilter("");
    setProyectoFilter("");
    setVencimientoBefore("");
    await load();
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <Navbar brand="Tareas" userName={userName} />
      <main className="p-6 max-w-4xl mx-auto">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Mis tareas
          </h1>
          <button
            onClick={() => window.history.back()}
            className="px-3 py-2 bg-gray-200 dark:bg-gray-700 rounded-md cursor-pointer"
          >
            ← Volver
          </button>
        </div>

        <form
          onSubmit={handleFilter}
          className="grid grid-cols-1 md:grid-cols-4 gap-3 mb-4"
        >
          <div>
            <label className="block text-sm text-gray-600 dark:text-gray-300">
              Estado
            </label>
            <select
              value={estadoFilter}
              onChange={(e) => setEstadoFilter(e.target.value)}
              className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
            >
              <option value="">-- Todos --</option>
              <option value="PENDIENTE">PENDIENTE</option>
              <option value="PROGRESO">PROGRESO</option>
              <option value="COMPLETADA">COMPLETADA</option>
            </select>
          </div>

          <div>
            <label className="block text-sm text-gray-600 dark:text-gray-300">
              Proyecto
            </label>
            <select
              value={proyectoFilter}
              onChange={(e) => setProyectoFilter(e.target.value)}
              className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
            >
              <option value="">-- Todos --</option>
              {proyectos.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.titulo}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm text-gray-600 dark:text-gray-300">
              Vencimiento antes
            </label>
            <input
              type="date"
              value={vencimientoBefore}
              onChange={(e) => setVencimientoBefore(e.target.value)}
              className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
            />
          </div>

          <div className="flex items-end gap-2">
            <button
              type="submit"
              className="px-3 py-2 bg-indigo-600 text-white rounded-md"
            >
              Filtrar
            </button>
            <button
              type="button"
              onClick={handleClear}
              className="px-3 py-2 bg-gray-200 dark:bg-gray-700 rounded-md"
            >
              Limpiar
            </button>
          </div>
        </form>

        {loading && <div>Cargando tareas...</div>}
        {error && <div className="text-red-600">{String(error)}</div>}

        {estadisticas && (
          <div className="mb-4 bg-white dark:bg-gray-800 p-4 rounded-md shadow">
            <div className="flex items-center justify-between">
              <div className="text-sm text-gray-600 dark:text-gray-300">
                Total
              </div>
              <div className="text-xl font-semibold text-gray-900 dark:text-gray-100">
                {estadisticas.total}
              </div>
            </div>

            <div className="grid grid-cols-3 gap-4 mt-4 text-sm">
              {Object.entries(estadisticas.counts || {}).map(([k, v]) => (
                <div
                  key={k}
                  className="p-2 bg-gray-50 dark:bg-gray-700 rounded"
                >
                  <div className="text-xs text-gray-500 dark:text-gray-300">
                    {k}
                  </div>
                  <div className="font-semibold text-gray-900 dark:text-gray-100">
                    {v}
                  </div>
                </div>
              ))}
              <div className="p-2 bg-gray-50 dark:bg-gray-700 rounded">
                <div className="text-xs text-gray-500 dark:text-gray-300">
                  % completadas
                </div>
                <div className="font-semibold text-gray-900 dark:text-gray-100">
                  {estadisticas.porcentaje_completadas}%
                </div>
              </div>
              <div className="p-2 bg-gray-50 dark:bg-gray-700 rounded">
                <div className="text-xs text-gray-500 dark:text-gray-300">
                  % progreso ponderado
                </div>
                <div className="font-semibold text-gray-900 dark:text-gray-100">
                  {estadisticas.porcentaje_progreso_ponderado}%
                </div>
              </div>
            </div>
          </div>
        )}

        <div className="grid grid-cols-1 gap-3">
          {tareas.map((t) => (
            <div
              key={t.id}
              className="bg-white dark:bg-gray-800 p-4 rounded-md shadow"
            >
              <div className="flex items-start justify-between">
                <div>
                  <div className="font-semibold text-lg text-gray-900 dark:text-gray-100">
                    {t.titulo}
                  </div>
                  <div className="text-sm text-gray-600 dark:text-gray-300">
                    {t.descripcion}
                  </div>
                  <div className="text-xs text-gray-500 dark:text-gray-400 mt-2">
                    Vence: {t.fecha_vencimiento}
                  </div>
                </div>
                <div className="text-sm text-gray-700 dark:text-gray-200 text-right">
                  <div className="mb-2">
                    <label className="block text-xs text-gray-500 dark:text-gray-400">
                      Estado
                    </label>
                    <select
                      value={t.estado}
                      onChange={(e) => handleChangeEstado(t.id, e.target.value)}
                      disabled={updatingTaskId === t.id}
                      className="mt-1 w-full px-2 py-1 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 text-sm"
                    >
                      <option value="PENDIENTE">PENDIENTE</option>
                      <option value="PROGRESO">PROGRESO</option>
                      <option value="COMPLETADA">COMPLETADA</option>
                    </select>
                    {updatingTaskId === t.id && (
                      <div className="text-xs text-gray-500 mt-1">
                        Actualizando...
                      </div>
                    )}
                  </div>

                  <div>Proyecto: {t.proyecto_id}</div>
                  <div className="mt-2">
                    Asignado: {t.asignado?.nombre || t.asignado?.name || "-"}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>

        <Modal isOpen={isOpen} onClose={() => setIsOpen(false)} title="">
          <div />
        </Modal>
      </main>
    </div>
  );
}

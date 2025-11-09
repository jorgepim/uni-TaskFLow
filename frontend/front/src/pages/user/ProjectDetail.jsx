import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Navbar from "../../components/layout/Navbar";
import {
  getProyectoById,
  deleteProyecto,
} from "../../api/endpoints/proyectosApi";
import {
  createTarea,
  updateTarea,
  updateTareaEstado,
  deleteTarea,
} from "../../api/endpoints/tareasApi";

export default function ProjectDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [project, setProject] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [newTaskTitle, setNewTaskTitle] = useState("");

  const currentUser = (() => {
    try {
      return JSON.parse(localStorage.getItem("user") || "{}");
    } catch {
      return {};
    }
  })();

  const currentUserId = currentUser?.id;

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await getProyectoById(id);
      setProject(res.data?.data || null);
    } catch (err) {
      setError(
        err?.response?.data?.message || err.message || "Error cargando proyecto"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const isCreador = project?.rol_proyecto === "CREADOR";
  const isColaborador = project?.rol_proyecto === "COLABORADOR";

  const handleCreateTask = async () => {
    if (!newTaskTitle.trim()) return;
    setLoading(true);
    try {
      await createTarea(id, { titulo: newTaskTitle });
      setNewTaskTitle("");
      await load();
    } catch (err) {
      setError(
        err?.response?.data?.message || err.message || "Error creando tarea"
      );
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateTaskState = async (tareaId, newEstado) => {
    try {
      // use the dedicated patch endpoint for estado
      await updateTareaEstado(tareaId, newEstado);
      await load();
    } catch (err) {
      setError(
        err?.response?.data?.message ||
          err.message ||
          "Error actualizando tarea"
      );
    }
  };

  const handleDeleteTask = async (tareaId) => {
    if (!confirm("¿Eliminar tarea?")) return;
    try {
      await deleteTarea(tareaId);
      await load();
    } catch (err) {
      setError(
        err?.response?.data?.message || err.message || "Error eliminando tarea"
      );
    }
  };

  const handleDeleteProject = async () => {
    if (!confirm("¿Eliminar proyecto? Esta acción no se puede deshacer."))
      return;
    try {
      await deleteProyecto(id);
      navigate("/user/dashboard", { replace: true });
    } catch (err) {
      setError(
        err?.response?.data?.message ||
          err.message ||
          "Error eliminando proyecto"
      );
    }
  };

  const estados = ["PENDIENTE", "PROGRESO", "COMPLETADA"];

  const grouped = { PENDIENTE: [], PROGRESO: [], COMPLETADA: [] };
  (project?.tareas || []).forEach((t) => {
    if (!grouped[t.estado]) grouped[t.estado] = [];
    grouped[t.estado].push(t);
  });

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <Navbar
        brand="Proyecto"
        userName={currentUser?.nombre || currentUser?.name}
      />
      <main className="p-6">
        {loading && <div>Cargando...</div>}
        {error && <div className="text-red-600">{String(error)}</div>}

        {project && (
          <div>
            <div className="flex items-center justify-between mb-6">
              <div>
                <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                  {project.titulo}
                </h1>
                <p className="text-sm text-gray-600 dark:text-gray-300">
                  {project.descripcion}
                </p>
                <div className="text-xs text-gray-500 dark:text-gray-400 mt-2">
                  Creado por:{" "}
                  {project.creado_por?.nombre ||
                    project.creado_por?.name ||
                    project.creado_por?.id}
                </div>
              </div>
              <div className="flex items-center gap-2">
                <div className="text-sm text-gray-700 dark:text-gray-200">
                  Rol: {project.rol_proyecto}
                </div>
                {isCreador && (
                  <>
                    <button
                      onClick={handleDeleteProject}
                      className="px-3 py-2 bg-red-600 text-white rounded-md"
                    >
                      Eliminar proyecto
                    </button>
                  </>
                )}
              </div>
            </div>

            {/* Trello-like columns */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {estados.map((estado) => (
                <div
                  key={estado}
                  className="bg-white dark:bg-gray-800 rounded-md p-4 shadow"
                >
                  <h3 className="font-semibold mb-3">
                    {estado} ({grouped[estado]?.length || 0})
                  </h3>
                  <div className="space-y-3">
                    {grouped[estado] && grouped[estado].length > 0 ? (
                      grouped[estado].map((t) => (
                        <div
                          key={t.id}
                          className="p-3 rounded-md bg-gray-50 dark:bg-gray-700/60"
                        >
                          <div className="flex items-start justify-between">
                            <div>
                              <div className="font-medium text-gray-900 dark:text-gray-50">
                                {t.titulo}
                              </div>
                              <div className="text-xs text-gray-600 dark:text-gray-300">
                                Asignado:{" "}
                                {t.asignado?.nombre ||
                                  t.asignado?.name ||
                                  "Sin asignar"}
                              </div>
                            </div>
                            <div className="flex flex-col items-end gap-2">
                              {/* Estado selector */}
                              <select
                                value={t.estado}
                                onChange={(e) => {
                                  const newEstado = e.target.value;
                                  const canEdit =
                                    isCreador ||
                                    (isColaborador &&
                                      t.asignado?.id === currentUserId);
                                  if (!canEdit) return;
                                  void handleUpdateTaskState(t.id, newEstado);
                                }}
                                className="px-2 py-1 rounded-md border bg-white dark:bg-gray-800 text-sm"
                                disabled={
                                  !(
                                    isCreador ||
                                    (isColaborador &&
                                      t.asignado?.id === currentUserId)
                                  )
                                }
                              >
                                {estados.map((s) => (
                                  <option key={s} value={s}>
                                    {s}
                                  </option>
                                ))}
                              </select>

                              <div className="flex gap-2">
                                {isCreador && (
                                  <button
                                    onClick={() => {
                                      const newTitle = prompt(
                                        "Editar título",
                                        t.titulo
                                      );
                                      if (
                                        newTitle &&
                                        newTitle.trim() &&
                                        newTitle !== t.titulo
                                      ) {
                                        void updateTarea(t.id, {
                                          titulo: newTitle,
                                        }).then(() => load());
                                      }
                                    }}
                                    className="text-sm text-indigo-600"
                                  >
                                    ✏️
                                  </button>
                                )}
                                {isCreador && (
                                  <button
                                    onClick={() => void handleDeleteTask(t.id)}
                                    className="text-sm text-red-600"
                                  >
                                    🗑️
                                  </button>
                                )}
                              </div>
                            </div>
                          </div>
                        </div>
                      ))
                    ) : (
                      <div className="text-sm text-gray-500">No hay tareas</div>
                    )}
                  </div>
                </div>
              ))}
            </div>

            {/* create task for creator */}
            {isCreador && (
              <div className="mt-6">
                <h4 className="font-semibold mb-2">Crear tarea</h4>
                <div className="flex gap-2">
                  <input
                    value={newTaskTitle}
                    onChange={(e) => setNewTaskTitle(e.target.value)}
                    className="px-3 py-2 rounded-md border w-full"
                    placeholder="Título de la tarea"
                  />
                  <button
                    onClick={handleCreateTask}
                    className="px-4 py-2 bg-indigo-600 text-white rounded-md"
                  >
                    Crear
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
}

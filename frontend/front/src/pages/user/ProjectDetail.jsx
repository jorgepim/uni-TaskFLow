import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Navbar from "../../components/layout/Navbar";
import {
  getProyectoById,
  deleteProyecto,
  getUsuariosNoAsignados,
  assignUsuarioProyecto,
  getUsuariosAsignados,
  deleteUsuarioProyecto,
} from "../../api/endpoints/proyectosApi";
import Modal from "../../components/ui/Modal";
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
  // assign users modal state
  const [isAssignOpen, setIsAssignOpen] = useState(false);
  const [searchNombre, setSearchNombre] = useState("");
  const [searchEmail, setSearchEmail] = useState("");
  const [availableUsers, setAvailableUsers] = useState([]);
  const [searchingUsers, setSearchingUsers] = useState(false);
  const [assigning, setAssigning] = useState(false);
  const [assignError, setAssignError] = useState(null);
  const [assignSuccess, setAssignSuccess] = useState(null);
  // assigned users modal state
  const [isAssignedOpen, setIsAssignedOpen] = useState(false);
  const [assignedFilterName, setAssignedFilterName] = useState("");
  const [assignedUsers, setAssignedUsers] = useState([]);
  const [loadingAssignedUsers, setLoadingAssignedUsers] = useState(false);
  const [deletingAssignedUserId, setDeletingAssignedUserId] = useState(null);

  const currentUser = (() => {
    try {
      return JSON.parse(localStorage.getItem("user") || "{}");
    } catch {
      return {};
    }
  })();

  const currentUserId = currentUser?.id;

  // fetch available (no-asignados) users
  const fetchAvailableUsers = async (filters = {}) => {
    setSearchingUsers(true);
    setAssignError(null);
    try {
      const res = await getUsuariosNoAsignados(id, filters);
      setAvailableUsers(res.data?.data || []);
    } catch (err) {
      void err;
      setAvailableUsers([]);
    } finally {
      setSearchingUsers(false);
    }
  };

  // fetch assigned users
  const fetchAssignedUsers = async (filters = {}) => {
    setLoadingAssignedUsers(true);
    try {
      const res = await getUsuariosAsignados(id, filters);
      setAssignedUsers(res.data?.data || []);
    } catch (err) {
      void err;
      setAssignedUsers([]);
    } finally {
      setLoadingAssignedUsers(false);
    }
  };

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
                    <button
                      onClick={async () => {
                        setIsAssignOpen(true);
                        setAvailableUsers([]);
                        setSearchNombre("");
                        setSearchEmail("");
                        setAssignError(null);
                        setAssignSuccess(null);
                        // preload available users
                        await fetchAvailableUsers({ nombre: "", email: "" });
                      }}
                      className="px-3 py-2 bg-green-600 text-white rounded-md"
                    >
                      Asignar usuarios
                    </button>
                  </>
                )}
                <button
                  onClick={async () => {
                    setIsAssignedOpen(true);
                    setAssignedUsers([]);
                    setAssignedFilterName("");
                    // preload assigned users
                    await fetchAssignedUsers({ nombre: "" });
                  }}
                  className="px-3 py-2 bg-blue-600 text-white rounded-md"
                >
                  Ver usuarios
                </button>
              </div>
            </div>

            {/* Trello-like columns */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {estados.map((estado) => (
                <div
                  key={estado}
                  className="bg-white dark:bg-gray-800 rounded-md p-4 shadow"
                >
                  <h3 className="dark:text-gray-100 font-semibold mb-3">
                    {estado} ({grouped[estado]?.length || 0})
                  </h3>
                  <div className="dark:text-gray-100 space-y-3">
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
                <h4 className="dark:text-gray-100 font-semibold mb-2">
                  Crear tarea
                </h4>
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
            {/* Assign users modal */}
            <Modal
              isOpen={isAssignOpen}
              onClose={() => {
                if (!assigning) {
                  setIsAssignOpen(false);
                  setAvailableUsers([]);
                  setSearchNombre("");
                  setSearchEmail("");
                  setAssignError(null);
                  setAssignSuccess(null);
                }
              }}
              title={`Asignar usuarios a ${project?.titulo}`}
            >
              <div className="space-y-3">
                {assignError && (
                  <div className="text-red-600">{assignError}</div>
                )}
                {assignSuccess && (
                  <div className="text-green-600">{assignSuccess}</div>
                )}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                  <div>
                    <label className="block text-sm text-gray-600 dark:text-gray-300">
                      Nombre
                    </label>
                    <input
                      value={searchNombre}
                      onChange={(e) => setSearchNombre(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === "Enter") {
                          e.preventDefault();
                          void fetchAvailableUsers({
                            nombre: searchNombre,
                            email: searchEmail,
                          });
                        }
                      }}
                      className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                      placeholder="Filtrar por nombre"
                    />
                  </div>
                  <div>
                    <label className="block text-sm text-gray-600 dark:text-gray-300">
                      Email
                    </label>
                    <input
                      value={searchEmail}
                      onChange={(e) => setSearchEmail(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === "Enter") {
                          e.preventDefault();
                          void fetchAvailableUsers({
                            nombre: searchNombre,
                            email: searchEmail,
                          });
                        }
                      }}
                      className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                      placeholder="Filtrar por email"
                    />
                  </div>
                </div>
                <div className="flex justify-end gap-2">
                  <button
                    onClick={() => {
                      setAvailableUsers([]);
                      setAssignError(null);
                      setAssignSuccess(null);
                    }}
                    className="px-3 py-2 bg-gray-200 dark:bg-gray-700 rounded-md"
                  >
                    Limpiar
                  </button>
                  <button
                    onClick={async () => {
                      setAssignError(null);
                      setAssignSuccess(null);
                      await fetchAvailableUsers({
                        nombre: searchNombre,
                        email: searchEmail,
                      });
                    }}
                    className="px-3 py-2 bg-indigo-600 text-white rounded-md"
                    disabled={searchingUsers}
                  >
                    {searchingUsers ? "Buscando..." : "Buscar usuarios"}
                  </button>
                </div>

                <div className="mt-3 space-y-2 max-h-64 overflow-auto">
                  {availableUsers.length === 0 && (
                    <div className="text-sm text-gray-500">
                      No hay usuarios disponibles
                    </div>
                  )}
                  {availableUsers.map((u) => (
                    <div
                      key={u.id}
                      className="flex items-center justify-between p-2 bg-gray-50 dark:bg-gray-800 rounded"
                    >
                      <div>
                        <div className="font-medium text-gray-900 dark:text-gray-100">
                          {u.nombre}
                        </div>
                        <div className="text-sm text-gray-600 dark:text-gray-300">
                          {u.email}
                        </div>
                      </div>
                      <div>
                        <button
                          onClick={async () => {
                            setAssignError(null);
                            setAssignSuccess(null);
                            setAssigning(true);
                            try {
                              await assignUsuarioProyecto(id, {
                                usuarioId: u.id,
                                rolProyecto: "COLABORADOR",
                              });
                              setAssignSuccess(
                                "Usuario asignado correctamente"
                              );
                              // remove user from list
                              setAvailableUsers((prev) =>
                                prev.filter((x) => x.id !== u.id)
                              );
                              await load();
                            } catch (err) {
                              setAssignError(
                                err?.response?.data?.message ||
                                  err.message ||
                                  "Error asignando usuario"
                              );
                            } finally {
                              setAssigning(false);
                            }
                          }}
                          className="px-3 py-1 bg-green-600 text-white rounded-md"
                          disabled={assigning}
                        >
                          {assigning ? "Asignando..." : "Asignar"}
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </Modal>
            {/* Assigned users modal */}
            <Modal
              isOpen={isAssignedOpen}
              onClose={() => {
                if (!deletingAssignedUserId) {
                  setIsAssignedOpen(false);
                  setAssignedUsers([]);
                  setAssignedFilterName("");
                }
              }}
              title={`Usuarios asignados a ${project?.titulo}`}
            >
              <div className="space-y-3">
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-300">
                    Filtrar por nombre
                  </label>
                  <div className="flex gap-2 mt-1">
                    <input
                      value={assignedFilterName}
                      onChange={(e) => setAssignedFilterName(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === "Enter") {
                          e.preventDefault();
                          void fetchAssignedUsers({
                            nombre: assignedFilterName,
                          });
                        }
                      }}
                      className="w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                      placeholder="Nombre"
                    />
                    <button
                      onClick={async () => {
                        await fetchAssignedUsers({
                          nombre: assignedFilterName,
                        });
                      }}
                      className="px-3 py-2 bg-indigo-600 text-white rounded-md"
                    >
                      Buscar
                    </button>
                  </div>
                </div>

                <div className="max-h-64 overflow-auto space-y-2">
                  {loadingAssignedUsers && <div>Buscando...</div>}
                  {assignedUsers.length === 0 && !loadingAssignedUsers && (
                    <div className="text-sm text-gray-500">
                      No hay usuarios asignados
                    </div>
                  )}
                  {assignedUsers.map((u) => (
                    <div
                      key={u.id}
                      className="flex items-center justify-between p-2 bg-gray-50 dark:bg-gray-800 rounded"
                    >
                      <div>
                        <div className="font-medium text-gray-900 dark:text-gray-100">
                          {u.nombre}
                        </div>
                        <div className="text-sm text-gray-600 dark:text-gray-300">
                          {u.email}
                        </div>
                        <div className="text-xs text-gray-500 dark:text-gray-400">
                          Rol en proyecto: {u.rol_proyecto}
                        </div>
                      </div>
                      <div className="flex gap-2">
                        {isCreador && u.id !== project?.creado_por?.id && (
                          <button
                            onClick={async () => {
                              if (
                                !confirm(`Eliminar asignación de ${u.nombre}?`)
                              )
                                return;
                              setDeletingAssignedUserId(u.id);
                              try {
                                await deleteUsuarioProyecto(id, u.id);
                                setAssignedUsers((prev) =>
                                  prev.filter((x) => x.id !== u.id)
                                );
                                await load();
                              } catch (err) {
                                void err;
                              } finally {
                                setDeletingAssignedUserId(null);
                              }
                            }}
                            className="px-2 py-1 bg-red-600 text-white rounded-md"
                            disabled={deletingAssignedUserId === u.id}
                          >
                            {deletingAssignedUserId === u.id
                              ? "Eliminando..."
                              : "Eliminar"}
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </Modal>
          </div>
        )}
      </main>
    </div>
  );
}

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
  updateTareaEstado,
  deleteTarea,
  patchTarea,
} from "../../api/endpoints/tareasApi";
import { createTareaGlobal } from "../../api/endpoints/tareasApi";
import { getTareaById } from "../../api/endpoints/tareasApi";
import {
  createComentario,
  patchComentario,
  deleteComentario,
} from "../../api/endpoints/comentariosApi";

export default function ProjectDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [project, setProject] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // create task modal state
  const [isCreateTaskOpen, setIsCreateTaskOpen] = useState(false);
  const [taskTitulo, setTaskTitulo] = useState("");
  const [taskDescripcion, setTaskDescripcion] = useState("");
  const [taskFechaVenc, setTaskFechaVenc] = useState("");
  const [taskEstado, setTaskEstado] = useState("PENDIENTE");
  const [taskAsignadoId, setTaskAsignadoId] = useState(null);
  const [creatingTask, setCreatingTask] = useState(false);
  const [createTaskError, setCreateTaskError] = useState(null);
  // assign users modal state
  const [isAssignOpen, setIsAssignOpen] = useState(false);
  const [searchNombre, setSearchNombre] = useState("");
  const [searchEmail, setSearchEmail] = useState("");
  const [availableUsers, setAvailableUsers] = useState([]);
  const [searchingUsers, setSearchingUsers] = useState(false);
  const [assigning, setAssigning] = useState(false);
  const [assignError, setAssignError] = useState(null);
  const [assignSuccess, setAssignSuccess] = useState(null);
  // task detail modal + comments
  const [isTaskOpen, setIsTaskOpen] = useState(false);
  const [taskDetail, setTaskDetail] = useState(null);
  const [loadingTaskDetail, setLoadingTaskDetail] = useState(false);
  const [commentText, setCommentText] = useState("");
  const [postingComment, setPostingComment] = useState(false);
  const [commentError, setCommentError] = useState(null);
  // inline edit comment state
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editingCommentText, setEditingCommentText] = useState("");
  const [editingCommentLoading, setEditingCommentLoading] = useState(false);
  const [editingCommentError, setEditingCommentError] = useState(null);
  const [deletingCommentId, setDeletingCommentId] = useState(null);
  // edit task modal state
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [editTaskId, setEditTaskId] = useState(null);
  const [editTitulo, setEditTitulo] = useState("");
  const [editDescripcion, setEditDescripcion] = useState("");
  const [editFechaVenc, setEditFechaVenc] = useState("");
  const [editAsignadoId, setEditAsignadoId] = useState(null);
  const [editingTask, setEditingTask] = useState(false);
  const [editError, setEditError] = useState(null);
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
    setCreateTaskError(null);
    if (!taskTitulo.trim()) {
      setCreateTaskError("El título es requerido");
      return;
    }
    setCreatingTask(true);
    try {
      const payload = {
        titulo: taskTitulo,
        descripcion: taskDescripcion,
        fechaVencimiento: taskFechaVenc || null,
        estado: taskEstado,
        proyectoId: id,
        asignadoA: taskAsignadoId || null,
      };
      await createTareaGlobal(payload);
      // close modal and refresh
      setIsCreateTaskOpen(false);
      setTaskTitulo("");
      setTaskDescripcion("");
      setTaskFechaVenc("");
      setTaskEstado("PENDIENTE");
      setTaskAsignadoId(null);
      await load();
    } catch (err) {
      setCreateTaskError(
        err?.response?.data?.message || err.message || "Error creando tarea"
      );
    } finally {
      setCreatingTask(false);
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

  const openTaskModal = async (tareaId) => {
    setLoadingTaskDetail(true);
    setCommentError(null);
    try {
      const res = await getTareaById(tareaId);
      const data = res.data?.data || null;
      setTaskDetail(data);
      setIsTaskOpen(true);
    } catch (err) {
      setCommentError(
        err?.response?.data?.message || err.message || "Error cargando tarea"
      );
    } finally {
      setLoadingTaskDetail(false);
    }
  };

  const closeTaskModal = () => {
    if (postingComment) return;
    setIsTaskOpen(false);
    setTaskDetail(null);
    setCommentText("");
    setCommentError(null);
  };

  const formatDate = (raw) => {
    if (!raw) return "";
    try {
      const d = new Date(raw);
      if (isNaN(d.getTime())) return String(raw);
      return d.toLocaleString();
    } catch {
      return String(raw);
    }
  };

  const handlePostComment = async () => {
    if (!taskDetail) return;
    if (!commentText.trim()) {
      setCommentError("El comentario no puede estar vacío");
      return;
    }
    setPostingComment(true);
    setCommentError(null);
    try {
      const payload = { texto: commentText, tareaId: taskDetail.id };
      await createComentario(payload);
      // After creating a comment, reload the tarea from the API to get canonical data
      const tareaRes = await getTareaById(taskDetail.id);
      const fresh = tareaRes.data?.data || null;
      setTaskDetail(fresh);
      setCommentText("");
    } catch (err) {
      setCommentError(
        err?.response?.data?.message ||
          err.message ||
          "Error enviando comentario"
      );
    } finally {
      setPostingComment(false);
    }
  };

  const startEditComment = (c) => {
    setEditingCommentId(c.id);
    setEditingCommentText(c.texto || c.text || "");
    setEditingCommentError(null);
  };

  const cancelEditComment = () => {
    if (editingCommentLoading) return;
    setEditingCommentId(null);
    setEditingCommentText("");
    setEditingCommentError(null);
  };

  const saveEditComment = async () => {
    if (!editingCommentText || !editingCommentText.trim()) {
      setEditingCommentError("El comentario no puede estar vacío");
      return;
    }
    setEditingCommentLoading(true);
    setEditingCommentError(null);
    try {
      await patchComentario(editingCommentId, { texto: editingCommentText });
      // reload tarea to get canonical comentarios
      const tareaRes = await getTareaById(taskDetail.id);
      const fresh = tareaRes.data?.data || null;
      setTaskDetail(fresh);
      // clear edit state
      setEditingCommentId(null);
      setEditingCommentText("");
    } catch (err) {
      setEditingCommentError(
        err?.response?.data?.message ||
          err.message ||
          "Error actualizando comentario"
      );
    } finally {
      setEditingCommentLoading(false);
    }
  };

  const handleDeleteComment = async (commentId) => {
    if (!confirm("¿Eliminar comentario?")) return;
    setDeletingCommentId(commentId);
    try {
      await deleteComentario(commentId);
      // reload tarea to get canonical comentarios
      const tareaRes = await getTareaById(taskDetail.id);
      const fresh = tareaRes.data?.data || null;
      setTaskDetail(fresh);
    } catch (err) {
      // show a small inline error near comments area
      setCommentError(
        err?.response?.data?.message ||
          err.message ||
          "Error eliminando comentario"
      );
    } finally {
      setDeletingCommentId(null);
    }
  };

  const openEditModal = async (t) => {
    // Fetch canonical tarea from API to ensure we have full fields (descripcion, etc.)
    setEditTaskId(t.id);
    setEditError(null);
    try {
      const res = await getTareaById(t.id);
      const tarea = res.data?.data || t;
      setEditTitulo(tarea.titulo || tarea.title || "");
      setEditDescripcion(
        tarea.descripcion ||
          tarea.description ||
          tarea.texto ||
          tarea.text ||
          ""
      );
      setEditFechaVenc(tarea.fecha_vencimiento || tarea.fechaVencimiento || "");
      setEditAsignadoId(tarea.asignado?.id || null);
    } catch (err) {
      void err;
      // fallback to the provided object if the fetch fails
      setEditTitulo(t.titulo || "");
      setEditDescripcion(
        t.descripcion || t.description || t.texto || t.text || ""
      );
      setEditFechaVenc(t.fecha_vencimiento || t.fechaVencimiento || "");
      setEditAsignadoId(t.asignado?.id || null);
    }
    // preload assigned users for select options
    await fetchAssignedUsers({ nombre: "" });
    setIsEditOpen(true);
  };

  const handleSaveEdit = async () => {
    if (!editTitulo.trim()) {
      setEditError("El título es requerido");
      return;
    }
    setEditingTask(true);
    setEditError(null);
    try {
      const payload = {
        titulo: editTitulo,
        descripcion: editDescripcion,
        fechaVencimiento: editFechaVenc || null,
        asignadoA: editAsignadoId || null,
      };
      await patchTarea(editTaskId, payload);
      setIsEditOpen(false);
      setEditTaskId(null);
      setEditTitulo("");
      setEditDescripcion("");
      setEditFechaVenc("");
      setEditAsignadoId(null);
      // reload project and task detail if open
      await load();
      if (taskDetail?.id === editTaskId) {
        const tareaRes = await getTareaById(editTaskId);
        setTaskDetail(tareaRes.data?.data || null);
      }
    } catch (err) {
      setEditError(
        err?.response?.data?.message ||
          err.message ||
          "Error actualizando tarea"
      );
    } finally {
      setEditingTask(false);
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
                <div className="flex items-center gap-3">
                  <button
                    onClick={() => window.history.back()}
                    className="px-2 py-1 bg-gray-200 dark:bg-gray-700 rounded-md"
                  >
                    ← Volver
                  </button>
                  <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                    {project.titulo}
                  </h1>
                </div>
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
                    <button
                      onClick={async () => {
                        // open create-task modal and preload assigned users
                        setIsCreateTaskOpen(true);
                        setTaskTitulo("");
                        setTaskDescripcion("");
                        setTaskFechaVenc("");
                        setTaskEstado("PENDIENTE");
                        setTaskAsignadoId(null);
                        await fetchAssignedUsers({ nombre: "" });
                      }}
                      className="px-3 py-2 bg-indigo-600 text-white rounded-md"
                    >
                      + Crear tarea
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
                          role="button"
                          tabIndex={0}
                          onClick={() => void openTaskModal(t.id)}
                          onKeyDown={(e) => {
                            if (e.key === "Enter" || e.key === " ") {
                              e.preventDefault();
                              void openTaskModal(t.id);
                            }
                          }}
                          className="p-3 rounded-md bg-gray-50 dark:bg-gray-700/60 cursor-pointer"
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
                                onClick={(e) => e.stopPropagation()}
                                value={t.estado}
                                onChange={(e) => {
                                  const newEstado = e.target.value;
                                  // if tarea is COMPLETADA, only CREADOR can change it
                                  if (t.estado === "COMPLETADA" && !isCreador)
                                    return;
                                  const canEdit =
                                    isCreador ||
                                    (isColaborador &&
                                      t.asignado?.id === currentUserId);
                                  if (!canEdit) return;
                                  void handleUpdateTaskState(t.id, newEstado);
                                }}
                                className="px-2 py-1 rounded-md border bg-white dark:bg-gray-800 text-sm"
                                disabled={
                                  // disabled if tarea ya está completada y el usuario no es creador
                                  t.estado === "COMPLETADA"
                                    ? !isCreador
                                    : !(
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
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      // open full edit modal
                                      openEditModal(t);
                                    }}
                                    className="text-sm text-indigo-600 cursor-pointer"
                                  >
                                    ✏️
                                  </button>
                                )}
                                {isCreador && (
                                  <button
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      void handleDeleteTask(t.id);
                                    }}
                                    className="text-sm text-red-600 cursor-pointer"
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

            {/* create task modal trigger moved to header */}
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
            {/* Task detail modal with comentarios */}
            <Modal
              isOpen={isTaskOpen}
              onClose={() => {
                closeTaskModal();
              }}
              title={taskDetail?.titulo || "Detalle tarea"}
            >
              <div className="space-y-3">
                {loadingTaskDetail && <div>Cargando tarea...</div>}
                {commentError && (
                  <div className="text-red-600">{String(commentError)}</div>
                )}

                {!loadingTaskDetail && taskDetail && (
                  <div>
                    <div className="mb-2">
                      <div className="text-sm text-gray-600 dark:text-gray-300">
                        Descripción
                      </div>
                      <div className="text-gray-900 dark:text-gray-100">
                        {taskDetail.descripcion || "-"}
                      </div>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-2 mb-3 text-sm text-gray-600 dark:text-gray-300">
                      <div>
                        Estado:{" "}
                        <span className="text-gray-900 dark:text-gray-100">
                          {taskDetail.estado}
                        </span>
                      </div>
                      <div>
                        Asignado:{" "}
                        <span className="text-gray-900 dark:text-gray-100">
                          {taskDetail.asignado?.nombre || "Sin asignar"}
                        </span>
                      </div>
                      <div>
                        Fecha venc.:{" "}
                        <span className="text-gray-900 dark:text-gray-100">
                          {taskDetail.fecha_vencimiento ||
                            taskDetail.fechaVencimiento ||
                            "-"}
                        </span>
                      </div>
                    </div>

                    <div className="mb-3">
                      <div className="font-semibold dark:text-gray-100">
                        Comentarios
                      </div>
                      <div className="max-h-48 overflow-auto mt-2 space-y-2">
                        {(!taskDetail.comentarios ||
                          taskDetail.comentarios.length === 0) && (
                          <div className="text-sm text-gray-500">
                            No hay comentarios
                          </div>
                        )}
                        {(taskDetail.comentarios || []).map((c) => {
                          const author =
                            c.usuario?.nombre ||
                            c.usuario?.name ||
                            c.usuario?.email ||
                            "Usuario";
                          const created =
                            c.created_at ||
                            c.createdAt ||
                            c.createdAtRaw ||
                            c.fecha ||
                            "";
                          const isOwnComment =
                            (c.usuario && c.usuario.id) === currentUserId ||
                            c.usuario_id === currentUserId;
                          return (
                            <div
                              key={c.id}
                              className="p-2 bg-gray-50 dark:bg-gray-800 rounded"
                            >
                              <div className="text-xs text-gray-500 dark:text-gray-400 flex items-center justify-between gap-2">
                                <div>
                                  <span className="font-medium text-gray-800 dark:text-gray-100">
                                    {author}
                                  </span>
                                  <span className="ml-2">•</span>
                                  <span className="ml-2">
                                    {formatDate(created)}
                                  </span>
                                </div>
                                <div className="flex items-center gap-2">
                                  {isOwnComment &&
                                    editingCommentId !== c.id && (
                                      <>
                                        <button
                                          onClick={(e) => {
                                            e.stopPropagation();
                                            startEditComment(c);
                                          }}
                                          className="text-xs text-indigo-600"
                                        >
                                          Editar
                                        </button>
                                        <button
                                          onClick={async (e) => {
                                            e.stopPropagation();
                                            if (deletingCommentId === c.id)
                                              return;
                                            await handleDeleteComment(c.id);
                                          }}
                                          className="text-xs text-red-600"
                                          disabled={deletingCommentId === c.id}
                                        >
                                          {deletingCommentId === c.id
                                            ? "Eliminando..."
                                            : "Eliminar"}
                                        </button>
                                      </>
                                    )}
                                </div>
                              </div>

                              {editingCommentId === c.id ? (
                                <div className="mt-2">
                                  <textarea
                                    value={editingCommentText}
                                    onChange={(e) =>
                                      setEditingCommentText(e.target.value)
                                    }
                                    className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                                  />
                                  {editingCommentError && (
                                    <div className="text-red-600 mt-1">
                                      {editingCommentError}
                                    </div>
                                  )}
                                  <div className="flex justify-end gap-2 mt-2">
                                    <button
                                      onClick={() => cancelEditComment()}
                                      className="px-3 py-2 bg-gray-200 dark:bg-gray-700 rounded-md"
                                      disabled={editingCommentLoading}
                                    >
                                      Cancelar
                                    </button>
                                    <button
                                      onClick={() => void saveEditComment()}
                                      className="px-3 py-2 bg-indigo-600 text-white rounded-md"
                                      disabled={editingCommentLoading}
                                    >
                                      {editingCommentLoading
                                        ? "Guardando..."
                                        : "Guardar"}
                                    </button>
                                  </div>
                                </div>
                              ) : (
                                <div className="mt-1 text-sm text-gray-900 dark:text-gray-100">
                                  {c.texto || c.text || ""}
                                </div>
                              )}
                            </div>
                          );
                        })}
                      </div>
                    </div>

                    <div>
                      <label className="block text-sm text-gray-600 dark:text-gray-300">
                        Agregar comentario
                      </label>
                      <textarea
                        value={commentText}
                        onChange={(e) => setCommentText(e.target.value)}
                        className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                        placeholder="Escribe tu comentario..."
                      />
                      <div className="flex justify-end gap-2 mt-2">
                        <button
                          onClick={() => {
                            setCommentText("");
                            setCommentError(null);
                          }}
                          className="px-3 py-2 bg-gray-200 dark:bg-gray-700 rounded-md"
                          disabled={postingComment}
                        >
                          Limpiar
                        </button>
                        <button
                          onClick={() => void handlePostComment()}
                          className="px-3 py-2 bg-indigo-600 text-white rounded-md"
                          disabled={postingComment}
                        >
                          {postingComment ? "Enviando..." : "Enviar comentario"}
                        </button>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </Modal>
            {/* Edit task modal */}
            <Modal
              isOpen={isEditOpen}
              onClose={() => {
                if (!editingTask) {
                  setIsEditOpen(false);
                  setEditTaskId(null);
                  setEditTitulo("");
                  setEditDescripcion("");
                  setEditFechaVenc("");
                  setEditAsignadoId(null);
                  setEditError(null);
                }
              }}
              title={editTitulo ? `Editar: ${editTitulo}` : "Editar tarea"}
            >
              <div className="space-y-3">
                {editError && <div className="text-red-600">{editError}</div>}
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-300">
                    Título
                  </label>
                  <input
                    value={editTitulo}
                    onChange={(e) => setEditTitulo(e.target.value)}
                    className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                  />
                </div>
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-300">
                    Descripción
                  </label>
                  <textarea
                    value={editDescripcion}
                    onChange={(e) => setEditDescripcion(e.target.value)}
                    className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                  />
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                  <div>
                    <label className="block text-sm text-gray-600 dark:text-gray-300">
                      Fecha vencimiento
                    </label>
                    <input
                      type="date"
                      value={editFechaVenc}
                      onChange={(e) => setEditFechaVenc(e.target.value)}
                      className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                    />
                  </div>
                  <div>
                    <label className="block text-sm text-gray-600 dark:text-gray-300">
                      Asignado a
                    </label>
                    <select
                      value={editAsignadoId || ""}
                      onChange={(e) =>
                        setEditAsignadoId(
                          e.target.value ? Number(e.target.value) : null
                        )
                      }
                      className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                    >
                      <option value="">-- Sin asignar --</option>
                      {assignedUsers.map((u) => (
                        <option key={u.id} value={u.id}>
                          {u.nombre} ({u.email})
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="flex justify-end gap-2">
                  <button
                    onClick={() => {
                      if (!editingTask) {
                        setIsEditOpen(false);
                        setEditTaskId(null);
                        setEditTitulo("");
                        setEditDescripcion("");
                        setEditFechaVenc("");
                        setEditAsignadoId(null);
                        setEditError(null);
                      }
                    }}
                    className="px-3 py-2 bg-gray-200 dark:bg-gray-700 rounded-md"
                    disabled={editingTask}
                  >
                    Cancelar
                  </button>
                  <button
                    onClick={() => void handleSaveEdit()}
                    className="px-3 py-2 bg-indigo-600 text-white rounded-md"
                    disabled={editingTask}
                  >
                    {editingTask ? "Guardando..." : "Guardar cambios"}
                  </button>
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

            {/* Create task modal */}
            <Modal
              isOpen={isCreateTaskOpen}
              onClose={() => {
                if (!creatingTask) {
                  setIsCreateTaskOpen(false);
                  setTaskTitulo("");
                  setTaskDescripcion("");
                  setTaskFechaVenc("");
                  setTaskEstado("PENDIENTE");
                  setTaskAsignadoId(null);
                  setCreateTaskError(null);
                }
              }}
              title={`Crear tarea en ${project?.titulo}`}
            >
              <div className="space-y-3">
                {createTaskError && (
                  <div className="text-red-600">{createTaskError}</div>
                )}
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-300">
                    Título
                  </label>
                  <input
                    value={taskTitulo}
                    onChange={(e) => setTaskTitulo(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter") {
                        e.preventDefault();
                        void handleCreateTask();
                      }
                    }}
                    className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                    placeholder="Título de la tarea"
                  />
                </div>
                <div>
                  <label className="block text-sm text-gray-600 dark:text-gray-300">
                    Descripción
                  </label>
                  <textarea
                    value={taskDescripcion}
                    onChange={(e) => setTaskDescripcion(e.target.value)}
                    className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                    placeholder="Descripción opcional"
                  />
                </div>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-2">
                  <div>
                    <label className="block text-sm text-gray-600 dark:text-gray-300">
                      Fecha vencimiento
                    </label>
                    <input
                      type="date"
                      value={taskFechaVenc}
                      onChange={(e) => setTaskFechaVenc(e.target.value)}
                      className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                    />
                  </div>
                  <div>
                    <label className="block text-sm text-gray-600 dark:text-gray-300">
                      Estado
                    </label>
                    <select
                      value={taskEstado}
                      onChange={(e) => setTaskEstado(e.target.value)}
                      className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                    >
                      {estados.map((s) => (
                        <option key={s} value={s}>
                          {s}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm text-gray-600 dark:text-gray-300">
                      Asignado a
                    </label>
                    <select
                      value={taskAsignadoId || ""}
                      onChange={(e) =>
                        setTaskAsignadoId(
                          e.target.value ? Number(e.target.value) : null
                        )
                      }
                      className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                    >
                      <option value="">-- Sin asignar --</option>
                      {assignedUsers.map((u) => (
                        <option key={u.id} value={u.id}>
                          {u.nombre} ({u.email})
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="flex justify-end gap-2">
                  <button
                    onClick={() => {
                      if (!creatingTask) {
                        setIsCreateTaskOpen(false);
                        setTaskTitulo("");
                        setTaskDescripcion("");
                        setTaskFechaVenc("");
                        setTaskEstado("PENDIENTE");
                        setTaskAsignadoId(null);
                        setCreateTaskError(null);
                      }
                    }}
                    className="px-3 py-2 bg-gray-200 dark:bg-gray-700 rounded-md"
                  >
                    Cancelar
                  </button>
                  <button
                    onClick={() => void handleCreateTask()}
                    className="px-3 py-2 bg-indigo-600 text-white rounded-md"
                    disabled={creatingTask}
                  >
                    {creatingTask ? "Creando..." : "Crear tarea"}
                  </button>
                </div>
              </div>
            </Modal>
          </div>
        )}
      </main>
    </div>
  );
}

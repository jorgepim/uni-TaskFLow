import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { logout } from "../../api/endpoints/authApi";
import Navbar from "../../components/layout/Navbar";
import {
  getMyProyectos,
  createProyecto,
} from "../../api/endpoints/proyectosApi";
import Modal from "../../components/ui/Modal";
import { patchUpdateProyecto } from "../../api/endpoints/proyectosApi";

export default function UserDashboard() {
  const navigate = useNavigate();
  const [proyectos, setProyectos] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [newTitulo, setNewTitulo] = useState("");
  const [newDescripcion, setNewDescripcion] = useState("");
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState(null);

  const handleLogout = async () => {
    try {
      await logout();
    } catch (err) {
      void err;
    }
    try {
      localStorage.removeItem("api_token");
      localStorage.removeItem("user");
      localStorage.removeItem("user_roles");
    } catch (err) {
      void err;
    }
    navigate("/login", { replace: true });
  };

  const user = (() => {
    try {
      return JSON.parse(localStorage.getItem("user") || "{}");
    } catch {
      return {};
    }
  })();

  const userName = user?.nombre || user?.name || "Usuario";
  const currentUserId = user?.id;

  const links = [
    { to: "/user/dashboard", label: "Proyectos" },
    { to: "/user/tasks", label: "Tareas" },
    { to: "/user/profile", label: "Perfil" },
  ];

  useEffect(() => {
    let mounted = true;
    const fetchProjects = async (params = {}) => {
      setLoading(true);
      setError(null);
      try {
        const res = await getMyProyectos(params);
        if (mounted) setProyectos(res.data?.data || []);
      } catch (err) {
        setError(
          err?.response?.data?.message ||
            err.message ||
            "Error cargando proyectos"
        );
      } finally {
        if (mounted) setLoading(false);
      }
    };

    // initial load
    void fetchProjects();
    return () => {
      mounted = false;
    };
  }, []);

  // filter state
  const [tituloFilter, setTituloFilter] = useState("");
  const [fechaInicio, setFechaInicio] = useState("");
  const [fechaFin, setFechaFin] = useState("");

  const handleFilter = async (e) => {
    e?.preventDefault();
    const params = {};
    if (tituloFilter) params.titulo = tituloFilter;
    if (fechaInicio) params.fecha_inicio = fechaInicio;
    if (fechaFin) params.fecha_fin = fechaFin;
    setLoading(true);
    setError(null);
    try {
      const res = await getMyProyectos(params);
      setProyectos(res.data?.data || []);
    } catch (err) {
      setError(
        err?.response?.data?.message ||
          err.message ||
          "Error cargando proyectos"
      );
    } finally {
      setLoading(false);
    }
  };

  const handleClear = async () => {
    setTituloFilter("");
    setFechaInicio("");
    setFechaFin("");
    setError(null);
    setLoading(true);
    try {
      const res = await getMyProyectos();
      setProyectos(res.data?.data || []);
    } catch (err) {
      setError(
        err?.response?.data?.message ||
          err.message ||
          "Error cargando proyectos"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-linear-to-b from-white to-gray-100 dark:from-gray-900 dark:to-gray-800">
      <Navbar
        brand="Mi espacio"
        userName={userName}
        links={links}
        onLogout={handleLogout}
      />
      <main className="p-8">
        <h2 className="text-black dark:text-white text-2xl font-bold mb-4">
          Tablero de Usuario
        </h2>

        <div className="mb-4">
          <button
            onClick={() => setIsCreateOpen(true)}
            className="px-4 py-2 bg-green-600 text-white rounded-md"
          >
            + Crear proyecto
          </button>
        </div>
        <form
          className="mb-6 grid grid-cols-1 md:grid-cols-4 gap-3 items-end"
          onSubmit={handleFilter}
        >
          <div>
            <label className="block text-sm text-gray-600 dark:text-gray-300">
              Título
            </label>
            <input
              value={tituloFilter}
              onChange={(e) => setTituloFilter(e.target.value)}
              className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
              placeholder="Buscar por título"
            />
          </div>
          <div>
            <label className="block text-sm text-gray-600 dark:text-gray-300">
              Fecha inicio
            </label>
            <input
              type="date"
              value={fechaInicio}
              onChange={(e) => setFechaInicio(e.target.value)}
              className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
            />
          </div>
          <div>
            <label className="block text-sm text-gray-600 dark:text-gray-300">
              Fecha fin
            </label>
            <input
              type="date"
              value={fechaFin}
              onChange={(e) => setFechaFin(e.target.value)}
              className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
            />
          </div>
          <div className="flex gap-2">
            <button
              type="submit"
              className="px-4 py-2 bg-indigo-600 text-white rounded-md"
            >
              Filtrar
            </button>
            <button
              type="button"
              onClick={handleClear}
              className="px-4 py-2 bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-gray-100 rounded-md"
            >
              Limpiar
            </button>
          </div>
        </form>

        {loading && <div className="p-4">Cargando proyectos...</div>}

        {error && <div className="p-4 text-red-600">{String(error)}</div>}

        {/* Modal crear proyecto */}
        <Modal
          isOpen={isCreateOpen}
          onClose={() => {
            if (!creating) {
              setIsCreateOpen(false);
              setNewTitulo("");
              setNewDescripcion("");
              setCreateError(null);
            }
          }}
          title="Crear nuevo proyecto"
        >
          <div className="space-y-3">
            {createError && <div className="text-red-600">{createError}</div>}
            <div>
              <label className="block text-sm text-gray-600 dark:text-gray-300">
                Título
              </label>
              <input
                value={newTitulo}
                onChange={(e) => setNewTitulo(e.target.value)}
                className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                placeholder="Proyecto Ejemplo Jorge"
              />
            </div>
            <div>
              <label className="block text-sm text-gray-600 dark:text-gray-300">
                Descripción (opcional)
              </label>
              <textarea
                value={newDescripcion}
                onChange={(e) => setNewDescripcion(e.target.value)}
                className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                placeholder="Descripción opcional"
              />
            </div>
            <div className="flex justify-end gap-2">
              <button
                onClick={() => {
                  if (!creating) {
                    setIsCreateOpen(false);
                    setNewTitulo("");
                    setNewDescripcion("");
                    setCreateError(null);
                  }
                }}
                className="px-4 py-2 bg-gray-200 dark:bg-gray-700 rounded-md"
              >
                Cancelar
              </button>
              <button
                onClick={async () => {
                  setCreateError(null);
                  if (!newTitulo.trim()) {
                    setCreateError("El título es requerido");
                    return;
                  }
                  setCreating(true);
                  try {
                    await createProyecto({
                      titulo: newTitulo,
                      descripcion: newDescripcion,
                    });
                    // reload proyectos
                    setIsCreateOpen(false);
                    setNewTitulo("");
                    setNewDescripcion("");
                    setCreateError(null);
                    setLoading(true);
                    const res = await getMyProyectos();
                    setProyectos(res.data?.data || []);
                  } catch (err) {
                    setCreateError(
                      err?.response?.data?.message ||
                        err.message ||
                        "Error creando proyecto"
                    );
                  } finally {
                    setCreating(false);
                    setLoading(false);
                  }
                }}
                className="px-4 py-2 bg-indigo-600 text-white rounded-md"
                disabled={creating}
              >
                {creating ? "Creando..." : "Crear proyecto"}
              </button>
            </div>
          </div>
        </Modal>

        {!loading && !error && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {proyectos.length === 0 && (
              <div className="p-6 bg-white dark:bg-gray-700 rounded-lg shadow text-gray-900 dark:text-gray-100">
                No tienes proyectos todavía.
              </div>
            )}

            {proyectos.map((p) => (
              <div
                key={p.id}
                role="button"
                tabIndex={0}
                onClick={() => navigate(`/projects/${p.id}`)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") navigate(`/projects/${p.id}`);
                }}
                className="p-6 bg-white dark:bg-gray-800 rounded-lg shadow-md hover:shadow-lg transform hover:-translate-y-0.5 transition cursor-pointer text-gray-900 dark:text-gray-100"
              >
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="text-lg font-semibold mb-1">{p.titulo}</h3>
                    <p className="text-sm text-gray-600 dark:text-gray-300">
                      {p.descripcion}
                    </p>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="text-xs text-gray-500 dark:text-gray-400">
                      {p.fecha_creacion}
                    </div>
                    {(p.creado_por?.id === currentUserId ||
                      p.creado_por === currentUserId ||
                      p.creado_por_id === currentUserId) && (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          // dispatch a custom event the modal listens to
                          try {
                            window.dispatchEvent(
                              new CustomEvent("open-edit-project", {
                                detail: { id: p.id },
                                bubbles: true,
                                cancelable: true,
                              })
                            );
                          } catch (err) {
                            // fallback: no-op
                            void err;
                          }
                        }}
                        title="Editar proyecto"
                        className="text-sm text-indigo-600 hover:text-indigo-800"
                      >
                        Editar
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Edit project modal state and UI */}
        <EditProjectModal
          proyectos={proyectos}
          onSave={async () => {
            // reload proyectos after edit
            setLoading(true);
            try {
              const res = await getMyProyectos();
              setProyectos(res.data?.data || []);
            } catch (err) {
              void err;
            } finally {
              setLoading(false);
            }
          }}
        />
      </main>
    </div>
  );
}

function EditProjectModal({ proyectos, onSave }) {
  const [isOpen, setIsOpen] = React.useState(false);
  const [editingProject, setEditingProject] = React.useState(null);
  const [titulo, setTitulo] = React.useState("");
  const [descripcion, setDescripcion] = React.useState("");
  const [saving, setSaving] = React.useState(false);
  const [error, setError] = React.useState(null);

  // listen for custom event `open-edit-project` dispatched from project cards
  React.useEffect(() => {
    function onOpen(e) {
      try {
        const id = e?.detail?.id;
        if (!id) return;
        const proj = proyectos.find((x) => String(x.id) === String(id));
        if (!proj) return;
        setEditingProject(proj);
        setTitulo(proj.titulo || "");
        setDescripcion(proj.descripcion || "");
        setError(null);
        setIsOpen(true);
      } catch {
        // ignore
      }
    }
    window.addEventListener("open-edit-project", onOpen);
    return () => window.removeEventListener("open-edit-project", onOpen);
  }, [proyectos]);

  const handleSave = async () => {
    if (!editingProject) return;
    setSaving(true);
    setError(null);
    try {
      await patchUpdateProyecto(editingProject.id, {
        titulo: titulo,
        descripcion: descripcion,
      });
      setIsOpen(false);
      setEditingProject(null);
      if (onSave) await onSave();
    } catch (err) {
      setError(
        err?.response?.data?.message ||
          err.message ||
          "Error guardando proyecto"
      );
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={() => {
        if (!saving) {
          setIsOpen(false);
          setEditingProject(null);
          setError(null);
        }
      }}
      title={
        editingProject
          ? `Editar proyecto: ${editingProject.titulo}`
          : "Editar proyecto"
      }
    >
      {editingProject ? (
        <div className="space-y-3">
          {error && <div className="text-red-600">{String(error)}</div>}
          <div>
            <label className="block text-sm text-gray-600 dark:text-gray-300">
              Título
            </label>
            <input
              value={titulo}
              onChange={(e) => setTitulo(e.target.value)}
              className="mt-1 w-full px-3 py-2 rounded-md border"
            />
          </div>
          <div>
            <label className="block text-sm text-gray-600 dark:text-gray-300">
              Descripción
            </label>
            <textarea
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
              className="mt-1 w-full px-3 py-2 rounded-md border"
            />
          </div>
          <div className="flex justify-end gap-2">
            <button
              onClick={() => {
                setIsOpen(false);
                setEditingProject(null);
                setError(null);
              }}
              className="px-3 py-2 bg-gray-200 rounded-md"
              disabled={saving}
            >
              Cancelar
            </button>
            <button
              onClick={handleSave}
              className="px-3 py-2 bg-indigo-600 text-white rounded-md"
              disabled={saving}
            >
              {saving ? "Guardando..." : "Guardar"}
            </button>
          </div>
        </div>
      ) : (
        <div>Cargando...</div>
      )}
    </Modal>
  );
}

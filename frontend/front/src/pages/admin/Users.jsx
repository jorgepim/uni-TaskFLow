import React, { useEffect, useState } from "react";
import Navbar from "../../components/layout/Navbar";
import Modal from "../../components/ui/Modal";
import {
  getUsuarios,
  deleteUsuario,
  getUsuarioById,
  adminPatchUsuario,
} from "../../api/endpoints/userApi";

export default function AdminUsers() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [usuarios, setUsuarios] = useState([]);
  const [estadisticas, setEstadisticas] = useState(null);

  const [nombreFilter, setNombreFilter] = useState("");
  const [emailFilter, setEmailFilter] = useState("");
  const [rolFilter, setRolFilter] = useState("");

  const [isEditOpen, setIsEditOpen] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [saving, setSaving] = useState(false);
  const [editingRole, setEditingRole] = useState("");
  const [editingPassword, setEditingPassword] = useState("");

  const currentUser = (() => {
    try {
      return JSON.parse(localStorage.getItem("user") || "{}");
    } catch {
      return {};
    }
  })();
  const userName = currentUser?.nombre || currentUser?.name || "Admin";
  const myId = currentUser?.id;

  const links = [
    { to: "/admin/dashboard", label: "Inicio" },
    { to: "/admin/users", label: "Usuarios" },
  ];

  const load = async (params = {}) => {
    setLoading(true);
    setError(null);
    setSuccessMessage(null);
    try {
      const res = await getUsuarios(params);
      const data = res.data?.data || {};
      setUsuarios(data.usuarios || []);
      setEstadisticas(data.estadisticas || null);
    } catch (err) {
      setError(
        err?.response?.data?.message || err.message || "Error cargando usuarios"
      );
      setUsuarios([]);
      setEstadisticas(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
    // roles are fixed (ADMIN/USER), no additional load required
  }, []);

  const handleFilter = async (e) => {
    e?.preventDefault();
    const params = {};
    if (nombreFilter) params.nombre = nombreFilter;
    if (emailFilter) params.email = emailFilter;
    if (rolFilter) params.rol = rolFilter;
    await load(params);
  };

  const handleClear = async () => {
    setNombreFilter("");
    setEmailFilter("");
    setRolFilter("");
    setSuccessMessage(null);
    await load();
  };

  const openEdit = async (u) => {
    try {
      const res = await getUsuarioById(u.id);
      const userData = res.data?.data || u;
      setEditingUser(userData);
      // Map backend role name (ADMIN/USER) to combo values expected by admin endpoint (Admin/User)
      const firstRole =
        (userData.roles && userData.roles[0] && userData.roles[0].nombre) || "";
      setEditingRole(
        firstRole === "ADMIN" ? "Admin" : firstRole === "USER" ? "User" : ""
      );
      setIsEditOpen(true);
    } catch (err) {
      setError(
        err?.response?.data?.message || err.message || "Error cargando usuario"
      );
    }
  };

  const handleSave = async () => {
    if (!editingUser) return;
    setSaving(true);
    try {
      // Use admin endpoint so rol can be updated too
      const payload = {
        nombre: editingUser.nombre,
        email: editingUser.email,
        activo: editingUser.activo ? 1 : 0,
        rol: editingRole,
      };
      if (editingPassword && editingPassword.trim())
        payload.password = editingPassword;
      await adminPatchUsuario(editingUser.id, payload);
      setIsEditOpen(false);
      setEditingUser(null);
      await load();
    } catch (err) {
      setError(
        err?.response?.data?.message || err.message || "Error guardando usuario"
      );
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (u) => {
    if (u.id === myId) return; // no borrar a sí mismo
    if (!confirm(`¿Eliminar usuario ${u.nombre}? Esta acción es irreversible.`))
      return;
    setLoading(true);
    try {
      const res = await deleteUsuario(u.id);
      const msg = res?.data?.message;
      if (msg) setSuccessMessage(msg);
      await load();
    } catch (err) {
      setError(
        err?.response?.data?.message ||
          err.message ||
          "Error eliminando usuario"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <Navbar brand="Admin Panel" userName={userName} links={links} />
      <main className="p-6 max-w-6xl mx-auto">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Usuarios
          </h1>
        </div>

        <form
          onSubmit={handleFilter}
          className="grid grid-cols-1 md:grid-cols-4 gap-3 mb-4"
        >
          <div>
            <label className="block text-sm text-gray-600 dark:text-gray-300">
              Nombre
            </label>
            <input
              value={nombreFilter}
              onChange={(e) => setNombreFilter(e.target.value)}
              className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
            />
          </div>
          <div>
            <label className="block text-sm text-gray-600 dark:text-gray-300">
              Email
            </label>
            <input
              value={emailFilter}
              onChange={(e) => setEmailFilter(e.target.value)}
              className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
            />
          </div>
          <div>
            <label className="block text-sm text-gray-600 dark:text-gray-300">
              Rol
            </label>
            <select
              value={rolFilter}
              onChange={(e) => setRolFilter(e.target.value)}
              className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
            >
              <option value="">-- Todos --</option>
              <option value="ADMIN">ADMIN</option>
              <option value="USER">USER</option>
            </select>
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

        {loading && <div>Cargando usuarios...</div>}
        {error && <div className="text-red-600 mb-4">{String(error)}</div>}
        {successMessage && (
          <div className="text-green-700 bg-green-50 dark:bg-green-900 dark:text-green-200 p-3 rounded mb-4">
            {successMessage}
          </div>
        )}

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
              <div className="p-2 bg-gray-50 dark:bg-gray-700 rounded">
                <div className="text-xs text-gray-500 dark:text-gray-300">
                  Activos
                </div>
                <div className="font-semibold text-gray-900 dark:text-gray-100">
                  {estadisticas.activos}
                </div>
              </div>
              <div className="p-2 bg-gray-50 dark:bg-gray-700 rounded">
                <div className="text-xs text-gray-500 dark:text-gray-300">
                  Inactivos
                </div>
                <div className="font-semibold text-gray-900 dark:text-gray-100">
                  {estadisticas.inactivos}
                </div>
              </div>
              <div className="p-2 bg-gray-50 dark:bg-gray-700 rounded">
                <div className="text-xs text-gray-500 dark:text-gray-300">
                  Por rol
                </div>
                <div className="font-semibold text-gray-900 dark:text-gray-100">
                  {JSON.stringify(estadisticas.por_rol)}
                </div>
              </div>
            </div>
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {usuarios.map((u) => (
            <div
              key={u.id}
              className={`p-4 rounded-md shadow ${
                u.activo
                  ? "bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                  : "bg-red-50 dark:bg-red-900 text-red-900 dark:text-red-100 border border-red-200 dark:border-red-700"
              }`}
            >
              <div className="flex items-start justify-between">
                <div>
                  <div className="font-semibold text-lg text-gray-900 dark:text-gray-100">
                    {u.nombre}
                  </div>
                  <div className="text-sm text-gray-600 dark:text-gray-300">
                    {u.email}
                  </div>
                  <div className="text-xs text-gray-500 dark:text-gray-400 mt-2">
                    Creado: {u.fecha_creacion}
                  </div>
                  <div className="text-xs text-gray-500 dark:text-gray-400">
                    Roles: {u.roles?.map((r) => r.nombre).join(", ")}
                  </div>
                </div>
                <div className="flex flex-col gap-2">
                  <button
                    onClick={() => openEdit(u)}
                    className="px-3 py-1 bg-blue-600 text-white rounded-md text-sm"
                  >
                    Editar
                  </button>
                  {u.id !== myId && (
                    <button
                      onClick={() => handleDelete(u)}
                      className="px-3 py-1 bg-red-600 text-white rounded-md text-sm"
                    >
                      Eliminar
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>

        <Modal
          isOpen={isEditOpen}
          onClose={() => {
            if (!saving) {
              setIsEditOpen(false);
              setEditingUser(null);
              setError(null);
            }
          }}
          title={
            editingUser ? `Editar ${editingUser.nombre}` : "Editar usuario"
          }
        >
          {editingUser ? (
            <div className="space-y-3">
              <div>
                <label className="block text-sm text-gray-600 dark:text-gray-300">
                  Nombre
                </label>
                <input
                  value={editingUser.nombre || ""}
                  onChange={(e) =>
                    setEditingUser({ ...editingUser, nombre: e.target.value })
                  }
                  className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-600 dark:text-gray-300">
                  Email
                </label>
                <input
                  value={editingUser.email || ""}
                  onChange={(e) =>
                    setEditingUser({ ...editingUser, email: e.target.value })
                  }
                  className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                />
              </div>
              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={!!editingUser.activo}
                  onChange={(e) =>
                    setEditingUser({ ...editingUser, activo: e.target.checked })
                  }
                />
                <label className="text-sm text-gray-600 dark:text-gray-300">
                  Activo
                </label>
              </div>

              <div>
                <label className="block text-sm text-gray-600 dark:text-gray-300">
                  Rol
                </label>
                <select
                  value={editingRole}
                  onChange={(e) => setEditingRole(e.target.value)}
                  className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                >
                  <option value="">-- Seleccionar --</option>
                  <option value="Admin">ADMIN</option>
                  <option value="User">USER</option>
                </select>
              </div>

              <div>
                <label className="block text-sm text-gray-600 dark:text-gray-300">
                  Password (opcional)
                </label>
                <input
                  type="password"
                  value={editingPassword}
                  onChange={(e) => setEditingPassword(e.target.value)}
                  placeholder="Dejar vacío para no cambiar"
                  className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                />
              </div>
              {error && <div className="text-red-600">{String(error)}</div>}
              <div className="flex justify-end gap-2">
                <button
                  onClick={() => {
                    setIsEditOpen(false);
                    setEditingUser(null);
                    setError(null);
                  }}
                  className="px-3 py-2 bg-gray-200 dark:bg-gray-700 rounded-md"
                >
                  Cancelar
                </button>
                <button
                  onClick={handleSave}
                  disabled={saving}
                  className="px-3 py-2 bg-indigo-600 text-white rounded-md"
                >
                  {saving ? "Guardando..." : "Guardar"}
                </button>
              </div>
            </div>
          ) : (
            <div>Cargando...</div>
          )}
        </Modal>
      </main>
    </div>
  );
}

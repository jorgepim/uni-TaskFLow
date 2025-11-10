import React, { useEffect, useState } from "react";
import Navbar from "../../components/layout/Navbar";
import { getUsuarioById, patchUsuario } from "../../api/endpoints/userApi";

export default function Profile() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [editing, setEditing] = useState(false);
  const [nombre, setNombre] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [activo, setActivo] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState(null);

  // current user id from localStorage
  const currentUser = (() => {
    try {
      return JSON.parse(localStorage.getItem("user") || "{}");
    } catch {
      return {};
    }
  })();
  const userId = currentUser?.id;

  const load = async () => {
    if (!userId) return;
    setLoading(true);
    setError(null);
    try {
      const res = await getUsuarioById(userId);
      const data = res.data?.data || null;
      setUser(data);
      setNombre(data?.nombre || "");
      setEmail(data?.email || "");
      setActivo(typeof data?.activo === "boolean" ? data.activo : true);
      return data;
    } catch (err) {
      setError(
        err?.response?.data?.message || err.message || "Error cargando usuario"
      );
      return null;
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId]);

  const handleSave = async () => {
    setSaveError(null);
    if (!nombre.trim() || !email.trim()) {
      setSaveError("Nombre y email son requeridos");
      return;
    }
    setSaving(true);
    try {
      const payload = {
        nombre: nombre,
        email: email,
        // Only send password if user filled a new one
        ...(password ? { password } : {}),
        activo: !!activo,
      };
      await patchUsuario(userId, payload);
      setEditing(false);
      setPassword("");
      const fresh = await load();
      // Update localStorage user so other pages/navbar can read the new name
      try {
        if (fresh) {
          // preserve roles if present in previous localStorage
          const prev = JSON.parse(localStorage.getItem("user") || "{}");
          const merged = { ...prev, ...fresh };
          localStorage.setItem("user", JSON.stringify(merged));
        }
      } catch {
        // ignore
      }
    } catch (err) {
      setSaveError(
        err?.response?.data?.message || err.message || "Error guardando usuario"
      );
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <Navbar brand="Perfil" userName={user?.nombre || user?.name} />
      <main className="p-6 max-w-3xl mx-auto">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Mi perfil
          </h1>
          <button
            onClick={() => window.history.back()}
            className="px-3 py-2 bg-gray-200 dark:bg-gray-700 rounded-md"
          >
            ← Volver
          </button>
        </div>
        {loading && <div>Cargando...</div>}
        {error && <div className="text-red-600">{String(error)}</div>}

        {user && (
          <div className="bg-white dark:bg-gray-800 p-6 rounded-md shadow">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
              <div>
                <label className="block text-sm text-gray-600 dark:text-gray-300">
                  Nombre
                </label>
                <input
                  className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                  value={nombre}
                  onChange={(e) => setNombre(e.target.value)}
                  disabled={!editing}
                />
              </div>
              <div>
                <label className="block text-sm text-gray-600 dark:text-gray-300">
                  Email
                </label>
                <input
                  className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={!editing}
                />
              </div>
              <div>
                <label className="block text-sm text-gray-600 dark:text-gray-300">
                  Activo
                </label>
                <div className="mt-1">
                  <label className="inline-flex items-center">
                    <input
                      type="checkbox"
                      checked={activo}
                      onChange={(e) => setActivo(e.target.checked)}
                      // disabled={!editing}
                      disabled={true}
                      className="form-checkbox h-5 w-5 text-indigo-600"
                    />
                    <span className="ml-2 text-gray-700 dark:text-gray-200">
                      {activo ? "Sí" : "No"}
                    </span>
                  </label>
                </div>
              </div>
            </div>

            <div className="mb-4">
              <label className="block text-sm text-gray-600 dark:text-gray-300">
                Roles
              </label>
              <div className="mt-1 text-gray-900 dark:text-gray-100">
                {(user.roles || []).map((r) => r.nombre).join(", ")}
              </div>
            </div>

            <div className="mb-4">
              <label className="block text-sm text-gray-600 dark:text-gray-300">
                Contraseña (dejar vacío para no cambiar)
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={!editing}
                className="mt-1 w-full px-3 py-2 rounded-md border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
              />
            </div>

            {saveError && <div className="text-red-600 mb-2">{saveError}</div>}

            <div className="flex justify-end gap-2">
              {editing ? (
                <>
                  <button
                    onClick={() => {
                      setEditing(false);
                      // reset fields to last saved
                      setNombre(user.nombre || "");
                      setEmail(user.email || "");
                      setActivo(
                        typeof user.activo === "boolean" ? user.activo : true
                      );
                      setPassword("");
                      setSaveError(null);
                    }}
                    className="px-3 py-2 bg-gray-200 dark:bg-gray-700 rounded-md"
                    disabled={saving}
                  >
                    Cancelar
                  </button>
                  <button
                    onClick={() => void handleSave()}
                    className="px-3 py-2 bg-indigo-600 text-white rounded-md"
                    disabled={saving}
                  >
                    {saving ? "Guardando..." : "Guardar"}
                  </button>
                </>
              ) : (
                <button
                  onClick={() => setEditing(true)}
                  className="px-3 py-2 bg-indigo-600 text-white rounded-md"
                >
                  Editar
                </button>
              )}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

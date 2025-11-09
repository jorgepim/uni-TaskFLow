import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { logout } from "../../api/endpoints/authApi";
import Navbar from "../../components/layout/Navbar";
import { getMyProyectos } from "../../api/endpoints/proyectosApi";

export default function UserDashboard() {
  const navigate = useNavigate();
  const [proyectos, setProyectos] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

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

  const links = [
    { to: "/user/dashboard", label: "Proyectos" },
    { to: "/user/tasks", label: "Tareas" },
    { to: "/user/profile", label: "Perfil" },
  ];

  useEffect(() => {
    let mounted = true;
    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await getMyProyectos();
        if (mounted) setProyectos(res.data?.data || []);
      } catch (err) {
        setError(err?.response?.data?.message || err.message || "Error cargando proyectos");
      } finally {
        if (mounted) setLoading(false);
      }
    };
    void load();
    return () => {
      mounted = false;
    };
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-b from-white to-gray-100 dark:from-gray-900 dark:to-gray-800">
      <Navbar
        brand="Mi espacio"
        userName={userName}
        links={links}
        onLogout={handleLogout}
      />
      <main className="p-8">
        <h2 className="text-black dark:text-white text-2xl font-bold mb-4">Tablero de Usuario</h2>

        {loading && (
          <div className="p-4">Cargando proyectos...</div>
        )}

        {error && (
          <div className="p-4 text-red-600">{String(error)}</div>
        )}

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
                className="p-6 bg-white dark:bg-gray-800 rounded-lg shadow cursor-pointer text-gray-900 dark:text-gray-100"
              >
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="text-lg font-semibold">{p.titulo}</h3>
                    <p className="text-sm text-gray-600 dark:text-gray-300">{p.descripcion}</p>
                    <div className="text-xs text-gray-500 dark:text-gray-400 mt-2">Creado: {p.fecha_creacion}</div>
                  </div>
                </div>

                <div className="mt-4">
                  <h4 className="text-sm font-medium mb-2">Tareas</h4>
                  <div className="space-y-2">
                    {Array.isArray(p.tareas) && p.tareas.length > 0 ? (
                      p.tareas.map((t) => (
                        <div
                          key={t.id}
                          className="flex items-center justify-between p-2 rounded-md bg-gray-50 dark:bg-gray-700/60"
                        >
                          <div className="text-sm">{t.titulo}</div>
                          <div>
                            <span className={`px-2 py-1 text-xs rounded-full ${t.estado === 'PENDIENTE' ? 'bg-yellow-400 text-black' : t.estado === 'COMPLETADA' ? 'bg-green-400 text-black' : 'bg-gray-300 text-black'}`}>
                              {t.estado}
                            </span>
                          </div>
                        </div>
                      ))
                    ) : (
                      <div className="text-sm text-gray-600 dark:text-gray-300">No hay tareas</div>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}

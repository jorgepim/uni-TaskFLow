import React from "react";
import { useNavigate } from "react-router-dom";
import { logout } from "../../api/endpoints/authApi";
import Navbar from "../../components/layout/Navbar";

export default function AdminDashboard() {
  const navigate = useNavigate();

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

  const userName = user?.nombre || user?.name || "Admin";

  const links = [
    { to: "/admin/dashboard", label: "Inicio" },
    { to: "/admin/users", label: "Usuarios" },
    { to: "/admin/stats", label: "Estadísticas" },
    { to: "/admin/projects", label: "Proyectos" },
    { to: "/admin/tasks", label: "Tareas" },
  ];

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <Navbar
        brand="Admin Panel"
        userName={userName}
        links={links}
        onLogout={handleLogout}
      />
      <main className="p-8">
        <h2 className="text-2xl font-bold mb-4">Tablero de Administrador</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="p-6 bg-white dark:bg-gray-700 rounded-lg shadow cursor-pointer text-gray-900 dark:text-gray-100">
            Usuarios - gestión rápida
          </div>
          <div className="p-6 bg-white dark:bg-gray-700 rounded-lg shadow cursor-pointer text-gray-900 dark:text-gray-100">
            Estadísticas - gráficos
          </div>
          <div className="p-6 bg-white dark:bg-gray-700 rounded-lg shadow cursor-pointer text-gray-900 dark:text-gray-100">
            Proyectos y Tareas
          </div>
        </div>
      </main>
    </div>
  );
}

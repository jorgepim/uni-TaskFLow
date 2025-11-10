import React, { useEffect } from "react";
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
    { to: "/admin/stats/projects", label: "Est. Proyectos" },
    { to: "/admin/stats/tasks", label: "Est. Tareas" },
  ];

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <Navbar
        brand="Admin Panel"
        userName={userName}
        links={links}
        onLogout={handleLogout}
      />
      {/* Al cargar el dashboard redirigimos directamente a la vista de usuarios */}
      <AdminDashboardRedirect />
    </div>
  );
}

function AdminDashboardRedirect() {
  const navigate = useNavigate();
  useEffect(() => {
    // Redirige a la lista de usuarios al entrar en el dashboard
    navigate("/admin/users", { replace: true });
  }, [navigate]);
  return null;
}

import React from "react";
import { Routes, Route, Navigate, useLocation } from "react-router-dom";
import Login from "../pages/auth/Login";
import Register from "../pages/auth/Register";
import AdminDashboard from "../pages/admin/AdminDashboard";
import AdminUsers from "../pages/admin/Users";
import StatsProjects from "../pages/admin/StatsProjects";
import StatsTasks from "../pages/admin/StatsTasks";
import UserDashboard from "../pages/user/UserDashboard";
import ProjectDetail from "../pages/user/ProjectDetail";
import Profile from "../pages/user/Profile";
import Tasks from "../pages/user/Tasks";

// Helpers simples basados en localStorage
const isAuthenticated = () => !!localStorage.getItem("api_token");
const getUserRoles = () => {
  try {
    const r = localStorage.getItem("user_roles");
    return r ? JSON.parse(r) : [];
  } catch {
    return [];
  }
};

function RequireAuth({ children }) {
  const location = useLocation();
  if (!isAuthenticated()) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  return children;
}

function RequireRole({ role, children }) {
  const roles = getUserRoles();
  if (!roles.includes(role)) {
    // Si no tiene el rol solicitado redirigimos al dashboard según rol real o login
    if (!isAuthenticated()) return <Navigate to="/login" replace />;
    const realRoles = roles;
    if (realRoles.includes("ADMIN"))
      return <Navigate to="/admin/dashboard" replace />;
    return <Navigate to="/user/dashboard" replace />;
  }
  return children;
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route
        path="/admin/dashboard"
        element={
          <RequireAuth>
            <RequireRole role="ADMIN">
              <AdminDashboard />
            </RequireRole>
          </RequireAuth>
        }
      />

      <Route
        path="/admin/stats/projects"
        element={
          <RequireAuth>
            <RequireRole role="ADMIN">
              <StatsProjects />
            </RequireRole>
          </RequireAuth>
        }
      />

      <Route
        path="/admin/stats/tasks"
        element={
          <RequireAuth>
            <RequireRole role="ADMIN">
              <StatsTasks />
            </RequireRole>
          </RequireAuth>
        }
      />

      <Route
        path="/admin/stats"
        element={<Navigate to="/admin/stats/projects" replace />}
      />

      <Route
        path="/admin/users"
        element={
          <RequireAuth>
            <RequireRole role="ADMIN">
              <AdminUsers />
            </RequireRole>
          </RequireAuth>
        }
      />

      <Route
        path="/user/dashboard"
        element={
          <RequireAuth>
            <RequireRole role="USER">
              <UserDashboard />
            </RequireRole>
          </RequireAuth>
        }
      />

      <Route
        path="/projects/:id"
        element={
          <RequireAuth>
            <ProjectDetail />
          </RequireAuth>
        }
      />

      <Route
        path="/user/profile"
        element={
          <RequireAuth>
            <Profile />
          </RequireAuth>
        }
      />

      <Route
        path="/user/tasks"
        element={
          <RequireAuth>
            <Tasks />
          </RequireAuth>
        }
      />

      {/* Catch all */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default AppRoutes;

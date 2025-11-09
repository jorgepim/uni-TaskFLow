import axiosClient from "../axiosClient";

// Register: POST /api/register
export const register = async (payload) => {
  const res = await axiosClient.post("/register", payload);
  // Si la API devuelve token (según tu ejemplo), lo almacenamos
  if (res?.data?.data?.token) {
    const { token, user, roles } = res.data.data;
    try {
      localStorage.setItem("api_token", token);
      localStorage.setItem("user", JSON.stringify(user));
      localStorage.setItem("user_roles", JSON.stringify(roles));
      axiosClient.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    } catch {
      // ignore
    }
  }
  return res;
};

// Login: POST /api/login
// On success, store token and user data in localStorage and set default Authorization header
export const login = async (payload) => {
  const res = await axiosClient.post("/login", payload);
  if (res?.data?.data?.token) {
    const { token, user, roles } = res.data.data;
    try {
      localStorage.setItem("api_token", token);
      localStorage.setItem("user", JSON.stringify(user));
      localStorage.setItem("user_roles", JSON.stringify(roles));
      axiosClient.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    } catch {
      // ignore storage errors
    }
  }
  return res;
};

// Logout: POST /api/logout (protegido)
export const logout = async () => {
  const res = await axiosClient.post("/logout");
  try {
    localStorage.removeItem("api_token");
    localStorage.removeItem("user");
    localStorage.removeItem("user_roles");
    delete axiosClient.defaults.headers.common["Authorization"];
  } catch {
    // ignore logout/cleanup errors
  }
  return res;
};

// Helper: obtener roles del usuario desde localStorage
export const getUserRoles = () => {
  try {
    const roles = localStorage.getItem("user_roles");
    return roles ? JSON.parse(roles) : [];
  } catch {
    return [];
  }
};

// Helper: verificar si el usuario es admin
export const isAdmin = () => {
  const roles = getUserRoles();
  return roles.includes("ADMIN");
};

export default { register, login, logout, getUserRoles, isAdmin };

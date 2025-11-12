import axios from "axios";
import { toast } from "react-toastify";

// Vite sólo expone variables que empiezan por VITE_. Preferimos VITE_API_URL.
// Si no está definida, usamos la URL local de la API de Laravel como fallback.
const baseURL =
  import.meta.env.VITE_API_URL ||
  import.meta.env.VITE_API_URL_LARAVEL ||
  "http://127.0.0.1:8000/api";

// Crear cliente axios común. Activamos withCredentials para que funcione Sanctum
// y agregamos headers por defecto.
const axiosClient = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
});

// Si hay token guardado (api_token), lo ponemos en headers por defecto.
try {
  const saved = localStorage.getItem("api_token");
  if (saved) {
    axiosClient.defaults.headers.common["Authorization"] = `Bearer ${saved}`;
  }
} catch {
  // ignore
}

// Manejo centralizado de errores: si recibimos 401 limpiamos token y redirigimos a login.
axiosClient.interceptors.response.use(
  (r) => {
    try {
      const msg = r?.data?.message;
      // Si la respuesta incluye un message, mostramos un toast (info/success)
      if (msg) {
        // Si el objeto incluye una propiedad status y es 'success', usar success, si no usar info
        const status = r?.data?.status;
        if (status && String(status).toLowerCase() === "success") {
          toast.success(msg);
        } else {
          toast.info(msg);
        }
      }
    } catch {
      // no bloquear por errores en el interceptor
    }
    return r;
  },
  (err) => {
    try {
      const apiMsg = err?.response?.data?.message;
      if (apiMsg) toast.error(apiMsg);
    } catch {
      // ignore
    }
    if (err?.response?.status === 401) {
      try {
        localStorage.removeItem("api_token");
        delete axiosClient.defaults.headers.common["Authorization"];
      } catch {
        // ignore
      }
      // Forzamos redirección al login
      if (typeof window !== "undefined") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(err);
  }
);

export default axiosClient;

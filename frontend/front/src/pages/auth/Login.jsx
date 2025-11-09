import React, { useState, useContext } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { login as loginApi } from "../../api/endpoints/authApi";
import { LoadingContext } from "../../context/LoadingContext";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { show, hide } = useContext(LoadingContext);
  const [errors, setErrors] = useState({ email: "", password: "", form: "" });

  const handleSubmit = async (e) => {
    e.preventDefault();
    // validations
    const fieldErrors = { email: "", password: "", form: "" };
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)) {
      fieldErrors.email = "Email inválido";
    }
    if (!password || password.length < 6) {
      fieldErrors.password = "La contraseña debe tener al menos 6 caracteres";
    }
    setErrors(fieldErrors);
    if (fieldErrors.email || fieldErrors.password) return;

    setLoading(true);
    show();
    try {
      const res = await loginApi({ email, password });
      toast.success(res?.data?.message || "Login exitoso");
      // Determinar rol y redirigir
      const roles =
        res?.data?.data?.roles ||
        JSON.parse(localStorage.getItem("user_roles") || "[]");
      if (roles.includes("ADMIN")) {
        navigate("/admin/dashboard", { replace: true });
      } else {
        navigate("/user/dashboard", { replace: true });
      }
    } catch (err) {
      const apiMsg = err?.response?.data?.message;
      const apiErrors = err?.response?.data?.errors;
      if (apiErrors) {
        const next = { email: "", password: "", form: "" };
        if (apiErrors.email) next.email = apiErrors.email.join(" ");
        if (apiErrors.password) next.password = apiErrors.password.join(" ");
        setErrors(next);
        toast.error(apiMsg || "Errores en el formulario");
      } else {
        const msg = apiMsg || "Error en el login";
        setErrors((s) => ({ ...s, form: msg }));
        toast.error(msg);
      }
    } finally {
      setLoading(false);
      hide();
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center from-purple-600 via-pink-500 to-indigo-500 p-6">
      <div className="max-w-md w-full bg-white/90 backdrop-blur-lg rounded-2xl shadow-xl p-8">
        <div className="text-center mb-6">
          <h1 className="text-3xl font-extrabold text-gray-900">Bienvenido</h1>
          <p className="text-sm text-gray-600">Inicia sesión para continuar</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">
              Email
            </label>
            <input
              type="email"
              name="email"
              autoComplete="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className={`mt-1 block w-full px-4 py-2 rounded-lg border ${
                errors.email ? "border-red-400" : "border-gray-200"
              } focus:outline-none focus:ring-2 focus:ring-pink-400`}
              placeholder="tu@ejemplo.com"
            />
            {errors.email && (
              <p className="mt-1 text-sm text-red-600">{errors.email}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">
              Contraseña
            </label>
            <input
              type="password"
              name="current-password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className={`mt-1 block w-full px-4 py-2 rounded-lg border ${
                errors.password ? "border-red-400" : "border-gray-200"
              } focus:outline-none focus:ring-2 focus:ring-pink-400`}
              placeholder="••••••••"
            />
            {errors.password && (
              <p className="mt-1 text-sm text-red-600">{errors.password}</p>
            )}
          </div>

          {errors.form && (
            <div className="text-sm text-red-600">{errors.form}</div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 bg-pink-500 hover:bg-pink-600 text-white font-semibold rounded-lg shadow cursor-pointer"
          >
            {loading ? "Entrando..." : "Entrar"}
          </button>
        </form>

        <div className="mt-6 text-center text-sm text-gray-700">
          ¿No tienes cuenta?{" "}
          <a
            href="/register"
            className="text-pink-600 font-medium cursor-pointer"
          >
            Regístrate
          </a>
        </div>
      </div>
    </div>
  );
}

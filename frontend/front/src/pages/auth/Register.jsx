import React, { useState, useContext } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { register as registerApi } from "../../api/endpoints/authApi";
import { LoadingContext } from "../../context/LoadingContext";

export default function Register() {
  const [nombre, setNombre] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [activo, setActivo] = useState(true);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { show, hide } = useContext(LoadingContext);
  const [errors, setErrors] = useState({
    nombre: "",
    email: "",
    password: "",
    form: "",
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    const fieldErrors = { nombre: "", email: "", password: "", form: "" };
    if (!nombre || nombre.trim().length < 2)
      fieldErrors.nombre = "Ingresa un nombre válido";
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email))
      fieldErrors.email = "Email inválido";
    if (!password || password.length < 6)
      fieldErrors.password = "La contraseña debe tener al menos 6 caracteres";
    setErrors(fieldErrors);
    if (fieldErrors.nombre || fieldErrors.email || fieldErrors.password) return;

    setLoading(true);
    show();
    try {
      const payload = { nombre, email, password, activo };
      const res = await registerApi(payload);
      toast.success(res?.data?.message || "Usuario registrado");
      navigate("/user/dashboard", { replace: true });
    } catch (err) {
      const apiMsg = err?.response?.data?.message;
      const apiErrors = err?.response?.data?.errors;
      if (apiErrors) {
        const next = { nombre: "", email: "", password: "", form: "" };
        if (apiErrors.nombre) next.nombre = apiErrors.nombre.join(" ");
        if (apiErrors.email) next.email = apiErrors.email.join(" ");
        if (apiErrors.password) next.password = apiErrors.password.join(" ");
        setErrors(next);
        toast.error(apiMsg || "Errores en el formulario");
      } else {
        const msg = apiMsg || "Error en el registro";
        setErrors((s) => ({ ...s, form: msg }));
        toast.error(msg);
      }
    } finally {
      setLoading(false);
      hide();
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center  from-green-400 via-blue-400 to-purple-600 p-6">
      <div className="max-w-md w-full bg-white/95 dark:bg-gray-800 rounded-2xl shadow-2xl p-8">
        <div className="text-center mb-6">
          <h1 className="text-3xl font-extrabold text-gray-900 dark:text-gray-100">
            Crea tu cuenta
          </h1>
          <p className="text-sm text-gray-600 dark:text-gray-300">
            Únete y empieza a gestionar tus proyectos
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
              Nombre
            </label>
            <input
              type="text"
              required
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              className={`mt-1 block w-full px-4 py-2 rounded-lg border ${
                errors.nombre ? "border-red-400" : "border-gray-200"
              } focus:outline-none focus:ring-2 focus:ring-blue-400 bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100`}
              placeholder="Tu nombre"
            />
            {errors.nombre && (
              <p className="mt-1 text-sm text-red-600">{errors.nombre}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
              Email
            </label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className={`mt-1 block w-full px-4 py-2 rounded-lg border ${
                errors.email ? "border-red-400" : "border-gray-200"
              } focus:outline-none focus:ring-2 focus:ring-blue-400 bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100`}
              placeholder="tu@ejemplo.com"
            />
            {errors.email && (
              <p className="mt-1 text-sm text-red-600">{errors.email}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
              Contraseña
            </label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className={`mt-1 block w-full px-4 py-2 rounded-lg border ${
                errors.password ? "border-red-400" : "border-gray-200"
              } focus:outline-none focus:ring-2 focus:ring-blue-400 bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100`}
              placeholder="Crea una contraseña segura"
            />
            {errors.password && (
              <p className="mt-1 text-sm text-red-600">{errors.password}</p>
            )}
          </div>

          <div className="flex items-center gap-2">
            <input
              id="activo"
              type="checkbox"
              checked={activo}
              onChange={(e) => setActivo(e.target.checked)}
              className="h-4 w-4 text-blue-600"
            />
            <label
              htmlFor="activo"
              className="text-sm text-gray-700 dark:text-gray-200"
            >
              Activo
            </label>
          </div>

          {errors.form && (
            <div className="text-sm text-red-600">{errors.form}</div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg shadow cursor-pointer"
          >
            {loading ? "Creando..." : "Crear cuenta"}
          </button>
        </form>

        <div className="mt-6 text-center text-sm text-gray-700 dark:text-gray-300">
          ¿Ya tienes cuenta?{" "}
          <a href="/login" className="text-blue-600 font-medium cursor-pointer">
            Inicia sesión
          </a>
        </div>
      </div>
    </div>
  );
}

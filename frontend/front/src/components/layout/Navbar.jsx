import React, { useState, useRef, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import ThemeSwitcher from "../ThemeSwitcher";
import { logout as apiLogout } from "../../api/endpoints/authApi";

export default function Navbar({
  brand = "App",
  userName = "Usuario",
  links = [],
  onLogout,
}) {
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    function onDoc(e) {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setOpen(false);
      }
    }
    document.addEventListener("mousedown", onDoc);
    return () => document.removeEventListener("mousedown", onDoc);
  }, []);

  const initials = (() => {
    try {
      const parts = String(userName).trim().split(/\s+/);
      if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    } catch {
      return "US";
    }
  })();

  // Determinar link de perfil según roles guardados
  const roles = (() => {
    try {
      return JSON.parse(localStorage.getItem("user_roles") || "[]");
    } catch {
      return [];
    }
  })();
  const profileLink = roles.includes("ADMIN")
    ? "/user/profile"
    : "/user/profile";

  return (
    <nav className="bg-white/90 dark:bg-gray-800/80 backdrop-blur px-6 py-4 flex items-center justify-between border-b dark:border-gray-700 relative">
      <div className="flex items-center gap-6">
        <div className="text-xl font-bold text-indigo-600">{brand}</div>
        {/* desktop links */}
        <div className="hidden md:flex items-center gap-4">
          {links.map((l) => (
            <Link
              key={l.to}
              to={l.to}
              className="text-sm text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white cursor-pointer"
            >
              {l.label}
            </Link>
          ))}
        </div>
      </div>

      <div className="flex items-center gap-4">
        {/* mobile hamburger */}
        <button
          onClick={() => setMobileOpen((s) => !s)}
          className="md:hidden p-2 rounded-md hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer"
          aria-label="Abrir menú"
        >
          <svg
            className="w-6 h-6"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M4 6h16M4 12h16M4 18h16"
            />
          </svg>
        </button>

        <div
          className="hidden sm:flex items-center gap-4 relative"
          ref={menuRef}
        >
          {/* Circular badge with initials/name */}
          <button
            onClick={() => setOpen((s) => !s)}
            aria-haspopup="true"
            aria-expanded={open}
            className="flex items-center gap-3 focus:outline-none cursor-pointer"
          >
            <div className="w-10 h-10 rounded-full bg-indigo-600 dark:bg-indigo-500 text-white flex items-center justify-center font-semibold">
              {initials}
            </div>
            <div className="hidden sm:block text-sm text-gray-700 dark:text-gray-200">
              {userName}
            </div>
          </button>

          {/* Dropdown */}
          {open && (
            <div className="absolute right-0 top-full mt-2 w-56 bg-white dark:bg-gray-800 rounded-md shadow-lg ring-1 ">
              <div className="py-1 ">
                <Link
                  to={profileLink}
                  onClick={() => setOpen(false)}
                  className="block px-4 py-2 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer"
                >
                  Perfil
                </Link>
                <div className="px-4 py-2">
                  <ThemeSwitcher />
                </div>
                <button
                  onClick={async () => {
                    setOpen(false);
                    if (onLogout) {
                      onLogout();
                      return;
                    }
                    try {
                      await apiLogout();
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
                  }}
                  className="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer"
                >
                  Cerrar sesión
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* mobile panel */}
      {mobileOpen && (
        <div className="md:hidden absolute left-0 top-full w-full bg-white shadow-md z-40">
          <div className="flex flex-col p-4 gap-2">
            {links.map((l) => (
              <Link
                key={l.to}
                to={l.to}
                className="block px-2 py-2 rounded hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer"
                onClick={() => setMobileOpen(false)}
              >
                {l.label}
              </Link>
            ))}
            <div className="border-t mt-2 pt-2">
              <div className="flex items-center gap-3 px-2">
                <div className="w-10 h-10 rounded-full bg-indigo-600 text-white flex items-center justify-center font-semibold">
                  {initials}
                </div>
                <div className="text-sm font-medium">{userName}</div>
              </div>
              <div className="mt-2 px-2">
                <Link
                  to={profileLink}
                  onClick={() => setMobileOpen(false)}
                  className="block px-2 py-2 rounded hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer"
                >
                  Perfil
                </Link>
                <div className="py-2">
                  <ThemeSwitcher />
                </div>
                <button
                  onClick={async () => {
                    setMobileOpen(false);
                    if (onLogout) {
                      onLogout();
                      return;
                    }
                    try {
                      await apiLogout();
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
                  }}
                  className="w-full text-left px-2 py-2 text-red-600 cursor-pointer"
                >
                  Cerrar sesión
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </nav>
  );
}

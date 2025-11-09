import { useContext } from "react";
import { ThemeContext } from "../context/ThemeContext";
import { Sun, Moon } from "lucide-react";

export default function ThemeSwitcher() {
  const { theme, toggleTheme } = useContext(ThemeContext);
  const isLight = theme === "light";

  return (
    <button
      onClick={toggleTheme}
      aria-label="Cambiar tema"
      title="Cambiar tema"
      className={
        "p-2 rounded-full transition flex items-center justify-center cursor-pointer" +
        (isLight
          ? " hover:bg-gray-100" 
          : " hover:bg-gray-600") 
      }
      style={isLight ? { backgroundColor: "#ffffff" } : undefined}
    >
      {isLight ? (
        // Luna en azul para tema claro
        <Moon className="w-6 h-6 text-blue-500" />
      ) : (
        // Sol en amarillo para tema oscuro
        <Sun className="w-6 h-6 text-yellow-400" />
      )}
    </button>
  );
}

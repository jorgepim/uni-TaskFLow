import React from "react";

const Button = ({
  children,
  onClick,
  type = "button",
  variant = "primary", // 'primary', 'secondary', 'danger', 'outline'
  size = "md", // 'sm', 'md', 'lg'
  className = "", // Clases adicionales de Tailwind
  ...props // Cualquier otra prop como disabled
}) => {
  const baseStyles =
    "font-semibold py-2 px-4 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 cursor-pointer";

  const variantStyles = {
    primary:
      "bg-primary text-text-light hover:bg-brand-purple/80 focus:ring-primary",
    secondary:
      "bg-secondary text-text-light hover:bg-brand-blue/80 focus:ring-secondary",
    accent: "bg-accent text-text-dark hover:bg-brand-pink/80 focus:ring-accent", // Added accent variant
    danger: "bg-red-600 text-text-light hover:bg-red-700 focus:ring-red-500",
    outline:
      "bg-transparent border border-muted text-muted hover:bg-muted/20 focus:ring-muted",
    // Puedes añadir más variantes como 'success', 'warning', etc.
  };

  const sizeStyles = {
    sm: "text-sm py-1.5 px-3",
    md: "text-base py-2 px-4",
    lg: "text-lg py-3 px-6",
  };

  return (
    <button
      type={type}
      onClick={onClick}
      className={`${baseStyles} ${variantStyles[variant]} ${sizeStyles[size]} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
};

export default Button;

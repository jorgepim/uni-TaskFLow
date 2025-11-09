import React from "react";

const Footer = () => {
  const currentYear = new Date().getFullYear(); // Obtiene el año actual dinámicamente

  return (
    <footer className="bg-primary text-text-light p-6 text-center shadow-inner mt-8">
      <div className="container mx-auto">
        <p className="text-sm">
          &copy; {currentYear} TecnoGlobal. Todos los derechos reservados.
        </p>
        <p className="text-xs mt-2 text-muted-foreground">
          Diseñado con <span className="text-accent">❤</span> por Jorge
        </p>
        {/* Opcional: Puedes añadir enlaces a redes sociales o políticas */}
        {/*
        <div className="flex justify-center space-x-4 mt-4">
          <a href="#" className="text-text-light hover:text-accent transition-colors duration-200">
            Privacidad
          </a>
          <a href="#" className="text-text-light hover:text-accent transition-colors duration-200">
            Términos
          </a>
        </div>
        */}
      </div>
    </footer>
  );
};

export default Footer;

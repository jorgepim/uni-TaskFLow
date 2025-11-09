import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { ThemeProvider } from "./context/ThemeProvider.jsx";
import { LoadingProvider } from "./context/LoadingContext";
import "./index.css";
import App from "./App.jsx";
import LoadingOverlay from "./components/ui/LoadingOverlay";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <LoadingProvider>
      <ThemeProvider>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </ThemeProvider>
      <LoadingOverlay />
    </LoadingProvider>
  </StrictMode>
);

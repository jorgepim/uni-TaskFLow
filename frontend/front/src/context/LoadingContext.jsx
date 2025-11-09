/* eslint-disable react-refresh/only-export-components */
import React, { createContext, useState } from "react";

export const LoadingContext = createContext({
  loading: false,
  show: () => {},
  hide: () => {},
});

export function LoadingProvider({ children }) {
  const [loading, setLoading] = useState(false);

  const show = () => setLoading(true);
  const hide = () => setLoading(false);

  return (
    <LoadingContext.Provider value={{ loading, show, hide }}>
      {children}
    </LoadingContext.Provider>
  );
}

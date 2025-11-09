// import { useEffect, useState } from "react";

// /**
//  * Hook genérico para realizar peticiones con Axios o Fetch.
//  * @param {Function} apiFunction - Función que retorna una promesa (por ejemplo: getAllUsuarios)
//  * @param {Array} deps - Dependencias del efecto (ej. [id] o [])
//  * @returns {{ data: any, loading: boolean, error: any }}
//  */
// export function useFetch(apiFunction, deps = []) {
//   const [data, setData] = useState(null);
//   const [loading, setLoading] = useState(true);
//   const [error, setError] = useState(null);

//   useEffect(() => {
//     let isMounted = true; // evita setState si el componente se desmonta
//     setLoading(true);

//     apiFunction()
//       .then((res) => {
//         if (!isMounted) return;
//         setData(res?.data ?? res);
//       })
//       .catch((err) => {
//         if (!isMounted) return;
//         setError(err);
//       })
//       .finally(() => {
//         if (!isMounted) return;
//         setLoading(false);
//       });

//     return () => {
//       isMounted = false;
//     };

//   }, [apiFunction, serializedDeps]);

//   const serializedDeps = JSON.stringify(deps);

//   return { data, loading, error };
// }

import { useEffect, useState, useCallback, useRef } from "react";

/**
 * Hook genérico adaptado para realizar peticiones con Axios y manejar la estructura ApiResponse.
 * Extrae `data` del campo `data` de ApiResponse en caso de éxito.
 * Extrae `message` del campo `message` de ApiResponse en caso de error.
 *
 * @param {Function} apiFunction - Función que retorna una promesa de Axios (ej: () => getEmpleados({ puesto: 'X' }))
 * @param {Array} deps - Dependencias del efecto para re-ejecutar la petición (ej. [filters])
 * @returns {{ data: any, loading: boolean, error: string | null }} - data contiene los datos extraídos, error contiene el mensaje de error como string.
 */
export function useFetch(apiFunction, deps = []) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null); // Ahora almacenará el string del mensaje de error

  // Ref para saber si el componente sigue montado (evita setState después del unmount)
  const mountedRef = useRef(true);

  // Serializamos deps para poder comparar contenido (evita re-ejecuciones por referencias nuevas)
  const serializedDeps = JSON.stringify(deps);

  // Mantener mountedRef actualizado
  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
    };
  }, []);

  // fetchData se memoiza únicamente en base al contenido de `deps` serializadas.
  // No incluimos `apiFunction` en la lista de dependencias porque llamadores suelen pasar
  // funciones inline; en su lugar asumimos que `deps` refleja los valores que la función usa.
  const fetchData = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await apiFunction(); // Ejecuta la función (ej: getEmpleados())

      if (!mountedRef.current) return; // Si ya se desmontó, no actualizamos estado

      // Verifica si la respuesta tiene la estructura esperada de ApiResponse
      if (response?.data && response.data.status) {
        if (response.data.status === "success") {
          setData(response.data.data ?? null);
          setError(null);
        } else {
          const errorMessage =
            response.data.message ||
            "API returned status: error without a message";
          setError(errorMessage);
          setData(null);
        }
      } else {
        console.warn("Received unexpected API response structure:", response);
        setError("Received unexpected response structure from API.");
        setData(null);
      }
    } catch (err) {
      if (!mountedRef.current) return;
      console.error("API call failed:", err);
      let errorMessage = "An unknown error occurred.";
      if (err.response?.data?.message) {
        errorMessage = err.response.data.message;
        if (err.response.data.data && Array.isArray(err.response.data.data)) {
          errorMessage += `: ${err.response.data.data.join(", ")}`;
        }
      } else if (err.message) {
        errorMessage = err.message;
      }
      setError(errorMessage);
      setData(null);
    } finally {
      if (mountedRef.current) setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [serializedDeps]); // Dependemos solo del contenido de `deps`

  // Ejecuta la petición cuando cambien las dependencias (serializadas)
  useEffect(() => {
    fetchData();
  }, [fetchData]);

  // Refetch estable que puede ser invocado por el consumidor
  const refetch = useCallback(() => {
    // Llamamos a la versión más reciente de fetchData (useCallback asegura estabilidad)
    fetchData();
  }, [fetchData]);

  return { data, loading, error, refetch };
}

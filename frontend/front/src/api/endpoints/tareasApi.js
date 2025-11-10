import axiosClient from "../axiosClient";

const API_ENDPOINT = "/tareas";

const toApiPayload = (form) => {
  if (!form) return {};
  const payload = {};
  Object.entries(form).forEach(([key, value]) => {
    const snake = key.replace(/([A-Z])/g, "_$1").toLowerCase();
    payload[snake] = value;
  });
  return payload;
};

export const getTareaById = (id) => axiosClient.get(`${API_ENDPOINT}/${id}`);

// Create a tarea under a project
export const createTarea = (projectId, data) =>
  axiosClient.post(`/proyectos/${projectId}/tareas`, toApiPayload(data));

// Create tarea using global endpoint /tareas (body must include proyecto_id)
export const createTareaGlobal = (data) =>
  axiosClient.post(`${API_ENDPOINT}`, toApiPayload(data));

export const updateTarea = (id, data) =>
  axiosClient.put(`${API_ENDPOINT}/${id}`, toApiPayload(data));

// Partial update using PATCH (edición parcial de campos)
export const patchTarea = (id, data) =>
  axiosClient.patch(`${API_ENDPOINT}/${id}`, toApiPayload(data));

// Update only the estado field using the backend's dedicated endpoint
export const updateTareaEstado = (id, estado) =>
  axiosClient.patch(`${API_ENDPOINT}/${id}/estado`, { estado });

export const deleteTarea = (id) => axiosClient.delete(`${API_ENDPOINT}/${id}`);

export default {
  getTareaById,
  createTarea,
  updateTarea,
  updateTareaEstado,
  deleteTarea,
};

// Get tareas for a specific usuario (by id) with optional filters
export const getUsuarioTareas = (usuarioId, params = {}) =>
  axiosClient.get(`/usuarios/${usuarioId}/tareas`, { params });

// Get tareas for the current authenticated user (/usuarios/me/tareas)
export const getMyTareas = (params = {}) =>
  axiosClient.get(`/usuarios/me/tareas`, { params });

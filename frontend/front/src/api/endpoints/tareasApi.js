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

export const updateTarea = (id, data) =>
  axiosClient.put(`${API_ENDPOINT}/${id}`, toApiPayload(data));

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

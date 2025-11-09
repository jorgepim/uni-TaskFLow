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

export const getTareas = (params = {}) =>
  axiosClient.get(API_ENDPOINT, { params });
export const getTareaById = (id) => axiosClient.get(`${API_ENDPOINT}/${id}`);
export const createTarea = (data) =>
  axiosClient.post(API_ENDPOINT, toApiPayload(data));
export const updateTarea = (id, data) =>
  axiosClient.put(`${API_ENDPOINT}/${id}`, toApiPayload(data));
export const deleteTarea = (id) => axiosClient.delete(`${API_ENDPOINT}/${id}`);

export default {
  getTareas,
  getTareaById,
  createTarea,
  updateTarea,
  deleteTarea,
};

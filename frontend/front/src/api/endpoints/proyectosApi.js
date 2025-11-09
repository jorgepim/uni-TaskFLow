import axiosClient from "../axiosClient";

const API_ENDPOINT = "/proyectos";

const toApiPayload = (form) => {
  if (!form) return {};
  const payload = {};
  Object.entries(form).forEach(([key, value]) => {
    const snake = key.replace(/([A-Z])/g, "_$1").toLowerCase();
    payload[snake] = value;
  });
  return payload;
};

export const getProyectos = (params = {}) =>
  axiosClient.get(API_ENDPOINT, { params });
export const getMyProyectos = () =>
  axiosClient.get(`/usuarios/me/proyectos`);
export const getProyectoById = (id) => axiosClient.get(`${API_ENDPOINT}/${id}`);
export const createProyecto = (data) =>
  axiosClient.post(API_ENDPOINT, toApiPayload(data));
export const updateProyecto = (id, data) =>
  axiosClient.put(`${API_ENDPOINT}/${id}`, toApiPayload(data));
export const deleteProyecto = (id) =>
  axiosClient.delete(`${API_ENDPOINT}/${id}`);

export default {
  getProyectos,
  getProyectoById,
  createProyecto,
  updateProyecto,
  deleteProyecto,
};

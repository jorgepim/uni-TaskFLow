import axiosClient from "../axiosClient";

const API_ENDPOINT = "/comentarios";

const toApiPayload = (form) => {
  if (!form) return {};
  const payload = {};
  Object.entries(form).forEach(([key, value]) => {
    const snake = key.replace(/([A-Z])/g, "_$1").toLowerCase();
    payload[snake] = value;
  });
  return payload;
};

export const getComentarios = (params = {}) =>
  axiosClient.get(API_ENDPOINT, { params });
export const getComentarioById = (id) =>
  axiosClient.get(`${API_ENDPOINT}/${id}`);
export const createComentario = (data) =>
  axiosClient.post(API_ENDPOINT, toApiPayload(data));
export const updateComentario = (id, data) =>
  axiosClient.put(`${API_ENDPOINT}/${id}`, toApiPayload(data));
export const patchComentario = (id, data) =>
  axiosClient.patch(`${API_ENDPOINT}/${id}`, toApiPayload(data));
export const deleteComentario = (id) =>
  axiosClient.delete(`${API_ENDPOINT}/${id}`);

export default {
  getComentarios,
  getComentarioById,
  createComentario,
  updateComentario,
  patchComentario,
  deleteComentario,
};

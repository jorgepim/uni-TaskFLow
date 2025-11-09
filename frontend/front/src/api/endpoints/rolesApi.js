import axiosClient from "../axiosClient";

const API_ENDPOINT = "/roles";

const toApiPayload = (form) => {
  if (!form) return {};
  const payload = {};
  Object.entries(form).forEach(([key, value]) => {
    const snake = key.replace(/([A-Z])/g, "_$1").toLowerCase();
    payload[snake] = value;
  });
  return payload;
};

export const getRoles = (params = {}) =>
  axiosClient.get(API_ENDPOINT, { params });
export const getRoleById = (id) => axiosClient.get(`${API_ENDPOINT}/${id}`);
export const createRole = (data) =>
  axiosClient.post(API_ENDPOINT, toApiPayload(data));
export const updateRole = (id, data) =>
  axiosClient.put(`${API_ENDPOINT}/${id}`, toApiPayload(data));
export const deleteRole = (id) => axiosClient.delete(`${API_ENDPOINT}/${id}`);

export default {
  getRoles,
  getRoleById,
  createRole,
  updateRole,
  deleteRole,
};

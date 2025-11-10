import axiosClient from "../axiosClient";

// Usuario autenticado (requiere auth:sanctum)
export const getCurrentUser = () => {
  return axiosClient.get(`/user`);
};

const toApiPayload = (form) => {
  if (!form) return {};
  const payload = {};
  Object.entries(form).forEach(([key, value]) => {
    const snake = key.replace(/([A-Z])/g, "_$1").toLowerCase();
    payload[snake] = value;
  });
  return payload;
};

export const getUsuarioById = (id) => axiosClient.get(`/usuarios/${id}`);

export const patchUsuario = (id, data) =>
  axiosClient.patch(`/usuarios/${id}`, toApiPayload(data));

export default { getCurrentUser };

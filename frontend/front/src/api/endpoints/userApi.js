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

// List users with optional filters: nombre, email, rol
export const getUsuarios = (params = {}) =>
  axiosClient.get(`/usuarios`, { params });

// Delete a user by id
export const deleteUsuario = (id) => axiosClient.delete(`/usuarios/${id}`);

// Admin update: backend exposes a special admin route to update user including roles
// Example: POST /usuarios/:id/admin with body {_method: 'PATCH', nombre: 'Nuevo', roles: [1,2]}
export const adminPatchUsuario = (id, data) =>
  axiosClient.patch(`/usuarios/${id}/admin`, toApiPayload(data));

export default { getCurrentUser };

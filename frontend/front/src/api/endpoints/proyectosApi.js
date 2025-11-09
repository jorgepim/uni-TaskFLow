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
export const getMyProyectos = (params = {}) =>
  axiosClient.get(`/usuarios/me/proyectos`, { params });
export const getProyectoById = (id) => axiosClient.get(`${API_ENDPOINT}/${id}`);
export const createProyecto = (data) =>
  axiosClient.post(API_ENDPOINT, toApiPayload(data));
export const updateProyecto = (id, data) =>
  axiosClient.put(`${API_ENDPOINT}/${id}`, toApiPayload(data));
export const deleteProyecto = (id) =>
  axiosClient.delete(`${API_ENDPOINT}/${id}`);

// Obtener usuarios no asignados a un proyecto (acepta filtros: nombre, email)
export const getUsuariosNoAsignados = (projectId, params = {}) =>
  axiosClient.get(`/proyectos/${projectId}/usuarios/no-asignados`, { params });

// Asignar un usuario a un proyecto: { usuario_id, rol_proyecto }
export const assignUsuarioProyecto = (projectId, body) =>
  axiosClient.post(`/proyectos/${projectId}/usuarios`, toApiPayload(body));

// Obtener usuarios asignados al proyecto (acepta filtro: nombre)
export const getUsuariosAsignados = (projectId, params = {}) =>
  axiosClient.get(`/proyectos/${projectId}/usuarios`, { params });

// Eliminar asignación de un usuario del proyecto
export const deleteUsuarioProyecto = (projectId, usuarioId) =>
  axiosClient.delete(`/proyectos/${projectId}/usuarios/${usuarioId}`);

export default {
  getProyectos,
  getProyectoById,
  createProyecto,
  updateProyecto,
  getUsuariosNoAsignados,
  assignUsuarioProyecto,
  getUsuariosAsignados,
  deleteUsuarioProyecto,
  deleteProyecto,
};

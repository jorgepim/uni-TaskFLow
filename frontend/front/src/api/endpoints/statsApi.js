import axiosClient from "../axiosClient";

export const getProyectosStats = (params = {}) =>
  axiosClient.get(`/stats/proyectos`, { params });

export const getTareasStats = (params = {}) =>
  axiosClient.get(`/stats/tareas`, { params });

export default { getProyectosStats, getTareasStats };

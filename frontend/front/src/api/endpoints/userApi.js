import axiosClient from "../axiosClient";

// Usuario autenticado (requiere auth:sanctum)
export const getCurrentUser = () => {
  return axiosClient.get(`/user`);
};

export default { getCurrentUser };

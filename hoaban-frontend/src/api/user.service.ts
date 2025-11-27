import http from "./http";
import type { AxiosRequestConfig } from "axios";

// align to backend AuthController endpoints
export const me = (config?: AxiosRequestConfig) => http.get("/auth/me", config);

// User profile and points
export const getUserPoints = () => http.get("/v1/loyalty/me/points");

export const updatePassword = (data: { currentPassword: string; newPassword: string }) =>
  http.post("/auth/change-password", data);

export const updateUserInfo = (data: {
  avatarUrl?: string;
  fullName?: string;
  phone?: string;
  address?: string;
}) => http.put("/auth/profile", data);

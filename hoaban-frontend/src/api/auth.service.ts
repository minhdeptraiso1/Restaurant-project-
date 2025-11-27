import http from "./http";

export const login = (p: { email: string; password: string }) => http.post("/auth/login", p);

export const refresh = (p: { refreshToken: string }) => http.post("/auth/refresh", p);

export const logout = () => http.post("/auth/logout", {});

export const me = () => http.get("/auth/me", { skipAuthRedirect: true } as any);

export const register = (p: { fullName: string; email: string; password: string }) =>
  http.post("/auth/register", p);

export const changePassword = (p: { oldPassword: string; newPassword: string }) =>
  http.post("/auth/change-password", p);

export const requestPasswordOtp = (p: { email: string }) =>
  http.post("/v1/auth/password/request-otp", p);

export const verifyPasswordOtp = (p: { email: string; code: string; newPassword: string }) =>
  http.post("/v1/auth/password/verify-otp", p);

import http from "./http";
import type {
  LoginRequest,
  RegisterRequest,
  ChangePasswordRequest,
  RefreshTokenRequest,
  RequestPasswordOtpRequest,
  VerifyPasswordOtpRequest,
  AuthResponse,
  UserInfo,
} from "@/types/auth.types";

export const login = (payload: LoginRequest) => http.post<AuthResponse>("/auth/login", payload);

export const refresh = (payload: RefreshTokenRequest) =>
  http.post<AuthResponse>("/auth/refresh", payload);

export const logout = () => http.post("/auth/logout", {});

export const me = () => http.get<UserInfo>("/auth/me", { skipAuthRedirect: true } as any);

export const register = (payload: RegisterRequest) =>
  http.post<AuthResponse>("/auth/register", payload);

export const changePassword = (payload: ChangePasswordRequest) =>
  http.post("/auth/change-password", payload);

export const requestPasswordOtp = (payload: RequestPasswordOtpRequest) =>
  http.post("/v1/auth/password/request-otp", payload);

export const verifyPasswordOtp = (payload: VerifyPasswordOtpRequest) =>
  http.post("/v1/auth/password/verify-otp", payload);

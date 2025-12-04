export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RequestPasswordOtpRequest {
  email: string;
}

export interface VerifyPasswordOtpRequest {
  email: string;
  code: string;
  newPassword: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: UserInfo;
}

export interface UserInfo {
  id: string;
  email: string;
  fullName: string;
  role?: string;
  phone?: string;
  address?: string;
  avatar?: string;
}

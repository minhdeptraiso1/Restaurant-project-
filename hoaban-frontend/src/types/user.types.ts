export type UserRole = "CUSTOMER" | "STAFF" | "ADMIN";
export type UserStatus = "ACTIVE" | "INACTIVE" | "BANNED";

export interface User {
  id: string;
  email: string;
  fullName: string;
  phone: string;
  address?: string;
  role: UserRole;
  status: UserStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  email: string;
  password: string;
  fullName: string;
  phone: string;
  address?: string;
  role: UserRole;
  status?: UserStatus;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

export interface ListUsersParams {
  page?: number;
  size?: number;
  role?: UserRole;
  status?: UserStatus;
  search?: string;
}

export interface UpdateUserInfoRequest {
  fullName?: string;
  phone?: string;
  address?: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

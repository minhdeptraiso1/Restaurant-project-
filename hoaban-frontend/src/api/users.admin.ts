import http from "./http";

export type User = {
  id: string;
  avatarUrl?: string;
  fullName: string;
  email: string;
  phone: string;
  role: string;
  address?: string;
  status: string;
};

// API Response wrapper type
export type ApiResponse<T> = {
  code: number;
  message: string;
  data: T;
};

// Paginated response type
export type PaginatedResponse<T> = {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

// List users with pagination - matches GET /v1/users
// API trả về trực tiếp Page object: { content: [], number: 0, size: 12, totalElements: 0, totalPages: 0 }
export const listUsers = (params?: {
  page?: number;
  size?: number;
  search?: string;
  role?: string;
  status?: string;
}) => http.get<PaginatedResponse<User>>("/v1/users", { params });

// Create user - matches POST /v1/users
export const createUser = (p: {
  fullName: string;
  email: string;
  password: string;
  role: string;
}) => http.post<ApiResponse<User>>("/v1/users", p);

// Get user by ID - matches GET /v1/users/{id}
export const getUserById = (id: string) => http.get<ApiResponse<User>>(`/v1/users/${id}`);

// Delete user - matches DELETE /v1/users/{id}
export const deleteUser = (id: string) => http.delete<ApiResponse<string>>(`/v1/users/${id}`);

// Update user role - matches PATCH /v1/users/{id}/role
export const updateUserRole = (id: string, role: string) =>
  http.patch<ApiResponse<User>>(`/v1/users/${id}/role`, { role });

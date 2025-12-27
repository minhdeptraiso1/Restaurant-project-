import http from "./http";
import type { Dish, CreateDishRequest, UpdateDishRequest } from "@/types/dish.types";
import type { PaginatedResponse } from "@/types/user.types";

export interface ListDishesAdminParams {
  page?: number;
  size?: number;
  sortBy?: string;
  direction?: string;
}

export const listDishesAdmin = (params?: ListDishesAdminParams) =>
  http.get<PaginatedResponse<Dish>>("/v1/dishes/all", { params });

export const createDish = (payload: CreateDishRequest) => http.post<Dish>("/v1/dishes", payload);

export const updateDish = (id: string, payload: UpdateDishRequest) =>
  http.put<Dish>(`/v1/dishes/${id}`, payload);

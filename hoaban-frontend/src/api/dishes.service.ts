import http from "./http";
import type { Dish, ListDishesParams } from "@/types/dish.types";
import type { PaginatedResponse } from "@/types/user.types";

export const listDishes = (params?: ListDishesParams) =>
  http.get<PaginatedResponse<Dish>>("/v1/dishes", {
    params: {
      page: params?.page ?? 0,
      size: params?.size ?? 1000, // Lấy 1000 món để hiển thị tất cả
    },
  });

export const getDish = (id: string) => http.get<Dish>(`/v1/dishes/${id}`);

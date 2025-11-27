import http from "./http";

export type Dish = {
  id: string;
  name: string;
  price: number;
  imageUrl?: string;
  description?: string;
};

export const listDishes = (params?: { page?: number; size?: number }) =>
  http.get<Dish[]>("/v1/dishes", {
    params: {
      page: params?.page ?? 0,
      size: params?.size ?? 1000, // Lấy 1000 món để hiển thị tất cả
    },
  });

export const getDish = (id: string) => http.get<Dish>(`/v1/dishes/${id}`);

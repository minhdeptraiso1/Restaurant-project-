import http from "./http";

export const listDishesAdmin = (params?: {
  page?: number;
  size?: number;
  sortBy?: string;
  direction?: string;
}) => http.get("/v1/dishes/all", { params });
export const createDish = (p: {
  categoryId: string;
  name: string;
  description?: string;
  unit: string;
  price: number;
  imageUrl?: string;
  status?: string;
}) => http.post("/v1/dishes", p);

export const updateDish = (
  id: string,
  p: {
    categoryId?: string;
    name?: string;
    description?: string;
    unit?: string;
    price?: number;
    imageUrl?: string;
    status?: string;
  }
) => http.put(`/v1/dishes/${id}`, p);

export const deleteDish = (id: string) => http.delete(`/v1/dishes/${id}`);

import http from "./http";

export const listCombos = () => http.get("/v1/combos/all");

export const getCombo = (id: string) => http.get(`/v1/combos/${id}`);

export const createCombo = (p: {
  name: string;
  description?: string;
  price: number;
  imageUrl?: string;
  status?: string;
  items: Array<{
    dishId: string;
    quantity: number;
  }>;
}) => http.post("/v1/combos", p);

export const updateCombo = (
  id: string,
  p: {
    name?: string;
    description?: string;
    price?: number;
    imageUrl?: string;
    status?: string;
    items?: Array<{
      dishId: string;
      quantity: number;
    }>;
  }
) => http.put(`/v1/combos/${id}`, p);

export const deleteCombo = (id: string) => http.delete(`/v1/combos/${id}`);

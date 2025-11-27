import http from "./http";


export type Combo = {
  id: string;
  name: string;
  description?: string;
  price: number;
  imageUrl?: string;
  status: "ACTIVE" | "INACTIVE" | string;
  items?: Array<{
    dishId: string;
    dishName: string;
    quantity: number;
  }>;
  suggestedSum?: number;
};

export const listCombos = () => http.get<Combo[]>("/v1/combos");


export const getCombo = (id: string) => http.get<Combo>(`/v1/combos/${id}`);

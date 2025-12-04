export type ComboStatus = "ACTIVE" | "INACTIVE";

export interface ComboItem {
  dishId: string;
  dishName: string;
  quantity: number;
}

export interface Combo {
  id: string;
  name: string;
  description?: string;
  price: number;
  imageUrl?: string;
  status: ComboStatus;
  items?: ComboItem[];
  suggestedSum?: number;
}

export interface CreateComboRequest {
  name: string;
  description?: string;
  price: number;
  imageUrl?: string;
  status?: ComboStatus;
  items: ComboItem[];
}

export interface UpdateComboRequest {
  name?: string;
  description?: string;
  price?: number;
  imageUrl?: string;
  status?: ComboStatus;
  items?: ComboItem[];
}

export interface SuggestedMenuDto {
  dishes: any[]; // Will be Dish[] when imported
  combos: Combo[];
}

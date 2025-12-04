export type Unit = "PORTION" | "PLATE" | "BOWL" | "GLASS";
export type MenuStatus = "ACTIVE" | "INACTIVE";

export interface Dish {
  id: string;
  categoryId: string;
  categoryName?: string;
  name: string;
  description?: string;
  unit: Unit;
  price: number;
  imageUrl?: string;
  signature?: boolean;
  status: MenuStatus;
}

export interface CreateDishRequest {
  categoryId: string;
  name: string;
  description?: string;
  unit: Unit;
  price: number;
  imageUrl?: string;
  signature: boolean;
  status: MenuStatus;
}

export interface UpdateDishRequest extends CreateDishRequest {}

export interface ListDishesParams {
  page?: number;
  size?: number;
}

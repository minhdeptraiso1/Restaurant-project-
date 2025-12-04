import type { ItemType } from "./order.types";

export interface CartItem {
  id: string;
  itemType: ItemType;
  itemId: string;
  itemName: string;
  price: number;
  quantity: number;
  subtotal: number;
  imageUrl?: string;
}

export interface Cart {
  id: string;
  customerId: string;
  items: CartItem[];
  subtotal: number;
  createdAt: string;
  updatedAt: string;
}

export interface AddCartItemRequest {
  itemType: ItemType;
  itemId: string;
  quantity: number;
}

export interface UpdateCartItemRequest {
  quantity: number;
}

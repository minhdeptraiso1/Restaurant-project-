import http from "./http";
import type { Cart, AddCartItemRequest, UpdateCartItemRequest } from "@/types/cart.types";

export const getCart = () => http.get<Cart>("/v1/cart");

export const addItem = (payload: AddCartItemRequest) => http.post<Cart>("/v1/cart/items", payload);

export const updateItem = (itemId: string, payload: UpdateCartItemRequest) =>
  http.patch<Cart>(`/v1/cart/items/${itemId}`, payload);

export const removeItem = (itemId: string) => http.delete<Cart>(`/v1/cart/items/${itemId}`);

/** Xóa toàn bộ giỏ hàng */
export const clearCart = () => http.delete(`/v1/cart/items`);

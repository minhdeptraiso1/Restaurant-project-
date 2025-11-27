import http from "./http";

/** Lấy hoặc tạo giỏ hàng DELIVERY */
export const getCart = () => http.get("/v1/cart");

/** Thêm item vào giỏ */
export const addItem = (p: { itemType: "DISH" | "COMBO"; itemId: string; quantity: number }) =>
  http.post("/v1/cart/items", p);

/** Update item (số lượng) */
export const updateItem = (itemId: string, quantity: number) =>
  http.patch(`/v1/cart/items/${itemId}`, { quantity });

/** Xóa 1 item */
export const removeItem = (itemId: string) => http.delete(`/v1/cart/items/${itemId}`);



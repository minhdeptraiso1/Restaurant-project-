import http from "./http";
import type { OrderStatus, OrderTypes } from "@/types";

/* ===== TYPES ===== */
export type OrderItemDto = {
  id: string;
  itemType: string;
  itemId: string;
  name: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
};

export type OrderDto = {
  id: string;
  userId?: string;
  tableId?: string;
  types: OrderTypes;
  status: OrderStatus;
  subtotal: number;
  discount: number;
  tax: number;
  total: number;
  note?: string;
  items: OrderItemDto[];
  appliedVoucherCode?: string;
};

/* ===== ORDER (DINE-IN + DELIVERY) ===== */

/** Mở order tại bàn bằng QR */
export const openByQr = (payload: { qrCode: string; note?: string }) =>
  http.post<OrderDto>("/v1/orders/open-by-qr", payload).then((r) => r.data);

/** Staff mở order dine-in */
export const openOrderByStaff = (p: { reservationId?: string; tableId?: string; note?: string }) =>
  http.post("/v1/orders/open", p);

/** Customer tự tạo order DELIVERY (từ cart) */
export const openOrderSelf = (p: { note?: string }) => http.post("/v1/orders/open", p);

/** Thêm item cho DINE-IN order */
export const addOrderItem = (
  orderId: string,
  p: { itemType: "DISH" | "COMBO"; itemId: string; quantity: number }
) => http.post(`/v1/orders/${orderId}/items`, p);

/** Áp voucher bằng code */
export const applyVoucherByCode = (orderId: string, code: string) =>
  http.post(`/v1/orders/${orderId}/apply-voucher`, { code });

/** Áp voucher thuộc user */
export const applyUserVoucher = (orderId: string, userVoucherId: string) =>
  http.post(`/v1/orders/${orderId}/apply-user-voucher/${userVoucherId}`, {});

/** Thanh toán order (CASH hoặc COD) */
export const payOrder = (id: string, p: { method: "CASH" | "COD"; amount: number }) =>
  http.post(`/v1/orders/${id}/pay`, p);

/** Lấy chi tiết 1 đơn */
export const detailOrder = (id: string) => http.get(`/v1/orders/${id}`);

/** Staff/Admin lấy toàn bộ order */
export const getAllOrders = () => http.get(`/v1/orders`);

/** CUSTOMER lấy lịch sử đơn */
export const listOrders = () => http.get(`/v1/orders/my-orders`);

/** Update status */
export const updateOrderStatus = (orderId: string, p: { status: OrderStatus }) =>
  http.patch(`/v1/orders/${orderId}/status`, p);

/** Xóa đơn trống (admin) */
export const clearOrder = () => http.delete(`/v1/orders/cleanup-empty`);

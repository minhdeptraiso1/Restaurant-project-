export type OrderStatus =
  | "PENDING"
  | "CONFIRMED"
  | "PREPARING"
  | "READY"
  | "DELIVERING"
  | "COMPLETED"
  | "CANCELLED";
export type OrderTypes = "DINE_IN" | "DELIVERY";
export type ItemType = "DISH" | "COMBO";

export interface OrderItem {
  id: string;
  itemType: ItemType;
  itemId: string;
  itemName: string;
  price: number;
  quantity: number;
  subtotal: number;
}

export interface Order {
  id: string;
  orderNumber: string;
  customerId: string;
  customerName: string;
  type: OrderTypes;
  status: OrderStatus;
  items: OrderItem[];
  subtotal: number;
  discount: number;
  total: number;
  voucherCode?: string;
  note?: string;
  tableIds?: string[];
  deliveryAddress?: string;
  deliveryFee?: number;
  paymentMethod?: string;
  createdAt: string;
  updatedAt: string;
}

export interface OpenOrderByQrRequest {
  qrCode: string;
  note?: string;
}

export interface OpenOrderByStaffRequest {
  reservationId?: string;
  tableId?: string;
  note?: string;
}

export interface OpenOrderSelfRequest {
  note?: string;
}

export interface AddOrderItemRequest {
  itemType: ItemType;
  itemId: string;
  quantity: number;
}

export interface PayOrderRequest {
  method: "CASH" | "COD";
  amount: number;
}

export interface UpdateOrderStatusRequest {
  status: OrderStatus;
}

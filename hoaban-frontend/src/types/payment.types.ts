export type PaymentMethod = "CASH" | "VNPAY" | "COD";
export type PaymentStatus = "PENDING" | "COMPLETED" | "FAILED" | "REFUNDED";

export interface VNPayCreateResponse {
  code: string;
  message: string;
  url: string;
}

export interface VNPayReturnResponse {
  success: boolean;
  verified: boolean;
  orderId: string;
  responseCode: string;
  message: string;
}

export interface VNPayIPNResponse {
  success: boolean;
  message: string;
}

export interface Payment {
  id: string;
  orderId: string;
  amount: number;
  method: PaymentMethod;
  status: PaymentStatus;
  transactionId?: string;
  createdAt: string;
  updatedAt: string;
}

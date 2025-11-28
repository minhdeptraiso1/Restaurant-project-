import http from "./http";

// VNPay payment response types
export interface VNPayCreateResponse {
  code: string;
  message: string;
  paymentUrl: string;
  txnRef: string; // KHÔNG phải transactionId
}

export interface VNPayReturnResponse {
  success: boolean;
  orderId: string;
  amount: number;
  transactionId: string;
  responseCode: string;
  message: string;
}

export interface VNPayIPNResponse {
  success: boolean;
  message: string;
}

// VNPay API functions
export const createVNPayPayment = (orderId: string, params?: any) =>
  http.post(`/v1/payments/vnpay/create/${orderId}`, params).then((res) => {
    return res.data?.data ?? res.data; // hỗ trợ mọi cấu trúc
  });

export const handleVNPayReturn = (params: Record<string, string>) =>
  http.get<VNPayReturnResponse>("/v1/payments/vnpay/return", { params });

export const handleVNPayIPN = (params: Record<string, string>) =>
  http.get<VNPayIPNResponse>("/v1/payments/vnpay/ipn", { params });

// Payment method types
export type PaymentMethod = "CASH" | "VNPAY";

// Payment handler
export const processPayment = async (
  orderId: string,
  method: PaymentMethod,
  amount: number,
  additionalData?: any
) => {
  if (method === "VNPAY") {
    const payment = await createVNPayPayment(orderId, additionalData);

    if (!payment?.paymentUrl) {
      throw new Error("Không nhận được paymentUrl từ VNPay API");
    }

    window.location.href = payment.paymentUrl;
    return;
  }

  return http.post(`/v1/orders/${orderId}/pay`, { method, amount });
};

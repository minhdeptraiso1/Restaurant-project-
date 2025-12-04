import http from "./http";

// VNPay payment response types
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

// VNPay API functions
export const createVNPayPayment = (orderId: string) =>
  http
    .post<VNPayCreateResponse>("/v1/payments/create_payment", {
      orderId,
      bankCode: null,
      language: "vn",
    })
    .then((res) => res.data ?? res);

export const handleVNPayReturn = (params: Record<string, string>) =>
  http.get<VNPayReturnResponse>("/v1/payments/vnpay_return", { params });

export const handleVNPayIPN = (params: Record<string, string>) =>
  http.get<VNPayIPNResponse>("/v1/payments/ipn", { params });

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
    const res = await createVNPayPayment(orderId);
    if (!res?.url) {
      throw new Error("Không nhận được payment URL từ VNPay API");
    }
    window.location.href = res.url; // MUST REDIRECT
    return;
  }

  return http.post(`/v1/orders/${orderId}/pay`, { method, amount });
};

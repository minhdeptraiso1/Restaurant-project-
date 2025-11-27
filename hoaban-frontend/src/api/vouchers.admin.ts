import http from "./http";

export interface CreateVoucherPayload {
  code: string;
  name: string;
  type: "PERCENT" | "FIXED";
  value: number;
  minOrder: number;
  maxDiscount: number;
  pointCost: number;
  status: "ACTIVE" | "INACTIVE" | "DRAFT";
  description: string;
  startAt: string;
  endAt: string;
}

export type UpdateVoucherPayload = Partial<{
  name: string;
  type: "PERCENT" | "FIXED";
  value: number;
  minOrder: number;
  maxDiscount: number;
  pointCost: number;
  startAt: string;
  endAt: string;
  status: "ACTIVE" | "INACTIVE" | "EXPIRED" | "DRAFT";
}>;

export const vouchersAdminAPI = {
  list: (page = 0, size = 50) => http.get(`/v1/vouchers`, { params: { page, size } }),
  get: (id: string) => http.get(`/v1/vouchers/${id}`),
  create: (data: CreateVoucherPayload) => http.post(`/v1/vouchers`, data),
  update: (id: string, data: UpdateVoucherPayload) => http.patch(`/v1/vouchers/${id}`, data),
  delete: (id: string) => http.delete(`/v1/vouchers/${id}`),
  changeStatus: (id: string, status: string) => http.patch(`/v1/vouchers/${id}`, { status }),
};

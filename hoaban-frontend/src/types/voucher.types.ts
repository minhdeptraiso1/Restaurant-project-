export type VoucherType = "PERCENT" | "FIXED";
export type VoucherStatus = "ACTIVE" | "INACTIVE" | "EXPIRED" | "DRAFT";

export interface Voucher {
  id: string;
  code: string;
  name: string;
  type: VoucherType;
  value: number;
  minOrder: number;
  maxDiscount?: number;
  pointCost: number;
  startAt: string;
  endAt: string;
  status: VoucherStatus;
  description?: string;
}

export interface UserVoucher {
  id: string;
  voucher: Voucher;
  status: "UNUSED" | "USED" | "EXPIRED";
  exchangedAt: string;
  expiresAt: string;
  usedAt?: string;
}

export interface CreateVoucherRequest {
  code: string;
  name: string;
  type: VoucherType;
  value: number;
  minOrder: number;
  maxDiscount?: number;
  pointCost: number;
  startAt: string;
  endAt: string;
  status: VoucherStatus;
  description?: string;
}

export interface UpdateVoucherRequest {
  code?: string;
  name?: string;
  type?: VoucherType;
  value?: number;
  minOrder?: number;
  maxDiscount?: number;
  pointCost?: number;
  startAt?: string;
  endAt?: string;
  status?: VoucherStatus;
  description?: string;
}

import http from "./http";

// NOTE: Aligning public voucher interfaces with admin schema:
// Admin schema fields: code, name, type (PERCENT|FIXED), value, minOrder, maxDiscount, pointCost, startAt, endAt, status
// Previous user-facing naming (discountType/discountValue/minOrderValue/pointsCost/validFrom/validTo) is deprecated.

export interface Voucher {
  id: string;
  code: string;
  name: string;
  description?: string;
  type: "PERCENT" | "FIXED";
  value: number; // percentage (0-100) if type=PERCENT else fixed amount in VND
  minOrder: number; // minimum order total in VND to apply
  maxDiscount: number; // max discount amount in VND when type=PERCENT
  pointCost: number; // points required to exchange
  startAt: string; // ISO datetime
  endAt: string; // ISO datetime
  status: "ACTIVE" | "INACTIVE" | "EXPIRED" | "DRAFT";
  createdAt?: string;
  updatedAt?: string;
}

export interface UserVoucher {
  id: string;
  voucher: Voucher;
  status: "UNUSED" | "USED" | "EXPIRED"; // usage status for this user-owned instance
  exchangedAt: string;
  usedAt?: string;
  expiresAt: string; // may mirror voucher.endAt
}

export const listActiveVouchers = () => http.get("/v1/vouchers", { params: { status: "ACTIVE" } });

// Get voucher detail by ID
export const getVoucherById = (id: string) => http.get(`/v1/vouchers/${id}`);

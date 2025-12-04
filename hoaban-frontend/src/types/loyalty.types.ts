import type { Voucher } from "./voucher.types";

export interface LoyaltyPoints {
  userId: string;
  points: number;
  totalEarned: number;
  totalSpent: number;
  lastUpdated: string;
}

export interface LoyaltyCatalogItem extends Voucher {
  canAfford: boolean;
}

export interface LoyaltyUserVoucher {
  id: string;
  voucher: Voucher;
  status: "UNUSED" | "USED" | "EXPIRED";
  exchangedAt: string;
  expiresAt: string;
  usedAt?: string;
}

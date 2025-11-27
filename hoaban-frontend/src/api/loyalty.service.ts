import http from "./http";

// Loyalty (Customer-side) APIs per spec
// Catalog: vouchers user can afford (filtered by backend using current points & validity)
export const getLoyaltyCatalog = (page = 0, size = 10) =>
  http.get("/v1/loyalty/catalog", { params: { page, size } });

// Points
export const getMyLoyaltyPoints = () => http.get("/v1/loyalty/me/points");

// Wallet (unused user vouchers)
export const getMyVouchers = () => http.get("/v1/loyalty/me/vouchers");

// Redeem by code
export const redeemVoucherByCode = (code: string) => http.post("/v1/loyalty/redeem", { code });

// Redeem by voucher id (catalog item id)
export const redeemVoucherById = (voucherId: string) =>
  http.post(`/v1/loyalty/redeem/${voucherId}`, {});

// Types (keep loose to accommodate both old & new fields while migrating)
export interface LoyaltyVoucher {
  id: string;
  code: string;
  name: string;
  type: "PERCENT" | "FIXED";
  value: number; // percent or fixed amount
  minOrder: number; // minimum order amount
  maxDiscount?: number; // cap for percent type
  pointCost: number; // cost to redeem
  startAt: string;
  endAt: string;
  status: "ACTIVE" | "INACTIVE" | "EXPIRED" | "DRAFT";
  description?: string;
}

export interface LoyaltyUserVoucher {
  id: string; // user-voucher id (used to apply to order)
  voucher: LoyaltyVoucher; // embedded base voucher data
  status: "UNUSED" | "USED" | "EXPIRED";
  exchangedAt: string;
  expiresAt: string;
  usedAt?: string;
}

type RawRecord = Record<string, unknown>;

const isRecord = (value: unknown): value is RawRecord =>
  typeof value === "object" && value !== null && !Array.isArray(value);

const toNonEmptyString = (value: unknown): string | undefined => {
  if (typeof value === "string") {
    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed : undefined;
  }

  if (typeof value === "number" && Number.isFinite(value)) {
    return String(value);
  }

  if (typeof value === "bigint") {
    return value.toString();
  }

  if (value instanceof Date) {
    return value.toISOString();
  }

  return undefined;
};

const pickString = (source: RawRecord, keys: string[], fallback = ""): string => {
  for (const key of keys) {
    const candidate = toNonEmptyString(source[key]);
    if (candidate !== undefined) {
      return candidate;
    }
  }
  return fallback;
};

const pickNumber = (source: RawRecord, keys: string[], fallback = 0): number => {
  for (const key of keys) {
    const value = source[key];
    if (typeof value === "number") {
      return value;
    }
    if (typeof value === "string") {
      const parsed = Number(value);
      if (!Number.isNaN(parsed)) {
        return parsed;
      }
    }
  }
  return fallback;
};

const coerceDateString = (value: unknown): string | undefined => {
  const asString = toNonEmptyString(value);
  if (!asString) {
    return undefined;
  }

  const parsed = new Date(asString);
  if (!Number.isNaN(parsed.getTime())) {
    return parsed.toISOString();
  }

  return asString;
};

const pickDateString = (source: RawRecord, keys: string[], fallback?: string): string => {
  for (const key of keys) {
    const candidate = coerceDateString(source[key]);
    if (candidate) {
      return candidate;
    }
  }

  if (fallback) {
    const coercedFallback = coerceDateString(fallback);
    if (coercedFallback) {
      return coercedFallback;
    }
  }

  return new Date().toISOString();
};

const resolveStatus = (raw: RawRecord): "UNUSED" | "USED" | "EXPIRED" => {
  // Check explicit status field first
  const value = pickString(raw, ["status", "userVoucherStatus"]);
  if (value === "UNUSED" || value === "USED" || value === "EXPIRED") {
    return value;
  }

  // Check redeemed boolean (API returns this field)
  const redeemed = raw.redeemed;
  if (typeof redeemed === "boolean") {
    if (redeemed) {
      return "USED";
    } else {
      return "UNUSED"; // redeemed: false means UNUSED
    }
  }

  // Check usedAt/redeemedAt timestamp
  const usedAt = pickString(raw, ["usedAt", "redeemedAt"]);
  if (usedAt) {
    return "USED";
  }

  // Check if expired by date
  const expiresAt = pickDateString(raw, ["expiresAt", "endAt", "expiredAt"], "");
  if (expiresAt && new Date(expiresAt).getTime() < Date.now()) {
    return "EXPIRED";
  }

  return "UNUSED";
};

export const normalizeUserVoucher = (rawValue: unknown): LoyaltyUserVoucher => {
  const raw = isRecord(rawValue) ? rawValue : {};
  const nested = isRecord(raw.voucher) ? (raw.voucher as RawRecord) : {};

  const rawType = pickString(nested, ["type"], pickString(raw, ["type", "voucherType"], "FIXED"));
  const normalizedType = rawType.toUpperCase().includes("PERCENT") ? "PERCENT" : "FIXED";

  const voucher: LoyaltyVoucher = {
    id: pickString(nested, ["id"], pickString(raw, ["voucherId", "id"], "")),
    code: pickString(nested, ["code"], pickString(raw, ["code", "voucherCode"], "")),
    name: pickString(nested, ["name"], pickString(raw, ["name", "voucherName"], "Voucher")),
    type: normalizedType,
    value: pickNumber(nested, ["value"], pickNumber(raw, ["value", "discountValue"], 0)),
    minOrder: pickNumber(nested, ["minOrder"], pickNumber(raw, ["minOrder", "minimumOrder"], 0)),
    maxDiscount:
      nested.maxDiscount !== undefined || raw.maxDiscount !== undefined
        ? pickNumber(
            nested,
            ["maxDiscount"],
            pickNumber(raw, ["maxDiscount", "maximumDiscount"], 0)
          )
        : undefined,
    pointCost: pickNumber(nested, ["pointCost"], pickNumber(raw, ["pointCost", "pointsCost"], 0)),
    startAt: pickDateString(
      nested,
      ["startAt", "validFrom"],
      pickDateString(raw, ["startAt", "validFrom", "exchangedAt"], new Date().toISOString())
    ),
    endAt: pickDateString(
      nested,
      ["endAt", "validUntil"],
      pickDateString(raw, ["endAt", "expiresAt", "validUntil"], new Date().toISOString())
    ),
    status: pickString(nested, ["status", "voucherStatus"], "ACTIVE") as LoyaltyVoucher["status"],
    description:
      pickString(
        nested,
        ["description", "voucherDescription"],
        pickString(raw, ["description"], "")
      ) || undefined,
  };

  const exchangedAt = pickDateString(
    raw,
    ["exchangedAt", "createdAt", "redeemedAt", "startAt"],
    new Date().toISOString()
  );

  // Default expiresAt to 1 year from now if not provided
  const defaultExpiry = new Date();
  defaultExpiry.setFullYear(defaultExpiry.getFullYear() + 1);
  const expiresAt = pickDateString(
    raw,
    ["expiresAt", "endAt", "validUntil"],
    pickDateString(nested, ["endAt", "validUntil"], defaultExpiry.toISOString())
  );

  const usedAt = pickString(raw, ["usedAt", "redeemedAt"], "");

  return {
    id: pickString(raw, ["id", "userVoucherId", "uuid"], voucher.id),
    voucher,
    status: resolveStatus(raw),
    exchangedAt,
    expiresAt,
    usedAt: usedAt || undefined,
  };
};

const unwrapVoucherPayload = (payload: unknown): unknown => {
  if (Array.isArray(payload)) {
    return payload;
  }

  if (!isRecord(payload)) {
    return payload;
  }

  if (Array.isArray(payload.data)) {
    return payload.data;
  }

  if (isRecord(payload.data) && Array.isArray((payload.data as RawRecord).content)) {
    return (payload.data as RawRecord).content;
  }

  if (Array.isArray(payload.content)) {
    return payload.content;
  }

  return payload;
};

export const extractUserVouchers = (payload: unknown): LoyaltyUserVoucher[] => {
  const unwrapped = unwrapVoucherPayload(payload);
  if (!Array.isArray(unwrapped)) {
    return [];
  }
  return unwrapped.map((item) => normalizeUserVoucher(item));
};

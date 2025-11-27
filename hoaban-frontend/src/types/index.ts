// Central export point for shared application types (DTOs/Responses)
// Import and re-export types from API services as needed to keep imports tidy

export type ApiListResponse<T> = {
  data: T[];
  total?: number;
};

export type ApiItemResponse<T> = {
  data: T;
};

export type Category = {
  id: string;
  name: string;
};

// Chat Types
export type Intent =
  | "GREET"
  | "HELP"
  | "BOOK_TABLE"
  | "RECOMMEND_DISH"
  | "COMBO_DISH"
  | "ORDER_STATUS"
  | "NEGATE"
  | "UNKNOWN";

export interface ApiResponse<T> {
  success: boolean;
  message: string | null;
  code: string | null;
  data: T;
}

export interface ChatReq {
  sessionId: string;
  message: string;
}

export interface ChatResponseData {
  reply: string;
  intent: string;
}

export type ChatRole = "user" | "assistant";

export interface ChatMessage {
  role: ChatRole;
  content: string;
  at: string; // ISO 8601
}

export type ChatResponse = ApiResponse<ChatResponseData>;

// Order Types
export type OrderStatus = "OPEN" | "PAID" | "CANCELLED" | "UNPAID";
export type OrderTypes = "DINE_IN" | "DELIVERY";

// Review Types
export interface Review {
  id: string;
  customerName: string;
  rating: number;
  comment?: string;
  createdAt: string;
}

export interface CreateReviewReq {
  orderId: string;
  rating: number;
  comment?: string;
}

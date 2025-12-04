export interface Review {
  id: string;
  customerName: string;
  rating: number;
  comment?: string;
  createdAt: string;
  avatarUrl?: string;
}

export interface CreateReviewRequest {
  orderId: string;
  rating: number;
  comment?: string;
}

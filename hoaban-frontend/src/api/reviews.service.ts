import http from "./http";
import type { ApiResponse } from "@/types";

export interface Review {
  id: string;
  customerName: string;
  rating: number;
  comment?: string;
  createdAt: string;
  avatarUrl?: string;
}

export interface CreateReviewReq {
  orderId: string;
  rating: number;
  comment?: string;
}

export function createReview(data: CreateReviewReq) {
  return http.post<ApiResponse<Review>>("/v1/reviews", data);
}

export function getLatestReviews() {
  return http.get<ApiResponse<Review[]>>("/v1/reviews/latest");
}

export function getAllReviews() {
  return http.get<ApiResponse<Review[]>>("/v1/reviews/all");
}

export function deleteReview(id: string) {
  return http.delete<ApiResponse<string>>("/v1/reviews", { params: { id } });
}

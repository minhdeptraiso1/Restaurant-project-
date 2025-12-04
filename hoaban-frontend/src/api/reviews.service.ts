import http from "./http";
import type { ApiResponse } from "@/types";
import type { Review, CreateReviewRequest } from "@/types/review.types";

export function createReview(data: CreateReviewRequest) {
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

import http from "./http";
import type {
  Category,
  CreateCategoryRequest,
  UpdateCategoryRequest,
} from "@/types/category.types";

export const listCategories = () => http.get<Category[]>("/v1/categories");

export const createCategory = (payload: CreateCategoryRequest) =>
  http.post<Category>("/v1/categories", payload);

export const updateCategory = (id: string, payload: UpdateCategoryRequest) =>
  http.put<Category>(`/v1/categories/${id}`, payload);

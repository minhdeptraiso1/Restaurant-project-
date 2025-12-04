export type CategoryStatus = "ACTIVE" | "INACTIVE";

export interface Category {
  id: string;
  name: string;
  description?: string;
  status?: CategoryStatus;
}

export interface CreateCategoryRequest {
  name: string;
  description?: string;
  status?: CategoryStatus;
}

export interface UpdateCategoryRequest extends CreateCategoryRequest {}

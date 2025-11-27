import http from "./http";

export const listCategories = () => http.get("/v1/categories");
export const createCategory = (p: { name: string; description?: string; status?: string }) =>
  http.post("/v1/categories", p);
export const updateCategory = (
  id: string,
  p: { name: string; description?: string; status?: string }
) => http.put(`/v1/categories/${id}`, p);

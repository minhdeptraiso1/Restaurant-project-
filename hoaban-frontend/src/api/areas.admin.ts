import http from "./http";
export type AreaStatus = "ACTIVE" | "INACTIVE";

export interface AreaDto {
  id: string;
  name: string;
  description?: string;
  status: AreaStatus;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateAreaReq {
  name: string;
  description?: string;
  status?: AreaStatus;
}
export const listAreas = () => http.get("/v1/areas");
export const createArea = (p: CreateAreaReq) => http.post("/v1/areas", p);
export const updateArea = (id: string, req: CreateAreaReq) =>
  http.put<AreaDto>(`/v1/areas/${id}`, req);

export const deleteArea = (id: string) => http.delete<void>(`/v1/areas/${id}`);

import http from "./http";
import type { AreaDto, CreateAreaReq } from "@/types/area.types";

export const listAreas = () => http.get<AreaDto[]>("/v1/areas");

export const createArea = (payload: CreateAreaReq) => http.post<AreaDto>("/v1/areas", payload);

export const updateArea = (id: string, payload: CreateAreaReq) =>
  http.put<AreaDto>(`/v1/areas/${id}`, payload);

export const deleteArea = (id: string) => http.delete<void>(`/v1/areas/${id}`);

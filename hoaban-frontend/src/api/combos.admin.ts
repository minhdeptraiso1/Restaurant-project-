import http from "./http";
import type { Combo, CreateComboRequest, UpdateComboRequest } from "@/types/combo.types";

export const listCombos = () => http.get<Combo[]>("/v1/combos/all");

export const getCombo = (id: string) => http.get<Combo>(`/v1/combos/${id}`);

export const createCombo = (payload: CreateComboRequest) => http.post<Combo>("/v1/combos", payload);

export const updateCombo = (id: string, payload: UpdateComboRequest) =>
  http.put<Combo>(`/v1/combos/${id}`, payload);

export const deleteCombo = (id: string) => http.delete<void>(`/v1/combos/${id}`);

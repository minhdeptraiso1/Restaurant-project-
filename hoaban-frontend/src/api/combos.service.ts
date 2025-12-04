import http from "./http";
import type { Combo, SuggestedMenuDto } from "@/types/combo.types";

export const listCombos = () => http.get<Combo[]>("/v1/combos");

export const getCombo = (id: string) => http.get<Combo>(`/v1/combos/${id}`);

export const getSuggestedMenu = () => http.get<SuggestedMenuDto>("/v1/combos/suggested");

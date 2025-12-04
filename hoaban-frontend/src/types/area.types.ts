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

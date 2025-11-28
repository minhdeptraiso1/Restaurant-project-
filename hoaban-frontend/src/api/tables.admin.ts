import http from "./http";
export type TableStatus = "AVAILABLE" | "OCCUPIED" | "RESERVED" | "INACTIVE";

export interface TableDto {
  id: string;
  areaId: string;
  seats: number;
  code: string;
  status: TableStatus;
  areaName?: string;
}

export type TableReq = {
  code: string;
  areaId: string;
  seats: number;
  status: "AVAILABLE" | "UNAVAILABLE";
};

export const listTables = () => http.get("/v1/tables");
export const listTablesByArea = (areaId: string) => http.get(`/v1/tables/by-area/${areaId}`);

// Get available tables in time range
export const getAvailableTables = (startTime: string, endTime: string) =>
  http.get("/v1/tables/available", { params: { startTime, endTime } });

// Get available tables by area in time range
export const getAvailableTablesByArea = (areaId: string, startTime: string, endTime: string) =>
  http.get(`/v1/tables/available/by-area/${areaId}`, { params: { startTime, endTime } });

export const createTable = (p: TableReq) => http.post("/v1/tables", p);

export const updateTable = (id: string, data: Partial<TableReq>) =>
  http.put<TableDto>(`/v1/tables/${id}`, data);

export const deleteTable = (id: string) => http.delete<void>(`/v1/tables/${id}`);

// Get QR code for table
export const issueQrCode = (tableId: string, ttlSeconds: number = 1800) =>
  http.post<{ tableId: string; ttlSeconds: number; qrCode: string }>("/v1/tables/issue", null, {
    params: { tableId, ttlSeconds },
  });

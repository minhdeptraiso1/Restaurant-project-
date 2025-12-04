import http from "./http";
import type {
  Table,
  CreateTableRequest,
  UpdateTableRequest,
  QrCodeResponse,
} from "@/types/table.types";

export const listTables = () => http.get<Table[]>("/v1/tables");

export const listTablesByArea = (areaId: string) =>
  http.get<Table[]>(`/v1/tables/by-area/${areaId}`);

// Get available tables in time range
export const getAvailableTables = (startTime: string, endTime: string) =>
  http.get<Table[]>("/v1/tables/available", { params: { startTime, endTime } });

// Get available tables by area in time range
export const getAvailableTablesByArea = (areaId: string, startTime: string, endTime: string) =>
  http.get<Table[]>(`/v1/tables/available/by-area/${areaId}`, { params: { startTime, endTime } });

export const createTable = (payload: CreateTableRequest) => http.post<Table>("/v1/tables", payload);

export const updateTable = (id: string, payload: UpdateTableRequest) =>
  http.put<Table>(`/v1/tables/${id}`, payload);

export const deleteTable = (id: string) => http.delete<void>(`/v1/tables/${id}`);

// Get QR code for table
export const issueQrCode = (tableId: string, ttlSeconds: number = 1800) =>
  http.post<QrCodeResponse>("/v1/tables/issue", null, {
    params: { tableId, ttlSeconds },
  });

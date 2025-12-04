export type TableStatus = "AVAILABLE" | "OCCUPIED" | "RESERVED" | "INACTIVE";

export interface Table {
  id: string;
  areaId: string;
  areaName?: string;
  seats: number;
  code: string;
  status: TableStatus;
}

export interface CreateTableRequest {
  code: string;
  areaId: string;
  seats: number;
  status: "AVAILABLE" | "UNAVAILABLE";
}

export interface UpdateTableRequest extends Partial<CreateTableRequest> {}

export interface QrCodeResponse {
  tableId: string;
  ttlSeconds: number;
  qrCode: string;
}

export interface AvailableTablesQuery {
  startTime: string;
  endTime: string;
}

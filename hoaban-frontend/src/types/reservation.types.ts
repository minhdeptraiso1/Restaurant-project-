export type ReservationStatus = "PENDING" | "CONFIRMED" | "CANCELLED" | "COMPLETED";

export interface Reservation {
  id: string;
  userId: string;
  startTime: string;
  endTime: string;
  partySize: number;
  note?: string;
  status: ReservationStatus;
  tableIds?: string[];
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateReservationRequest {
  startTime: string;
  endTime: string;
  partySize: number;
  note?: string;
}

export interface AssignTablesRequest {
  tableIds: string[];
}

export interface CancelReservationRequest {
  reason?: string;
}

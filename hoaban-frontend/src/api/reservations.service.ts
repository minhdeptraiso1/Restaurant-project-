import http from "./http";

// Customer creates a reservation (PENDING) without tables
export const createReservation = (p: {
  startTime: string;
  endTime: string;
  partySize: number;
  note?: string;
}) => http.post("/v1/reservations", p);

// Customer: own reservations
export const listMyReservations = () => http.get("/v1/reservations/me");
export const cancelMyReservation = (id: string, reason?: string) =>
  http.post(`/v1/reservations/${id}/cancel`, { reason });

// Staff/Admin: list all
export const listAllReservations = (params?: Record<string, any>) =>
  http.get("/v1/reservations", { params });

// Staff/Admin: assign one or many tables (array of tableIds) and confirm
export const assignTables = (id: string, tableIds: string[]) =>
  http.post(`/v1/reservations/${id}/assign-tables`, { tableIds });

// Staff/Admin: cancel (different auditing done backend)
export const staffCancelReservation = (id: string, reason?: string) =>
  http.post(`/v1/reservations/${id}/cancel`, { reason });

export const adminCancelReservation = (id: string, reason?: string) =>
  http.post(`/v1/reservations/${id}/cancel-by-staff`, { reason });

export const getReservation = (id: string) => http.get(`/v1/reservations/${id}`);

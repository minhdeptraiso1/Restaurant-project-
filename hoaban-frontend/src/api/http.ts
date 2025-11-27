import axios from "axios";
import { TokenManager } from "@/utils/cookies";

// Refresh handling state
let isRefreshing = false;
let refreshQueue: { resolve: (v?: any) => void; reject: (e: any) => void; original: any }[] = [];

async function performRefresh(failedConfig: any) {
  const refreshToken = TokenManager.getRefreshToken();
  if (!refreshToken) throw new Error("NO_REFRESH_TOKEN");
  const resp = await axios.post(
    (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api") + "/auth/refresh",
    { refreshToken }
  );
  const payload = resp.data?.data || resp.data; // unwrap if ApiResponse
  const newAccess = payload?.accessToken || payload?.access || payload?.token;
  const newRefresh = payload?.refreshToken;
  if (!newAccess) throw new Error("INVALID_REFRESH_RESPONSE");
  TokenManager.setAccessToken(newAccess);
  if (newRefresh) TokenManager.setRefreshToken(newRefresh);
  // Retry original request with new token
  failedConfig.headers.Authorization = `Bearer ${newAccess}`;
  return http.request(failedConfig);
}

// Helper: trích xuất message thân thiện từ phản hồi lỗi backend
function extractErrorMessage(err: any): string {
  if (!err) return "Lỗi không xác định";
  const r = err.response;
  if (r) {
    // Các cấu trúc phổ biến: {message}, {error}, {errors:[..]}, {details:..}
    const data = r.data || {};
    if (typeof data === "string") return data;
    if (data.message) return data.message;
    if (data.error) return data.error;
    if (Array.isArray(data.errors) && data.errors.length) {
      // Nếu errors là mảng string hoặc object { field, message }
      const first = data.errors[0];
      if (typeof first === "string") return first;
      if (first?.message) return first.message;
    }
    if (data.details) return data.details;
    return `HTTP ${r.status}`;
  }
  if (err.message) return err.message;
  return "Lỗi không xác định";
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
});

http.interceptors.request.use((config) => {
  // Skip authentication for open-by-qr endpoint (guest access)
  const isGuestEndpoint = config.url?.includes("/open-by-qr");

  const token = TokenManager.getAccessToken();
  if (token && !isGuestEndpoint) {
    config.headers.Authorization = `Bearer ${token}`;
    if (import.meta.env.DEV) {
      console.log("🔑 Request with token:", {
        url: config.url,
        hasToken: !!token,
        tokenPreview: token ? `${token.substring(0, 20)}...` : null,
      });
    }
  } else {
    if (import.meta.env.DEV) {
      console.log(
        "⚠️ Request without token:",
        config.url,
        isGuestEndpoint ? "(guest endpoint)" : ""
      );
    }
  }
  return config;
});

http.interceptors.response.use(
  (res) => {
    const d = res?.data;
    if (d && typeof d === "object" && "success" in d && "data" in d) {
      return { ...res, data: d.data };
    }
    return res;
  },
  async (err) => {
    const status = err?.response?.status;
    const originalConfig = err.config || {};
    const friendlyMessage = extractErrorMessage(err);
    err.friendlyMessage = friendlyMessage;

    // 401 handling with refresh retry (once)
    if (status === 401 && !originalConfig._retry && !originalConfig.url?.includes("/auth/login")) {
      originalConfig._retry = true;
      try {
        if (isRefreshing) {
          return await new Promise((resolve, reject) => {
            refreshQueue.push({ resolve, reject, original: originalConfig });
          });
        }
        isRefreshing = true;
        const resp = await performRefresh(originalConfig);
        // Flush queue success
        refreshQueue.forEach((p) => p.resolve(http.request(p.original)));
        refreshQueue = [];
        return resp;
      } catch (refreshErr) {
        refreshQueue.forEach((p) => p.reject(refreshErr));
        refreshQueue = [];
        // Clear tokens & redirect
        TokenManager.clearTokens();
        if (!window.location.pathname.startsWith("/login")) {
          setTimeout(() => (window.location.href = "/login"), 0);
        }
      } finally {
        isRefreshing = false;
      }
    }

    if (import.meta.env.DEV) {
      console.warn("API error:", {
        url: originalConfig?.url,
        status,
        data: err?.response?.data,
        friendlyMessage,
      });
    }
    return Promise.reject(err);
  }
);

export type ApiError = Error & { friendlyMessage?: string };

export default http;

import { defineStore } from "pinia";
import * as authApi from "@/api/auth.service";
import { getUserPoints } from "@/api/user.service";
import { TokenManager } from "@/utils/cookies";

type User = { id: string; fullName: string; email: string; role?: string; points?: number };

export const useAuthStore = defineStore("auth", {
  state: () => {
    // Auto-migrate tokens from localStorage to cookies
    TokenManager.migrateFromLocalStorage();

    return {
      token: TokenManager.getAccessToken() || "",
      user: null as User | null,
      loadedUser: false, // đã cố gắng fetch user (thành công hoặc thất bại)
    };
  },
  actions: {
    async login(email: string, password: string) {
      console.log("Auth store login called", { email, password });
      const { data } = await authApi.login({ email, password });
      console.log("Login response:", data);
      this.token = data.accessToken;
      TokenManager.setAccessToken(data.accessToken);
      if (data.refreshToken) {
        TokenManager.setRefreshToken(data.refreshToken);
      }
      // cố gắng lấy thông tin user nhưng không chặn luồng đăng nhập nếu lỗi
      try {
        await this.fetchMe();
        // Sau khi đăng nhập, đồng bộ giỏ hàng từ server
        try {
          const { useCartStore } = await import("@/stores/cart");
          const cart = useCartStore();
          await cart.load();
        } catch (e) {
          console.warn("Could not sync cart after login", e);
        }
      } catch (e) {
        console.warn("fetchMe failed after login", e);
      }
    },
    async fetchMe() {
      if (!this.token) {
        this.user = null;
        this.loadedUser = true;
        return;
      }
      try {
        const { data } = await authApi.me();
        this.user = data;
        // Sau khi lấy thông tin user, đồng bộ điểm nếu endpoint /auth/me không bao gồm points
        try {
          const { data: pointsResp } = await getUserPoints();
          const pointsValue = typeof pointsResp === "number" ? pointsResp : pointsResp?.points;
          if (pointsValue != null) {
            this.user = { ...this.user!, points: pointsValue };
          }
        } catch (e) {
          // không chặn luồng nếu lỗi
          console.warn("Could not load user points after fetchMe", e);
        }
      } catch (e: any) {
        // Nếu token sai / hết hạn -> xoá token nhưng KHÔNG redirect ở đây để guard quyết định
        if (e?.response?.status === 401) {
          this.token = "";
          TokenManager.clearTokens();
          this.user = null;
        }
        throw e;
      } finally {
        this.loadedUser = true;
        // Ensure cart is loaded once user is known
        if (this.token) {
          try {
            const { useCartStore } = await import("@/stores/cart");
            const cart = useCartStore();
            await cart.load();
          } catch (e) {
            console.warn("Could not load cart after fetchMe", e);
          }
        }
      }
    },
    async logout() {
      try {
        await authApi.logout();
      } catch {}
      this.token = "";
      this.user = null;
      TokenManager.clearTokens();
      window.location.href = "/login";
    },
    async refreshPoints(): Promise<{ old?: number; current: number; delta: number } | void> {
      if (!this.token) return;
      try {
        const old = this.user?.points;
        const { data } = await getUserPoints();
        const current = typeof data === "number" ? data : data?.points ?? 0;
        if (this.user) this.user = { ...this.user, points: current };
        return { old, current, delta: old != null ? current - old : 0 };
      } catch (e) {
        console.warn("Failed to refresh points", e);
      }
    },
    async refreshUserInfo() {
      if (!this.token) return;
      try {
        await this.fetchMe();
      } catch (e) {
        console.warn("Failed to refresh user info", e);
      }
    },
  },
});

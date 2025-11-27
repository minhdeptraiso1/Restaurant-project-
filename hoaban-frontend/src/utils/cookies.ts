// Cookie utility functions
export class CookieManager {
  static set(name: string, value: string, days: number = 7, httpOnly: boolean = false): void {
    let expires = "";
    if (days) {
      const date = new Date();
      date.setTime(date.getTime() + days * 24 * 60 * 60 * 1000);
      expires = "; expires=" + date.toUTCString();
    }

    const secure = window.location.protocol === "https:" ? "; Secure" : "";
    const sameSite = "; SameSite=Strict";
    const httpOnlyFlag = httpOnly ? "; HttpOnly" : "";

    document.cookie = `${name}=${value || ""}${expires}; path=/${secure}${sameSite}${httpOnlyFlag}`;
  }

  static get(name: string): string | null {
    const nameEQ = name + "=";
    const ca = document.cookie.split(";");

    for (let i = 0; i < ca.length; i++) {
      let c = ca[i];
      if (!c) continue;
      while (c.charAt(0) === " ") c = c.substring(1, c.length);
      if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
  }

  static remove(name: string): void {
    document.cookie = `${name}=; Max-Age=-99999999; path=/`;
  }

  static clear(): void {
    const cookies = document.cookie.split(";");
    for (let cookie of cookies) {
      const eqPos = cookie.indexOf("=");
      const name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
      this.remove(name.trim());
    }
  }
}

// Specific token management
export class TokenManager {
  private static readonly ACCESS_TOKEN_KEY = "access_token";
  private static readonly REFRESH_TOKEN_KEY = "refresh_token";
  private static readonly TOKEN_EXPIRY_DAYS = 1; // Access token expires in 1 day
  private static readonly REFRESH_EXPIRY_DAYS = 30; // Refresh token expires in 30 days

  static setAccessToken(token: string): void {
    CookieManager.set(this.ACCESS_TOKEN_KEY, token, this.TOKEN_EXPIRY_DAYS, false);
  }

  static getAccessToken(): string | null {
    return CookieManager.get(this.ACCESS_TOKEN_KEY);
  }

  static setRefreshToken(token: string): void {
    CookieManager.set(this.REFRESH_TOKEN_KEY, token, this.REFRESH_EXPIRY_DAYS, false);
  }

  static getRefreshToken(): string | null {
    return CookieManager.get(this.REFRESH_TOKEN_KEY);
  }

  static clearTokens(): void {
    CookieManager.remove(this.ACCESS_TOKEN_KEY);
    CookieManager.remove(this.REFRESH_TOKEN_KEY);
  }

  static hasValidToken(): boolean {
    return !!this.getAccessToken();
  }

  // Migration helper: Move tokens from localStorage to cookies
  static migrateFromLocalStorage(): void {
    try {
      const accessToken = localStorage.getItem("access_token");
      const refreshToken = localStorage.getItem("refresh_token");

      if (accessToken) {
        this.setAccessToken(accessToken);
        localStorage.removeItem("access_token");
        console.log("🔄 Migrated access token from localStorage to cookies");
      }

      if (refreshToken) {
        this.setRefreshToken(refreshToken);
        localStorage.removeItem("refresh_token");
        console.log("🔄 Migrated refresh token from localStorage to cookies");
      }
    } catch (error) {
      console.warn("Failed to migrate tokens from localStorage:", error);
    }
  }
}

import { HOME_PATH, LOGIN_PATH } from "@/constants/auth";
import { API_SUCCESS_CODE, REFRESH_TOKEN_PATH } from "@/constants/http";
import { sessionStore } from "@/lib/auth/session-store";
import type { ApiResponse } from "@/types/api";

/**
 * 统一业务异常。
 * 当后端返回稳定业务错误码时，前端通过该异常把 code 和 message 向页面层透出。
 */
export class ApiBusinessError extends Error {
  code: number;

  /**
   * 构造业务异常。
   *
   * @param code 后端业务错误码
   * @param message 错误提示
   */
  constructor(code: number, message: string) {
    super(message);
    this.name = "ApiBusinessError";
    this.code = code;
  }
}

/**
 * 认证失效异常。
 * 当 accessToken 与 refreshToken 都无法继续使用时，用于中断当前请求链路。
 */
export class AuthExpiredError extends Error {
  /**
   * 构造认证失效异常。
   */
  constructor() {
    super("登录状态已失效，请重新登录");
    this.name = "AuthExpiredError";
  }
}

/**
 * 统一请求选项。
 * auth 和 retryOnUnauthorized 负责控制是否带认证头以及是否允许 401 后自动重试。
 */
export interface ApiRequestOptions extends RequestInit {
  auth?: boolean;
  retryOnUnauthorized?: boolean;
}

let refreshPromise: Promise<void> | null = null;

/**
 * 认证失效后执行统一收口。
 * 这里必须同步清理浏览器登录态，并把用户送回登录页而不是继续留在坏状态页面。
 */
function handleAuthExpired(): never {
  sessionStore.clear();

  if (typeof window !== "undefined" && window.location.pathname !== LOGIN_PATH) {
    const redirectPath =
      window.location.pathname + window.location.search + window.location.hash;
    const redirectQuery =
      redirectPath === HOME_PATH ? "" : `?redirect=${encodeURIComponent(redirectPath)}`;

    window.location.replace(`${LOGIN_PATH}${redirectQuery}`);
  }

  throw new AuthExpiredError();
}

/**
 * 统一构造请求头。
 * 当前默认走 JSON 协议，且对受保护请求自动注入 Bearer 令牌。
 */
function buildHeaders(options: ApiRequestOptions): Headers {
  const headers = new Headers(options.headers ?? {});

  if (!headers.has("Accept")) {
    headers.set("Accept", "application/json");
  }

  if (!headers.has("Content-Type") && options.body) {
    headers.set("Content-Type", "application/json");
  }

  if (options.auth !== false) {
    const accessToken = sessionStore.getAccessToken();
    if (accessToken) {
      headers.set("Authorization", `Bearer ${accessToken}`);
    }
  }

  return headers;
}

/**
 * 统一解包后端响应。
 * 用户前台只面向后端统一响应协议编程，不让页面层自己去判 code 和 data。
 */
async function parseApiResponse<T>(response: Response): Promise<ApiResponse<T>> {
  return (await response.json()) as ApiResponse<T>;
}

/**
 * 刷新访问令牌。
 * 这里用单飞控制并发 401，避免多个请求同时打爆刷新接口。
 */
async function refreshAccessToken(): Promise<void> {
  if (refreshPromise) {
    return refreshPromise;
  }

  const refreshToken = sessionStore.getRefreshToken();
  if (!refreshToken) {
    handleAuthExpired();
  }

  refreshPromise = (async () => {
    try {
      const response = await fetch(REFRESH_TOKEN_PATH, {
        method: "POST",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ refreshToken }),
      });

      if (response.status === 401) {
        handleAuthExpired();
      }

      const payload = await parseApiResponse<{
        accessToken: string;
        refreshToken: string;
        username: string;
        displayName?: string;
      }>(response);

      if (payload.code !== API_SUCCESS_CODE || !payload.data) {
        handleAuthExpired();
      }

      sessionStore.setSession({
        accessToken: payload.data.accessToken,
        refreshToken: payload.data.refreshToken,
        username: payload.data.username,
        displayName: payload.data.displayName,
      });
    } catch (error) {
      if (error instanceof AuthExpiredError) {
        throw error;
      }

      handleAuthExpired();
    }
  })().finally(() => {
    refreshPromise = null;
  });

  return refreshPromise;
}

/**
 * 统一 HTTP 请求入口。
 * 负责认证头注入、401 自动刷新重试、统一响应解包和稳定业务异常抛出。
 */
export async function apiRequest<T>(
  url: string,
  options: ApiRequestOptions = {},
): Promise<T> {
  const requestOptions: ApiRequestOptions = {
    method: options.method ?? "GET",
    auth: options.auth ?? true,
    retryOnUnauthorized: options.retryOnUnauthorized ?? true,
    ...options,
  };

  const response = await fetch(url, {
    ...requestOptions,
    headers: buildHeaders(requestOptions),
  });

  /**
   * 认证请求在 401 时只允许重放一次，避免刷新异常导致无限递归。
   */
  if (
    response.status === 401 &&
    requestOptions.auth !== false &&
    requestOptions.retryOnUnauthorized &&
    url !== REFRESH_TOKEN_PATH
  ) {
    await refreshAccessToken();
    return apiRequest<T>(url, {
      ...requestOptions,
      retryOnUnauthorized: false,
    });
  }

  if (response.status === 401) {
    handleAuthExpired();
  }

  const payload = await parseApiResponse<T>(response);
  if (payload.code !== API_SUCCESS_CODE) {
    throw new ApiBusinessError(payload.code, payload.message);
  }

  return payload.data;
}

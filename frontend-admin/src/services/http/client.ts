import { HOME_PATH, LOGIN_PATH } from '@/constants/auth';
import { API_SUCCESS_CODE, REFRESH_TOKEN_PATH } from '@/constants/http';
import type { ApiResponse } from '@/models/api';
import { tokenStore } from '@/utils/token';

/**
 * 统一业务异常。
 * 当后端返回非成功码时，前端通过该错误类型保留稳定的错误码和错误消息。
 */
export class ApiBusinessError extends Error {
  code: number;

  /**
   * 构造业务异常。
   *
   * @param code 后端业务错误码
   * @param message 错误描述
   */
  constructor(code: number, message: string) {
    super(message);
    this.code = code;
    this.name = 'ApiBusinessError';
  }
}

/**
 * 认证过期异常。
 * 用于标记 accessToken 和 refreshToken 都无法继续使用的场景。
 */
export class AuthExpiredError extends Error {
  /**
   * 构造认证过期异常。
   */
  constructor() {
    super('登录状态已失效，请重新登录');
    this.name = 'AuthExpiredError';
  }
}

/**
 * 自定义请求选项。
 * 统一收口是否带认证头、是否允许 401 后重试等认证相关控制项。
 */
export interface ApiRequestOptions extends RequestInit {
  auth?: boolean;
  retryOnUnauthorized?: boolean;
}

let refreshPromise: Promise<void> | null = null;

/**
 * 在认证过期后执行统一收敛。
 * 这里负责清理本地登录态并跳回登录页，避免页面各自实现失效跳转。
 */
function handleAuthExpired(): never {
  tokenStore.clear();

  if (typeof window !== 'undefined' && window.location.pathname !== LOGIN_PATH) {
    const redirectTarget =
      window.location.pathname + window.location.search + window.location.hash;
    const redirectQuery = redirectTarget === HOME_PATH ? '' : `?redirect=${encodeURIComponent(redirectTarget)}`;
    window.location.replace(`${LOGIN_PATH}${redirectQuery}`);
  }

  throw new AuthExpiredError();
}

/**
 * 生成请求头。
 * 这里统一追加 JSON 类型和 Bearer 令牌，避免各个服务文件重复拼接。
 *
 * @param options 原始请求参数
 * @returns 可直接传给 fetch 的 Headers
 */
function buildHeaders(options: ApiRequestOptions): Headers {
  const headers = new Headers(options.headers || {});

  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json');
  }

  if (!headers.has('Content-Type') && options.body) {
    headers.set('Content-Type', 'application/json');
  }

  if (options.auth !== false) {
    const accessToken = tokenStore.getAccessToken();
    if (accessToken) {
      headers.set('Authorization', `Bearer ${accessToken}`);
    }
  }

  return headers;
}

/**
 * 解析 fetch 返回内容。
 * 当前后台统一返回 JSON 包装，因此这里统一解析并做最小健壮性保护。
 *
 * @param response 原始响应
 * @returns 反序列化后的统一响应体
 */
async function parseApiResponse<T>(response: Response): Promise<ApiResponse<T>> {
  const payload = (await response.json()) as ApiResponse<T>;
  return payload;
}

/**
 * 刷新访问令牌。
 * 使用单飞控制避免多个并发 401 同时打爆刷新接口。
 */
async function refreshAccessToken(): Promise<void> {
  if (refreshPromise) {
    return refreshPromise;
  }

  const refreshToken = tokenStore.getRefreshToken();
  if (!refreshToken) {
    handleAuthExpired();
  }

  refreshPromise = (async () => {
    const response = await fetch(REFRESH_TOKEN_PATH, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
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

    tokenStore.setSession({
      accessToken: payload.data.accessToken,
      refreshToken: payload.data.refreshToken,
      username: payload.data.username,
      displayName: payload.data.displayName,
    });
  })().finally(() => {
    refreshPromise = null;
  });

  return refreshPromise;
}

/**
 * 统一 HTTP 请求入口。
 * 这里负责认证头注入、401 刷新重试、统一响应解包和业务异常转换。
 *
 * @param url 接口路径
 * @param options fetch 选项和认证控制项
 * @returns 已解包的后端业务数据
 */
export async function apiRequest<T>(
  url: string,
  options: ApiRequestOptions = {},
): Promise<T> {
  const requestOptions: ApiRequestOptions = {
    method: options.method || 'GET',
    auth: options.auth ?? true,
    retryOnUnauthorized: options.retryOnUnauthorized ?? true,
    ...options,
  };

  const response = await fetch(url, {
    ...requestOptions,
    headers: buildHeaders(requestOptions),
  });

  /**
   * 对访问令牌过期执行一次刷新并重放原请求。
   * 只允许重试一次，避免异常条件下无限循环。
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

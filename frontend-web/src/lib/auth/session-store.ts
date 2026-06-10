import {
  ACCESS_TOKEN_KEY,
  DISPLAY_NAME_KEY,
  REFRESH_TOKEN_KEY,
  USERNAME_KEY,
} from "@/constants/auth";

/**
 * 用户前台登录态对象。
 * 当前前台只保存请求层和页面展示真正需要的最小字段。
 */
export interface UserSession {
  accessToken: string;
  refreshToken: string;
  username: string;
  displayName?: string;
}

/**
 * 判断当前运行环境是否可安全访问 localStorage。
 * App Router 下服务端渲染阶段也会加载模块，这里必须先收口运行环境判断。
 */
function canUseStorage(): boolean {
  return typeof window !== "undefined" && typeof window.localStorage !== "undefined";
}

/**
 * 用户前台登录态存取器。
 * 统一负责 token、用户名和展示名的读写，避免多处各自操作浏览器存储。
 */
export const sessionStore = {
  /**
   * 读取完整登录态。
   * 当关键字段不完整时直接返回 null，避免上层误把半残状态当成已登录。
   */
  getSession(): UserSession | null {
    if (!canUseStorage()) {
      return null;
    }

    const accessToken = window.localStorage.getItem(ACCESS_TOKEN_KEY);
    const refreshToken = window.localStorage.getItem(REFRESH_TOKEN_KEY);
    const username = window.localStorage.getItem(USERNAME_KEY);
    const displayName = window.localStorage.getItem(DISPLAY_NAME_KEY) ?? undefined;

    if (!accessToken || !refreshToken || !username) {
      return null;
    }

    return {
      accessToken,
      refreshToken,
      username,
      displayName,
    };
  },

  /**
   * 读取访问令牌。
   * 请求层优先使用该方法拿 token，避免重复解析完整登录态对象。
   */
  getAccessToken(): string | null {
    return this.getSession()?.accessToken ?? null;
  },

  /**
   * 读取刷新令牌。
   * 访问令牌失效后由统一请求层调用，不允许页面自行拼接刷新逻辑。
   */
  getRefreshToken(): string | null {
    return this.getSession()?.refreshToken ?? null;
  },

  /**
   * 写入完整登录态。
   * 登录成功和刷新成功都通过这里收敛，避免字段命名和写入策略不一致。
   */
  setSession(session: UserSession): void {
    if (!canUseStorage()) {
      return;
    }

    window.localStorage.setItem(ACCESS_TOKEN_KEY, session.accessToken);
    window.localStorage.setItem(REFRESH_TOKEN_KEY, session.refreshToken);
    window.localStorage.setItem(USERNAME_KEY, session.username);

    if (session.displayName) {
      window.localStorage.setItem(DISPLAY_NAME_KEY, session.displayName);
      return;
    }

    window.localStorage.removeItem(DISPLAY_NAME_KEY);
  },

  /**
   * 清理登录态。
   * 认证失效、主动退出和刷新失败都必须走统一清理逻辑。
   */
  clear(): void {
    if (!canUseStorage()) {
      return;
    }

    window.localStorage.removeItem(ACCESS_TOKEN_KEY);
    window.localStorage.removeItem(REFRESH_TOKEN_KEY);
    window.localStorage.removeItem(USERNAME_KEY);
    window.localStorage.removeItem(DISPLAY_NAME_KEY);
  },
};

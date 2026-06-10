import {
  ACCESS_TOKEN_KEY,
  DISPLAY_NAME_KEY,
  REFRESH_TOKEN_KEY,
  USERNAME_KEY,
} from '@/constants/auth';

/**
 * 后台登录态快照。
 * 用于在登录成功后一次性写入令牌和最小展示信息。
 */
export interface AdminSession {
  accessToken: string;
  refreshToken: string;
  username: string;
  displayName?: string;
}

/**
 * 统一 token 与最小用户信息存储工具。
 * 所有登录态读写都必须经过这里，避免业务页面直接操作 localStorage。
 */
export const tokenStore = {
  /**
   * 获取访问令牌。
   *
   * @returns 当前 accessToken，没有则返回空字符串
   */
  getAccessToken(): string {
    return localStorage.getItem(ACCESS_TOKEN_KEY) || '';
  },

  /**
   * 获取刷新令牌。
   *
   * @returns 当前 refreshToken，没有则返回空字符串
   */
  getRefreshToken(): string {
    return localStorage.getItem(REFRESH_TOKEN_KEY) || '';
  },

  /**
   * 获取用户名。
   *
   * @returns 当前用户名，没有则返回空字符串
   */
  getUsername(): string {
    return localStorage.getItem(USERNAME_KEY) || '';
  },

  /**
   * 获取展示名称。
   *
   * @returns 优先返回展示名称，缺失时退化为用户名
   */
  getDisplayName(): string {
    return (
      localStorage.getItem(DISPLAY_NAME_KEY) ||
      localStorage.getItem(USERNAME_KEY) ||
      ''
    );
  },

  /**
   * 判断当前是否存在访问令牌。
   *
   * @returns true 表示前端仍持有 accessToken
   */
  isLoggedIn(): boolean {
    return !!this.getAccessToken();
  },

  /**
   * 写入后台会话。
   *
   * @param session 登录成功后返回的最小会话数据
   */
  setSession(session: AdminSession): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, session.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, session.refreshToken);
    localStorage.setItem(USERNAME_KEY, session.username);
    localStorage.setItem(DISPLAY_NAME_KEY, session.displayName || session.username);
  },

  /**
   * 清理全部登录态。
   * 用于退出登录、刷新失败或令牌失效后的统一收敛。
   */
  clear(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USERNAME_KEY);
    localStorage.removeItem(DISPLAY_NAME_KEY);
  },
};

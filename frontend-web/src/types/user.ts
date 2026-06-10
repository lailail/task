/**
 * 注册请求对象。
 * 对齐当前 user-service 的最小输入字段。
 */
export interface RegisterRequest {
  username: string;
  password: string;
  displayName: string;
}

/**
 * 登录请求对象。
 * 当前登录只要求用户名和密码。
 */
export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * 登录结果对象。
 * 对齐 JWT 登录响应，用于统一写入本地登录态。
 */
export interface LoginResponse {
  userId: number;
  username: string;
  displayName: string;
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  accessTokenExpireAt: string;
  refreshTokenExpireAt: string;
}

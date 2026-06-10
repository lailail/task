import { apiRequest } from '@/services/http/client';

/**
 * 登录请求。
 * 与后端 UserLoginRequest 保持一致，只传最小认证字段。
 */
export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * 登录响应。
 * 与后端 UserLoginResponse 对齐，前端只保存当前阶段需要的字段。
 */
export interface LoginResponse {
  userId: number;
  username: string;
  displayName?: string;
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  accessTokenExpireAt: string;
  refreshTokenExpireAt: string;
}

/**
 * 调用登录接口。
 *
 * @param data 登录表单
 * @returns 登录成功后的会话结果
 */
export function login(data: LoginRequest): Promise<LoginResponse> {
  return apiRequest<LoginResponse>('/api/v1/users/login', {
    method: 'POST',
    auth: false,
    body: JSON.stringify(data),
  });
}

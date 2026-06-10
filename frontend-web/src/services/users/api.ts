import { apiRequest } from "@/lib/http/api-client";
import type { LoginRequest, LoginResponse, RegisterRequest } from "@/types/user";

/**
 * 调用注册接口。
 * 当前前台第一轮只提交后端已要求的最小注册字段。
 */
export function registerUser(request: RegisterRequest): Promise<{
  userId: number;
  username: string;
  displayName: string;
}> {
  return apiRequest("/api/v1/users/register", {
    auth: false,
    method: "POST",
    body: JSON.stringify(request),
  });
}

/**
 * 调用登录接口。
 * 登录成功后由页面层把结果写入统一登录态存储。
 */
export function loginUser(request: LoginRequest): Promise<LoginResponse> {
  return apiRequest<LoginResponse>("/api/v1/users/login", {
    auth: false,
    method: "POST",
    body: JSON.stringify(request),
  });
}

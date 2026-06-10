import { API_SUCCESS_CODE } from "@/constants/http";
import type { ApiResponse } from "@/types/api";
import { ApiBusinessError } from "@/lib/http/api-client";

/**
 * 服务端获取网关地址。
 * 首页和详情页采用服务端取数时，需要显式走网关真实地址，不能依赖浏览器侧重写。
 */
function getGatewayBaseUrl(): string {
  return process.env.TICKET_WEB_GATEWAY_BASE_URL ?? "http://localhost:8080";
}

/**
 * 服务端统一请求入口。
 * 当前只用于公开查询接口，保持 no-store，避免演示站点把旧活动状态缓存成静态页面。
 */
export async function serverApiRequest<T>(path: string): Promise<T> {
  const response = await fetch(`${getGatewayBaseUrl()}${path}`, {
    cache: "no-store",
    headers: {
      Accept: "application/json",
    },
  });

  const payload = (await response.json()) as ApiResponse<T>;
  if (payload.code !== API_SUCCESS_CODE) {
    throw new ApiBusinessError(payload.code, payload.message);
  }

  return payload.data;
}

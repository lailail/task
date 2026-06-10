import { apiRequest } from "@/lib/http/api-client";
import type { ReserveRequest, ReserveResponse } from "@/types/seckill";

/**
 * 发起抢票预扣请求。
 * 当前前台只负责提交活动、票种、数量与幂等字段，用户身份由网关透传。
 */
export function reserveTicket(request: ReserveRequest): Promise<ReserveResponse> {
  return apiRequest<ReserveResponse>("/api/v1/seckill/reservations", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

import { apiRequest } from "@/lib/http/api-client";
import type { ReservationResult, UserOrderPage } from "@/types/order";

/**
 * 查询当前用户自己的订单分页。
 * 用户身份由网关认证头透传，前端只提交分页参数，不允许传 userId。
 */
export function queryMyOrders(
  current = 1,
  pageSize = 10,
): Promise<UserOrderPage> {
  const query = new URLSearchParams({
    current: String(current),
    pageSize: String(pageSize),
  });

  return apiRequest<UserOrderPage>(`/api/v1/orders?${query.toString()}`);
}

/**
 * 查询当前用户某次抢票的真实结果。
 * 该接口会在后端校验 reservationId 归属，前端不做越权兜底假设。
 */
export function queryReservationResult(
  reservationId: string,
): Promise<ReservationResult> {
  return apiRequest<ReservationResult>(
    `/api/v1/orders/reservations/${encodeURIComponent(reservationId)}`,
  );
}

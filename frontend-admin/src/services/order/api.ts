import { apiRequest } from '@/services/http/client';

/**
 * 订单状态查询结果。
 * 与后端 OrderStatusResponse 对齐。
 */
export interface OrderStatusResponse {
  orderId: number;
  orderStatus: string;
}

/**
 * 订单取消请求。
 * 与后端 OrderCancelRequest 对齐，用于后台人工取消待支付订单。
 */
export interface OrderCancelRequest {
  requestId: string;
  reason: string;
}

/**
 * 查询订单状态。
 *
 * @param orderId 订单主键
 * @returns 当前订单状态视图
 */
export function queryOrderStatus(orderId: string): Promise<OrderStatusResponse> {
  return apiRequest<OrderStatusResponse>(
    `/api/v1/internal/orders/${orderId}/status`,
  );
}

/**
 * 取消指定订单。
 *
 * @param orderId 订单主键
 * @param data 取消原因和请求标识
 * @returns 空响应
 */
export function cancelOrder(
  orderId: string,
  data: OrderCancelRequest,
): Promise<void> {
  return apiRequest<void>(`/api/v1/orders/${orderId}/cancel`, {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

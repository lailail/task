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

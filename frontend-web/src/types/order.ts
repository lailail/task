/**
 * 用户侧订单条目。
 * 对齐 order-service 的“我的订单”DTO，只暴露页面展示需要的最小订单事实。
 */
export interface UserOrder {
  orderId: number;
  orderNo: string;
  reservationId: string;
  activityId: number;
  ticketId: number;
  quantity: number;
  amountCent: number;
  orderStatus: string;
  expireAt?: string;
  paidAt?: string;
  closedAt?: string;
}

/**
 * 用户侧订单分页。
 * 当前接口必须分页，避免“我的订单”在高频入口退化成无界查询。
 */
export interface UserOrderPage {
  current: number;
  pageSize: number;
  total: number;
  records: UserOrder[];
}

/**
 * 预扣结果感知响应。
 * 用于结果页按 reservationId 读取真实预扣与订单状态，而不是只信任 URL 参数。
 */
export interface ReservationResult {
  reservationId: string;
  resultStatus: string;
  reservationStatus?: string;
  orderId?: number;
  orderNo?: string;
  orderStatus?: string;
  activityId?: number;
  ticketId?: number;
  quantity?: number;
  amountCent?: number;
  expireAt?: string;
  paidAt?: string;
  closedAt?: string;
}

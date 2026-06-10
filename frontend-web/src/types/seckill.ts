/**
 * 抢票预扣请求对象。
 * requestId 和 idempotencyKey 由前端生成，userId 不再由前端传递。
 */
export interface ReserveRequest {
  requestId: string;
  idempotencyKey: string;
  activityId: number;
  ticketId: number;
  quantity: number;
}

/**
 * 抢票预扣响应对象。
 * 用于抢票结果页展示预扣是否成功以及结果过期时间。
 */
export interface ReserveResponse {
  reservationId: string;
  status: string;
  expireAt: string;
}

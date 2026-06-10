import { apiRequest } from '@/services/http/client';

/**
 * 预扣记录查询参数。
 * 与后端 ReservationRecordQueryRequest 对齐，并补充分页参数。
 */
export interface ReservationRecordQuery {
  reservationId?: string;
  requestId?: string;
  userId?: number;
  activityId?: number;
  ticketId?: number;
  reservationStatus?: string;
  current?: number;
  pageSize?: number;
}

/**
 * 预扣记录视图。
 * 与后端 ReservationRecordResponse 对齐，用于后台展示正式预扣事实。
 */
export interface ReservationRecord {
  reservationId: string;
  requestId?: string;
  userId?: number;
  orderId?: number;
  activityId?: number;
  ticketId?: number;
  quantity?: number;
  reservationStatus: string;
  source?: string;
  reason?: string;
  expireAt?: string;
  releasedAt?: string;
}

/**
 * 预扣记录分页响应。
 * 与后端 ReservationRecordPageResponse 对齐，避免前端自行猜测分页字段。
 */
export interface ReservationRecordPage {
  records: ReservationRecord[];
  total: number;
  current: number;
  pageSize: number;
}

/**
 * 查询预扣记录分页列表。
 *
 * @param params 过滤条件和分页参数
 * @returns 分页后的预扣记录视图
 */
export function queryReservationRecords(
  params: ReservationRecordQuery,
): Promise<ReservationRecordPage> {
  const query = new URLSearchParams();

  if (params.reservationId) {
    query.set('reservationId', params.reservationId);
  }
  if (params.requestId) {
    query.set('requestId', params.requestId);
  }
  if (typeof params.userId === 'number' && !Number.isNaN(params.userId)) {
    query.set('userId', String(params.userId));
  }
  if (
    typeof params.activityId === 'number' &&
    !Number.isNaN(params.activityId)
  ) {
    query.set('activityId', String(params.activityId));
  }
  if (typeof params.ticketId === 'number' && !Number.isNaN(params.ticketId)) {
    query.set('ticketId', String(params.ticketId));
  }
  if (params.reservationStatus) {
    query.set('reservationStatus', params.reservationStatus);
  }
  if (typeof params.current === 'number') {
    query.set('current', String(params.current));
  }
  if (typeof params.pageSize === 'number') {
    query.set('pageSize', String(params.pageSize));
  }

  const suffix = query.toString() ? `?${query.toString()}` : '';
  return apiRequest<ReservationRecordPage>(
    `/api/v1/internal/reservation-records${suffix}`,
  );
}

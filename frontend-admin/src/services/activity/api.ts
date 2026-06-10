import { apiRequest } from '@/services/http/client';

/**
 * 票种摘要。
 * 用于活动详情页展示可售票种和库存。
 */
export interface TicketItem {
  ticketId: number;
  ticketName: string;
  price: number;
  availableStock: number;
}

/**
 * 活动列表项。
 * 与后端 ActivitySummaryResponse 对齐。
 */
export interface ActivitySummary {
  activityId: number;
  activityName: string;
  city: string;
  venueName: string;
  saleStatus: string;
}

/**
 * 活动详情。
 * 在列表摘要基础上追加票种明细。
 */
export interface ActivityDetail extends ActivitySummary {
  ticketItems: TicketItem[];
}

/**
 * 查询活动列表。
 *
 * @returns 当前可展示的活动列表
 */
export function queryActivities(): Promise<ActivitySummary[]> {
  return apiRequest<ActivitySummary[]>('/api/v1/activities');
}

/**
 * 查询活动详情。
 *
 * @param activityId 活动主键
 * @returns 目标活动的详情信息
 */
export function queryActivityDetail(activityId: string): Promise<ActivityDetail> {
  return apiRequest<ActivityDetail>(`/api/v1/activities/${activityId}`);
}

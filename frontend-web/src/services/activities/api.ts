import { apiRequest } from "@/lib/http/api-client";
import { serverApiRequest } from "@/lib/http/server-api";
import type { ActivityDetail, ActivitySummary } from "@/types/activity";

/**
 * 查询活动列表。
 * 当前首页与活动选择区域统一复用该接口，不额外造前端聚合接口。
 */
export function queryActivities(): Promise<ActivitySummary[]> {
  return apiRequest<ActivitySummary[]>("/api/v1/activities", {
    auth: false,
  });
}

/**
 * 服务端查询活动列表。
 * 首页首屏使用服务端取数，避免客户端首屏再发一次加载请求。
 */
export function queryActivitiesOnServer(): Promise<ActivitySummary[]> {
  return serverApiRequest<ActivitySummary[]>("/api/v1/activities");
}

/**
 * 查询活动详情。
 * 当前详情页只依赖活动摘要与票种集合，不扩额外字段假设。
 */
export function queryActivityDetail(activityId: string): Promise<ActivityDetail> {
  return apiRequest<ActivityDetail>(`/api/v1/activities/${activityId}`, {
    auth: false,
  });
}

/**
 * 服务端查询活动详情。
 * 活动详情页首屏直接拿到真实数据，再把抢票动作下放到客户端组件。
 */
export function queryActivityDetailOnServer(
  activityId: string,
): Promise<ActivityDetail> {
  return serverApiRequest<ActivityDetail>(`/api/v1/activities/${activityId}`);
}

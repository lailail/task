/**
 * 活动摘要对象。
 * 对齐活动列表接口返回字段，用于首页列表和顶部主活动展示。
 */
export interface ActivitySummary {
  activityId: number;
  activityName: string;
  city: string;
  venueName: string;
  saleStatus: string;
}

/**
 * 票种对象。
 * 当前详情页只使用后端已稳定提供的字段，不虚构额外说明字段。
 */
export interface TicketItem {
  ticketId: number;
  ticketName: string;
  price: number;
  availableStock: number;
}

/**
 * 活动详情对象。
 * 当前以活动摘要加票种列表为最小可用视图，后续字段扩展再增量补。
 */
export interface ActivityDetail extends ActivitySummary {
  ticketItems: TicketItem[];
}

/**
 * 将后端销售状态转换为更易读的中文文案。
 * 当前只覆盖已知稳定状态，未知值保底返回“状态待确认”。
 */
export function formatSaleStatus(saleStatus: string): string {
  if (saleStatus === "ON_SALE") {
    return "抢票进行中";
  }

  if (saleStatus === "COMING_SOON") {
    return "即将开售";
  }

  return "状态待确认";
}

/**
 * 判断活动是否允许直接发起抢票。
 * 当前以后端稳定状态 ON_SALE 为唯一可抢状态，避免前端自行发散业务规则。
 */
export function isActivityReservable(saleStatus: string): boolean {
  return saleStatus === "ON_SALE";
}

/**
 * 统一格式化票价。
 * 当前后端 price 使用整数元展示，前端先按人民币普通金额格式输出。
 */
export function formatPrice(price: number): string {
  return `¥${price.toLocaleString("zh-CN")}`;
}

/**
 * 统一格式化时间。
 * 当前后端返回 ISO 时间字符串时，前端统一按本地可读格式展示。
 */
export function formatDateTime(value?: string): string {
  if (!value) {
    return "待确认";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "待确认";
  }

  return new Intl.DateTimeFormat("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

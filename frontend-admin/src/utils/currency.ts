/**
 * 价格格式化工具。
 * 当前后端票价使用分为单位，这里统一格式化成元，避免页面各自重复换算。
 */
export function formatPrice(priceInCent?: number): string {
  if (typeof priceInCent !== 'number') {
    return '-';
  }

  return `¥${(priceInCent / 100).toFixed(2)}`;
}

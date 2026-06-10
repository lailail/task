import { Badge } from "@/components/ui/badge";
import { formatSaleStatus } from "@/lib/formatters";

/**
 * 活动状态标签。
 * 统一收敛状态文案和颜色映射，避免列表页和详情页对同一状态给出不同表达。
 */
export function ActivityStatusBadge({ saleStatus }: { saleStatus: string }) {
  const badgeClassName =
    saleStatus === "ON_SALE"
      ? "border-emerald-500/20 bg-emerald-500/12 text-emerald-700"
      : "border-amber-500/20 bg-amber-500/12 text-amber-700";

  return (
    <Badge className={`rounded-full border px-3 py-1 text-xs font-medium ${badgeClassName}`}>
      {formatSaleStatus(saleStatus)}
    </Badge>
  );
}

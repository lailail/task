import { OrdersView } from "@/components/orders-view";
import type { SeckillResultContext } from "@/lib/seckill-result";

/**
 * 我的订单页。
 * 服务端只解析最近一次结果上下文，真实订单数据交给客户端携带登录态访问网关接口。
 */
export default async function OrdersPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const resolvedSearchParams = await searchParams;
  const recentContext: SeckillResultContext = {
    status:
      typeof resolvedSearchParams.status === "string" ? resolvedSearchParams.status : "",
    reservationId:
      typeof resolvedSearchParams.reservationId === "string"
        ? resolvedSearchParams.reservationId
        : "",
    activityId:
      typeof resolvedSearchParams.activityId === "string" ? resolvedSearchParams.activityId : "",
    ticketId:
      typeof resolvedSearchParams.ticketId === "string" ? resolvedSearchParams.ticketId : "",
    ticketName:
      typeof resolvedSearchParams.ticketName === "string"
        ? resolvedSearchParams.ticketName
        : "",
    expireAt:
      typeof resolvedSearchParams.expireAt === "string" ? resolvedSearchParams.expireAt : "",
    message:
      typeof resolvedSearchParams.message === "string" ? resolvedSearchParams.message : "",
  };

  return <OrdersView recentContext={recentContext} />;
}

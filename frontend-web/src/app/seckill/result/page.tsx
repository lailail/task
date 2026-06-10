import { SeckillResultView } from "@/components/seckill-result-view";
import type { SeckillResultContext } from "@/lib/seckill-result";

/**
 * 抢票结果页。
 * 当前阶段继续以查询参数承接预扣结果，但把展示逻辑收敛到客户端结果视图，便于后续继续接状态感知。
 */
export default async function SeckillResultPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const resolvedSearchParams = await searchParams;
  const context: SeckillResultContext = {
    status:
      typeof resolvedSearchParams.status === "string" ? resolvedSearchParams.status : "UNKNOWN",
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

  return <SeckillResultView context={context} />;
}

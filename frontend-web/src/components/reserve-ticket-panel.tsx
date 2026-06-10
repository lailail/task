"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { Button, buttonVariants } from "@/components/ui/button";
import { HOME_PATH, LOGIN_PATH } from "@/constants/auth";
import { sessionStore } from "@/lib/auth/session-store";
import { AuthExpiredError } from "@/lib/http/api-client";
import { formatPrice, isActivityReservable } from "@/lib/formatters";
import { buildOrdersEntryHref, classifyReserveSubmitError } from "@/lib/seckill-result";
import { cn } from "@/lib/utils";
import { reserveTicket } from "@/services/seckill/api";
import type { TicketItem } from "@/types/activity";

/**
 * 生成稳定请求标识。
 * 抢票请求必须显式带 requestId 和 idempotencyKey，前端统一生成避免页面层散落。
 */
function buildRequestIdentity() {
  const uuid =
    typeof crypto !== "undefined" && typeof crypto.randomUUID === "function"
      ? crypto.randomUUID()
      : `req-${Date.now()}`;

  return {
    requestId: uuid,
    idempotencyKey: uuid,
  };
}

/**
 * 活动详情页抢票面板。
 * 负责登录校验、预扣提交以及把稳定结果上下文带到结果页和订单入口。
 */
export function ReserveTicketPanel({
  activityId,
  saleStatus,
  ticketItems,
}: {
  activityId: number;
  saleStatus: string;
  ticketItems: TicketItem[];
}) {
  const router = useRouter();
  const [submittingTicketId, setSubmittingTicketId] = useState<number | null>(null);
  const canReserve = isActivityReservable(saleStatus);

  /**
   * 发起抢票预扣。
   * 第一轮仍默认一次只提交 1 张票，但会把后端稳定错误码映射成更清晰的前台结果状态。
   */
  async function handleReserve(ticketItem: TicketItem) {
    const session = sessionStore.getSession();
    if (!session) {
      router.push(`${LOGIN_PATH}?redirect=${encodeURIComponent(`/activities/${activityId}`)}`);
      return;
    }

    const identity = buildRequestIdentity();
    setSubmittingTicketId(ticketItem.ticketId);

    try {
      const result = await reserveTicket({
        ...identity,
        activityId,
        ticketId: ticketItem.ticketId,
        quantity: 1,
      });

      const resultHref = buildOrdersEntryHref({
        reservationId: result.reservationId,
        status: result.status,
        expireAt: result.expireAt ?? "",
        activityId: String(activityId),
        ticketId: String(ticketItem.ticketId),
        ticketName: ticketItem.ticketName,
      }).replace("/orders", "/seckill/result");

      router.push(resultHref);
    } catch (error) {
      if (error instanceof AuthExpiredError) {
        return;
      }

      const failureContext = classifyReserveSubmitError(error);
      const resultHref = buildOrdersEntryHref({
        ...failureContext,
        activityId: String(activityId),
        ticketId: String(ticketItem.ticketId),
        ticketName: ticketItem.ticketName,
      }).replace("/orders", "/seckill/result");

      router.push(resultHref);
    } finally {
      setSubmittingTicketId(null);
    }
  }

  return (
    <section className="grid gap-8 lg:grid-cols-[1.2fr_0.8fr]">
      <div className="space-y-5 rounded-[2rem] border border-black/8 bg-white/76 p-7 shadow-[0_24px_80px_rgba(15,23,42,0.06)]">
        <div className="space-y-2">
          <p className="text-xs font-semibold uppercase tracking-[0.28em] text-black/38">
            Ticket Options
          </p>
          <h2 className="text-2xl font-semibold text-black">票档与库存</h2>
        </div>

        <div className="space-y-4">
          {ticketItems.map((ticketItem) => (
            <article
              className="rounded-[1.6rem] border border-black/8 bg-black/[0.025] p-5"
              key={ticketItem.ticketId}
            >
              <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                <div className="space-y-2">
                  <h3 className="text-xl font-medium text-black">{ticketItem.ticketName}</h3>
                  <p className="text-sm text-black/55">库存 {ticketItem.availableStock} 张</p>
                </div>
                <div className="flex items-center gap-4">
                  <p className="text-2xl font-semibold text-black">
                    {formatPrice(ticketItem.price)}
                  </p>
                  <Button
                    className="rounded-full bg-black px-5 text-white"
                    disabled={
                      !canReserve ||
                      ticketItem.availableStock <= 0 ||
                      submittingTicketId === ticketItem.ticketId
                    }
                    onClick={() => void handleReserve(ticketItem)}
                  >
                    {submittingTicketId === ticketItem.ticketId
                      ? "提交中..."
                      : canReserve
                        ? "立即抢票"
                        : "尚未开售"}
                  </Button>
                </div>
              </div>
            </article>
          ))}
        </div>
      </div>

      <aside className="space-y-5 rounded-[2rem] border border-black/8 bg-white/76 p-7 shadow-[0_24px_80px_rgba(15,23,42,0.06)]">
        <div className="space-y-2">
          <p className="text-xs font-semibold uppercase tracking-[0.28em] text-black/38">
            Reserve Rules
          </p>
          <h2 className="text-2xl font-semibold text-black">当前规则说明</h2>
        </div>
        <ul className="space-y-4 text-sm leading-7 text-black/62">
          <li>当前第一轮前台默认每次只提交 1 张票，直接演示秒杀式预扣入口。</li>
          <li>用户身份不再由前端直传，后端只信任网关透传的认证用户。</li>
          <li>如果活动状态不是“抢票进行中”，按钮会直接收口为不可提交状态。</li>
          <li>提交后的结果页会区分库存不足、重复提交、活动不可抢和系统失败。</li>
        </ul>
        <Link className={cn(buttonVariants({ variant: "outline" }), "w-full rounded-full")} href="/orders">
          查看我的订单入口
        </Link>
        <Link className={cn(buttonVariants({ variant: "ghost" }), "w-full rounded-full")} href={HOME_PATH}>
          返回活动首页
        </Link>
      </aside>
    </section>
  );
}

"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { AppHeader } from "@/components/app-header";
import { EmptyState } from "@/components/empty-state";
import { SectionShell } from "@/components/section-shell";
import { Badge } from "@/components/ui/badge";
import { Button, buttonVariants } from "@/components/ui/button";
import { queryMyOrders } from "@/services/orders/api";
import { formatDateTime } from "@/lib/formatters";
import {
  buildResultHref,
  hasRecentReservationContext,
  type SeckillResultContext,
} from "@/lib/seckill-result";
import { cn } from "@/lib/utils";
import type { UserOrderPage } from "@/types/order";

/**
 * 我的订单客户端视图。
 * 负责调用真实用户订单分页接口，用户身份只来自网关认证链路携带的 token。
 */
export function OrdersView({
  recentContext,
}: {
  recentContext: SeckillResultContext;
}) {
  const [ordersPage, setOrdersPage] = useState<UserOrderPage | null>(null);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const hasRecentContext = hasRecentReservationContext(recentContext);
  const resultHref = buildResultHref(recentContext);

  useEffect(() => {
    let cancelled = false;

    async function loadOrders() {
      try {
        setLoading(true);
        setErrorMessage("");
        const nextPage = await queryMyOrders(1, 10);
        if (!cancelled) {
          setOrdersPage(nextPage);
        }
      } catch {
        if (!cancelled) {
          setErrorMessage("暂时无法加载我的订单，请确认登录状态后重试。");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void loadOrders();

    return () => {
      cancelled = true;
    };
  }, []);

  function handleRetry() {
    setLoading(true);
    setErrorMessage("");
    queryMyOrders(1, 10)
      .then((nextPage) => {
        setOrdersPage(nextPage);
      })
      .catch(() => {
        setErrorMessage("暂时无法加载我的订单，请确认登录状态后重试。");
      })
      .finally(() => {
        setLoading(false);
      });
  }

  return (
    <div className="min-h-screen bg-[linear-gradient(180deg,#f4efe4_0%,#f8f5ef_100%)]">
      <AppHeader />
      <SectionShell className="space-y-8 py-14">
        <section className="rounded-[2rem] border border-black/8 bg-white/78 p-8 shadow-[0_30px_100px_rgba(15,23,42,0.08)]">
          <p className="text-xs font-semibold uppercase tracking-[0.3em] text-black/42">
            Orders / Real User API
          </p>
          <h1 className="mt-4 text-4xl font-semibold tracking-[0.04em] text-black">
            我的订单
          </h1>
          <p className="mt-4 max-w-3xl text-sm leading-8 text-black/60 md:text-base">
            当前页面已经接入真实订单分页接口，只查询当前登录用户自己的订单。前端不会提交 userId，避免用户侧篡改查询范围。
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Button className="rounded-full bg-black text-white" onClick={handleRetry}>
              {loading ? "正在加载..." : "刷新订单"}
            </Button>
            <Link className={cn(buttonVariants({ variant: "outline" }), "rounded-full")} href="/">
              返回活动首页
            </Link>
            {hasRecentContext ? (
              <Link className={cn(buttonVariants({ variant: "outline" }), "rounded-full")} href={resultHref}>
                回到刚才的结果页
              </Link>
            ) : null}
          </div>
        </section>

        {errorMessage ? (
          <section className="rounded-[2rem] border border-amber-500/20 bg-amber-50 p-6 text-sm text-amber-900">
            {errorMessage}
          </section>
        ) : null}

        <section className="rounded-[2rem] border border-black/8 bg-white/78 p-6 shadow-[0_30px_100px_rgba(15,23,42,0.08)]">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.28em] text-black/40">
                Order List
              </p>
              <h2 className="mt-3 text-2xl font-semibold text-black">最近订单</h2>
            </div>
            <Badge className="rounded-full bg-black text-white">
              共 {ordersPage?.total ?? 0} 笔
            </Badge>
          </div>

          {loading ? (
            <div className="mt-8 grid gap-4">
              {Array.from({ length: 3 }).map((_, index) => (
                <div key={index} className="h-28 animate-pulse rounded-[1.5rem] bg-black/6" />
              ))}
            </div>
          ) : null}

          {!loading && ordersPage?.records.length === 0 ? (
            <div className="mt-8">
              <EmptyState
                title="还没有订单"
                description="完成一次抢票预扣并等待异步建单后，订单会出现在这里。"
              />
            </div>
          ) : null}

          {!loading && ordersPage?.records.length ? (
            <div className="mt-8 grid gap-4">
              {ordersPage.records.map((order) => (
                <article
                  key={order.orderId}
                  className="rounded-[1.5rem] border border-black/8 bg-[#fbf8f0] p-5"
                >
                  <div className="flex flex-wrap items-start justify-between gap-4">
                    <div>
                      <p className="break-all text-sm font-medium text-black">
                        {order.orderNo}
                      </p>
                      <p className="mt-2 break-all text-xs text-black/45">
                        reservationId: {order.reservationId}
                      </p>
                    </div>
                    <Badge className="rounded-full bg-black text-white">
                      {order.orderStatus}
                    </Badge>
                  </div>
                  <div className="mt-5 grid gap-4 text-sm md:grid-cols-4">
                    <OrderFact label="活动" value={String(order.activityId)} />
                    <OrderFact label="票档" value={String(order.ticketId)} />
                    <OrderFact label="数量" value={String(order.quantity)} />
                    <OrderFact label="金额" value={formatAmountCent(order.amountCent)} />
                    <OrderFact label="过期时间" value={formatDateTime(order.expireAt)} />
                    <OrderFact label="支付时间" value={formatDateTime(order.paidAt)} />
                    <OrderFact label="关闭时间" value={formatDateTime(order.closedAt)} />
                  </div>
                </article>
              ))}
            </div>
          ) : null}
        </section>
      </SectionShell>
    </div>
  );
}

/**
 * 订单事实字段。
 */
function OrderFact({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-xs text-black/40">{label}</p>
      <p className="mt-1 font-medium text-black">{value}</p>
    </div>
  );
}

/**
 * 将分为单位的订单金额格式化成人民币展示。
 */
function formatAmountCent(amountCent: number): string {
  return new Intl.NumberFormat("zh-CN", {
    style: "currency",
    currency: "CNY",
  }).format(amountCent / 100);
}

"use client";

import Link from "next/link";
import { startTransition, useEffect, useState } from "react";
import { AppHeader } from "@/components/app-header";
import { SectionShell } from "@/components/section-shell";
import { Badge } from "@/components/ui/badge";
import { Button, buttonVariants } from "@/components/ui/button";
import { queryReservationResult } from "@/services/orders/api";
import { formatDateTime } from "@/lib/formatters";
import {
  SECKILL_RESULT_STATUS,
  buildOrdersEntryHref,
  buildSeckillResultViewModel,
  mergeReservationResultContext,
  type SeckillResultContext,
} from "@/lib/seckill-result";
import { cn } from "@/lib/utils";

/**
 * 抢票结果页客户端视图。
 * 负责按 reservationId 查询真实结果，并在异步建单空窗内进行轻量状态确认。
 */
export function SeckillResultView({
  context,
}: {
  context: SeckillResultContext;
}) {
  const [resultContext, setResultContext] = useState(context);
  const [now, setNow] = useState(() => Date.now());
  const [refreshing, setRefreshing] = useState(false);
  const [queryError, setQueryError] = useState("");
  const viewModel = buildSeckillResultViewModel(resultContext);
  const ordersHref = buildOrdersEntryHref(resultContext);
  const activityHref = resultContext.activityId ? `/activities/${resultContext.activityId}` : "/";
  const shouldPoll =
    Boolean(resultContext.reservationId) &&
    viewModel.normalizedStatus === SECKILL_RESULT_STATUS.RESERVED;

  useEffect(() => {
    if (!resultContext.expireAt || !viewModel.shouldShowExpiry) {
      return;
    }

    const timer = window.setInterval(() => {
      setNow(Date.now());
    }, 1000);

    return () => {
      window.clearInterval(timer);
    };
  }, [resultContext.expireAt, viewModel.shouldShowExpiry]);

  useEffect(() => {
    if (!resultContext.reservationId) {
      return;
    }

    let cancelled = false;
    let pollCount = 0;

    async function fetchLatestResult() {
      try {
        setQueryError("");
        const latestResult = await queryReservationResult(resultContext.reservationId as string);
        if (!cancelled) {
          setResultContext((current) => mergeReservationResultContext(current, latestResult));
        }
      } catch {
        if (!cancelled) {
          setQueryError("暂时无法确认最新结果，请稍后重试。");
        }
      }
    }

    void fetchLatestResult();

    if (!shouldPoll) {
      return () => {
        cancelled = true;
      };
    }

    const timer = window.setInterval(() => {
      pollCount += 1;
      if (pollCount > 5) {
        window.clearInterval(timer);
        return;
      }
      void fetchLatestResult();
    }, 2000);

    return () => {
      cancelled = true;
      window.clearInterval(timer);
    };
  }, [resultContext.reservationId, shouldPoll]);

  function handleRefresh() {
    if (!resultContext.reservationId) {
      setNow(Date.now());
      return;
    }

    setRefreshing(true);
    startTransition(() => {
      queryReservationResult(resultContext.reservationId as string)
        .then((latestResult) => {
          setResultContext((current) => mergeReservationResultContext(current, latestResult));
          setQueryError("");
        })
        .catch(() => {
          setQueryError("暂时无法确认最新结果，请稍后重试。");
        })
        .finally(() => {
          setNow(Date.now());
          setRefreshing(false);
        });
    });
  }

  const remainingText = buildRemainingTimeText(resultContext.expireAt, now);
  const themeClasses = getThemeClasses(viewModel.theme);

  return (
    <div className="min-h-screen bg-[linear-gradient(180deg,#111827_0%,#1f2937_42%,#f4efe4_42%,#f4efe4_100%)]">
      <AppHeader />
      <SectionShell className="space-y-10 py-14">
        <section className="overflow-hidden rounded-[2.5rem] border border-white/10 bg-black/90 px-8 py-10 text-white shadow-[0_40px_120px_rgba(15,23,42,0.28)] md:px-12">
          <div className="flex flex-wrap items-center gap-3">
            <p className="text-xs font-semibold uppercase tracking-[0.34em] text-white/48">
              Reservation Result
            </p>
            <Badge className={cn("rounded-full px-3 py-1 text-[11px]", themeClasses.badge)}>
              {viewModel.statusLabel}
            </Badge>
          </div>
          <h1 className="mt-4 text-4xl font-semibold tracking-[0.04em] md:text-6xl">
            {viewModel.headline}
          </h1>
          <p className="mt-4 max-w-3xl text-sm leading-8 text-white/70 md:text-base">
            {viewModel.summary}
          </p>
          {queryError ? (
            <p className="mt-4 rounded-full bg-amber-400/12 px-4 py-2 text-sm text-amber-100">
              {queryError}
            </p>
          ) : null}
          <div className="mt-6 flex flex-wrap gap-3">
            <Button
              className="rounded-full bg-white/10 px-5 text-white hover:bg-white/16"
              onClick={handleRefresh}
            >
              {refreshing ? "正在重新确认..." : "重新确认当前结果"}
            </Button>
            <Link className={cn(buttonVariants({ variant: "outline" }), "rounded-full border-white/18 bg-transparent text-white hover:bg-white/10 hover:text-white")} href={ordersHref}>
              去我的订单查看
            </Link>
          </div>
        </section>

        <section className="grid gap-8 lg:grid-cols-[1fr_0.9fr]">
          <article className="rounded-[2rem] border border-black/8 bg-white/78 p-8 shadow-[0_30px_100px_rgba(15,23,42,0.08)]">
            <p className="text-xs font-semibold uppercase tracking-[0.28em] text-black/40">
              Result Summary
            </p>
            <div className="mt-6 grid gap-5 md:grid-cols-2">
              <ResultField label="结果状态" value={viewModel.normalizedStatus} />
              <ResultField label="预扣编号" value={resultContext.reservationId || "待确认"} breakAll />
              <ResultField label="订单编号" value={resultContext.orderNo || "尚未生成"} breakAll />
              <ResultField label="订单状态" value={resultContext.orderStatus || "尚未生成"} />
              <ResultField label="活动编号" value={resultContext.activityId || "待确认"} />
              <ResultField label="票档编号" value={resultContext.ticketId || "待确认"} />
              <ResultField label="票档名称" value={resultContext.ticketName || "票档待确认"} />
              {viewModel.shouldShowExpiry ? (
                <ResultField label="结果过期时间" value={formatDateTime(resultContext.expireAt)} />
              ) : null}
            </div>

            <div className={cn("mt-6 rounded-[1.5rem] border px-5 py-4 text-sm leading-7", themeClasses.panel)}>
              <p className="font-medium">{viewModel.message}</p>
              {viewModel.shouldShowExpiry ? (
                <p className="mt-2 text-xs uppercase tracking-[0.22em]">
                  剩余有效时间: {remainingText}
                </p>
              ) : null}
            </div>
          </article>

          <aside className="rounded-[2rem] border border-black/8 bg-white/78 p-8 shadow-[0_30px_100px_rgba(15,23,42,0.08)]">
            <p className="text-xs font-semibold uppercase tracking-[0.28em] text-black/40">
              Next Action
            </p>
            <h2 className="mt-4 text-2xl font-semibold text-black">{viewModel.actionTitle}</h2>
            <ul className="mt-5 space-y-4 text-sm leading-7 text-black/60">
              {viewModel.actionHints.map((hint) => (
                <li key={hint}>{hint}</li>
              ))}
            </ul>
            <div className="mt-8 flex flex-col gap-3">
              <Link className={cn(buttonVariants(), "rounded-full bg-black text-white")} href={ordersHref}>
                去我的订单查看
              </Link>
              <Link
                className={cn(buttonVariants({ variant: "outline" }), "rounded-full")}
                href={viewModel.canRetryFromActivity ? activityHref : "/"}
              >
                {viewModel.canRetryFromActivity ? "返回活动详情继续确认" : "回到活动首页重新选择"}
              </Link>
            </div>
          </aside>
        </section>
      </SectionShell>
    </div>
  );
}

/**
 * 结果事实字段。
 * 用于统一渲染结果页中的 key-value 信息。
 */
function ResultField({
  label,
  value,
  breakAll = false,
}: {
  label: string;
  value: string;
  breakAll?: boolean;
}) {
  return (
    <div>
      <p className="text-sm text-black/45">{label}</p>
      <p className={cn("mt-2 text-base font-medium text-black", breakAll && "break-all text-sm")}>
        {value}
      </p>
    </div>
  );
}

/**
 * 根据结果主题返回对应样式。
 */
function getThemeClasses(theme: "success" | "warning" | "danger" | "neutral") {
  switch (theme) {
    case "success":
      return {
        badge: "bg-emerald-500/18 text-emerald-100",
        panel: "border-emerald-500/18 bg-emerald-500/8 text-emerald-900",
      };
    case "warning":
      return {
        badge: "bg-amber-500/18 text-amber-100",
        panel: "border-amber-500/18 bg-amber-500/8 text-amber-900",
      };
    case "danger":
      return {
        badge: "bg-rose-500/18 text-rose-100",
        panel: "border-rose-500/18 bg-rose-500/8 text-rose-900",
      };
    default:
      return {
        badge: "bg-white/12 text-white",
        panel: "border-slate-400/20 bg-slate-500/8 text-slate-900",
      };
  }
}

/**
 * 构造剩余有效时间文案。
 */
function buildRemainingTimeText(expireAt?: string, now = Date.now()): string {
  if (!expireAt) {
    return "待确认";
  }

  const expireAtTimestamp = new Date(expireAt).getTime();
  if (Number.isNaN(expireAtTimestamp)) {
    return "待确认";
  }

  const remainingMilliseconds = expireAtTimestamp - now;
  if (remainingMilliseconds <= 0) {
    return "已过期";
  }

  const totalSeconds = Math.floor(remainingMilliseconds / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${minutes} 分 ${seconds.toString().padStart(2, "0")} 秒`;
}

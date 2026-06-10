import Link from "next/link";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

/**
 * 页面空态。
 * 当前用于活动为空、订单入口尚未补齐等场景，向用户明确说明现在能做什么。
 */
export function EmptyState({
  title,
  description,
  actionHref,
  actionLabel,
}: {
  title: string;
  description: string;
  actionHref?: string;
  actionLabel?: string;
}) {
  return (
    <section className="rounded-[2rem] border border-black/10 bg-white/70 p-8 shadow-[0_24px_80px_rgba(15,23,42,0.06)]">
      <p className="text-xs font-semibold uppercase tracking-[0.28em] text-black/35">
        当前为空
      </p>
      <h2 className="mt-3 text-2xl font-semibold text-black">{title}</h2>
      <p className="mt-3 max-w-2xl text-sm leading-7 text-black/60">{description}</p>
      {actionHref && actionLabel ? (
        <Link
          className={cn(buttonVariants(), "mt-6 rounded-full bg-black text-white")}
          href={actionHref}
        >
          {actionLabel}
        </Link>
      ) : null}
    </section>
  );
}

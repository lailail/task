import { Skeleton } from "@/components/ui/skeleton";

/**
 * 页面加载态。
 * 统一为关键页面提供稳定的首屏占位，避免每个页面各自写一套骨架结构。
 */
export function PageLoading({ title }: { title: string }) {
  return (
    <section className="space-y-6">
      <div className="space-y-3">
        <p className="text-xs font-semibold uppercase tracking-[0.28em] text-black/40">
          {title}
        </p>
        <Skeleton className="h-12 w-72 rounded-full bg-black/10" />
        <Skeleton className="h-5 w-full max-w-xl rounded-full bg-black/8" />
      </div>
      <div className="grid gap-4 md:grid-cols-2">
        <Skeleton className="h-44 rounded-[2rem] bg-black/8" />
        <Skeleton className="h-44 rounded-[2rem] bg-black/8" />
      </div>
    </section>
  );
}

import { Button } from "@/components/ui/button";

/**
 * 页面错误态。
 * 统一给请求失败场景提供可重试入口，避免错误只停留在一段难以操作的文案里。
 */
export function PageError({
  description,
  title,
  onRetry,
}: {
  title: string;
  description: string;
  onRetry?: () => void;
}) {
  return (
    <section className="rounded-[2rem] border border-red-500/20 bg-red-500/5 p-8">
      <p className="text-xs font-semibold uppercase tracking-[0.28em] text-red-700/70">
        请求失败
      </p>
      <h2 className="mt-3 text-2xl font-semibold text-black">{title}</h2>
      <p className="mt-3 max-w-2xl text-sm leading-7 text-black/65">{description}</p>
      {onRetry ? (
        <Button className="mt-6 rounded-full" onClick={onRetry} variant="outline">
          重新加载
        </Button>
      ) : null}
    </section>
  );
}

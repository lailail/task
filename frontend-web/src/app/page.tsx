import Link from "next/link";
import { AppHeader } from "@/components/app-header";
import { ActivityStatusBadge } from "@/components/activity-status-badge";
import { EmptyState } from "@/components/empty-state";
import { PageError } from "@/components/page-error";
import { SectionShell } from "@/components/section-shell";
import { Button, buttonVariants } from "@/components/ui/button";
import { formatSaleStatus, isActivityReservable } from "@/lib/formatters";
import { cn } from "@/lib/utils";
import { queryActivitiesOnServer } from "@/services/activities/api";

/**
 * 首页始终实时取数。
 * 当前是演示站点，不希望活动状态被构造成静态缓存页面。
 */
export const dynamic = "force-dynamic";

/**
 * 用户前台首页。
 * 首屏直接由服务端拿到活动数据，避免客户端先空白再加载。
 */
export default async function HomePage() {
  let activities = [] as Awaited<ReturnType<typeof queryActivitiesOnServer>>;
  let errorMessage: string | null = null;

  try {
    activities = await queryActivitiesOnServer();
  } catch (error) {
    errorMessage = error instanceof Error ? error.message : "活动列表加载失败";
  }

  const leadActivity = activities[0] ?? null;

  return (
    <div className="min-h-screen bg-[linear-gradient(180deg,#f8f4eb_0%,#f2ede2_48%,#f8f7f4_100%)]">
      <AppHeader />
      <SectionShell className="space-y-10 pb-20 pt-8">
        {errorMessage ? (
          <PageError description={errorMessage} title="活动列表暂时无法加载" />
        ) : null}

        {!errorMessage && !leadActivity ? (
          <EmptyState
            description="当前本地演示环境还没有可展示活动，可以先检查 ticket-service 和 gateway-service 是否已启动。"
            title="暂时没有可展示活动"
          />
        ) : null}

        {!errorMessage && leadActivity ? (
          <>
            <section className="overflow-hidden rounded-[2.8rem] border border-black/8 bg-[linear-gradient(135deg,rgba(17,24,39,0.98),rgba(52,36,27,0.84))] px-8 py-10 text-white shadow-[0_40px_120px_rgba(15,23,42,0.2)] md:px-12 md:py-14">
              <div className="grid gap-10 lg:grid-cols-[1.1fr_0.9fr] lg:items-end">
                <div className="space-y-6">
                  <p className="text-xs font-semibold uppercase tracking-[0.34em] text-white/48">
                    Live Drop / 抢票进行时
                  </p>
                  <h1 className="max-w-4xl text-4xl font-semibold leading-tight tracking-[0.04em] md:text-6xl">
                    把后端真实秒杀链路，压缩成用户一眼能看懂的抢票入口。
                  </h1>
                  <p className="max-w-2xl text-sm leading-8 text-white/70 md:text-base">
                    当前首屏不做普通商城式堆叠，而是直接把“正在演示的主活动、当前销售状态、下一步动作”
                    放在同一个视觉平面里。
                  </p>
                  <div className="flex flex-wrap gap-3">
                    <Link
                      className={cn(buttonVariants(), "rounded-full bg-white text-black hover:bg-white/90")}
                      href={`/activities/${leadActivity.activityId}`}
                    >
                      进入主活动详情
                    </Link>
                    <Link
                      className={cn(
                        buttonVariants({ variant: "outline" }),
                        "rounded-full border-white/20 bg-transparent text-white hover:bg-white/8 hover:text-white",
                      )}
                      href="/orders"
                    >
                      查看我的订单入口
                    </Link>
                  </div>
                </div>

                <div className="space-y-5 rounded-[2rem] border border-white/12 bg-white/6 p-6 backdrop-blur-sm">
                  <div className="flex items-center justify-between gap-4">
                    <p className="text-sm text-white/55">当前主活动</p>
                    <ActivityStatusBadge saleStatus={leadActivity.saleStatus} />
                  </div>
                  <h2 className="text-3xl font-semibold leading-tight">
                    {leadActivity.activityName}
                  </h2>
                  <div className="grid grid-cols-2 gap-4 text-sm text-white/72">
                    <div>
                      <p className="text-white/42">城市</p>
                      <p className="mt-1 font-medium text-white">{leadActivity.city}</p>
                    </div>
                    <div>
                      <p className="text-white/42">场馆</p>
                      <p className="mt-1 font-medium text-white">{leadActivity.venueName}</p>
                    </div>
                  </div>
                  <p className="text-sm leading-7 text-white/62">
                    当前状态：{formatSaleStatus(leadActivity.saleStatus)}。
                    {isActivityReservable(leadActivity.saleStatus)
                      ? "可以直接进入详情并发起抢票。"
                      : "当前仍在等待开售，可先查看票种信息。"}
                  </p>
                </div>
              </div>
            </section>

            <section className="space-y-5">
              <div className="flex items-end justify-between gap-4">
                <div>
                  <p className="text-xs font-semibold uppercase tracking-[0.3em] text-black/36">
                    Activities
                  </p>
                  <h2 className="mt-2 text-3xl font-semibold text-black">活动列表</h2>
                </div>
                <Button className="rounded-full" variant="outline">
                  当前共 {activities.length} 场
                </Button>
              </div>

              <div className="space-y-4">
                {activities.map((activity) => (
                  <article
                    className="grid gap-5 rounded-[2rem] border border-black/8 bg-white/76 p-6 shadow-[0_24px_80px_rgba(15,23,42,0.06)] transition hover:-translate-y-0.5 hover:shadow-[0_30px_90px_rgba(15,23,42,0.1)] md:grid-cols-[1.2fr_0.8fr] md:items-center"
                    key={activity.activityId}
                  >
                    <div className="space-y-3">
                      <ActivityStatusBadge saleStatus={activity.saleStatus} />
                      <h3 className="text-2xl font-semibold text-black">{activity.activityName}</h3>
                      <p className="text-sm leading-7 text-black/60">
                        {activity.city} / {activity.venueName}
                      </p>
                    </div>
                    <div className="flex flex-col items-start gap-3 md:items-end">
                      <p className="text-sm text-black/48">
                        当前状态：{formatSaleStatus(activity.saleStatus)}
                      </p>
                      <Link
                        className={cn(buttonVariants(), "rounded-full bg-black px-5 text-white")}
                        href={`/activities/${activity.activityId}`}
                      >
                        查看详情并进入抢票
                      </Link>
                    </div>
                  </article>
                ))}
              </div>
            </section>
          </>
        ) : null}
      </SectionShell>
    </div>
  );
}

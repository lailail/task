import { AppHeader } from "@/components/app-header";
import { ActivityStatusBadge } from "@/components/activity-status-badge";
import { EmptyState } from "@/components/empty-state";
import { PageError } from "@/components/page-error";
import { ReserveTicketPanel } from "@/components/reserve-ticket-panel";
import { SectionShell } from "@/components/section-shell";
import { queryActivityDetailOnServer } from "@/services/activities/api";

/**
 * 活动详情页始终实时取数。
 * 当前销售状态和票种库存都可能变化，因此禁用静态缓存。
 */
export const dynamic = "force-dynamic";

/**
 * 活动详情页。
 * 服务端负责首屏数据展示，客户端抢票面板负责提交动作。
 */
export default async function ActivityDetailPage({
  params,
}: {
  params: Promise<{ activityId: string }>;
}) {
  const { activityId } = await params;
  let activity: Awaited<ReturnType<typeof queryActivityDetailOnServer>> | null = null;
  let errorMessage: string | null = null;

  try {
    activity = await queryActivityDetailOnServer(activityId);
  } catch (error) {
    errorMessage = error instanceof Error ? error.message : "活动详情加载失败";
  }

  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top,#fff6df_0%,#f7f2e7_40%,#f3efe7_100%)]">
      <AppHeader />
      <SectionShell className="space-y-10">
        {errorMessage ? (
          <PageError description={errorMessage} title="活动详情暂时无法加载" />
        ) : null}

        {!errorMessage && !activity ? (
          <EmptyState
            actionHref="/"
            actionLabel="返回活动列表"
            description="当前活动可能已下线，或者活动编号不在本地演示范围内。"
            title="没有找到对应活动"
          />
        ) : null}

        {!errorMessage && activity ? (
          <>
            <section className="overflow-hidden rounded-[2.5rem] border border-black/8 bg-[linear-gradient(135deg,rgba(15,23,42,0.96),rgba(31,41,55,0.82))] px-8 py-10 text-white shadow-[0_36px_120px_rgba(15,23,42,0.18)] md:px-12 md:py-14">
              <div className="max-w-4xl space-y-6">
                <p className="text-xs font-semibold uppercase tracking-[0.34em] text-white/55">
                  活动详情 / Activity Brief
                </p>
                <ActivityStatusBadge saleStatus={activity.saleStatus} />
                <h1 className="max-w-3xl text-4xl font-semibold leading-tight tracking-[0.04em] md:text-6xl">
                  {activity.activityName}
                </h1>
                <p className="max-w-2xl text-sm leading-7 text-white/70 md:text-base">
                  当前第一轮用户前台直接围绕真实活动接口、真实票种库存和真实抢票入口展开，
                  不额外虚构商城式文案和复杂聚合字段。
                </p>
                <div className="grid gap-4 text-sm text-white/78 md:grid-cols-3">
                  <div>
                    <p className="text-white/45">城市</p>
                    <p className="mt-1 font-medium text-white">{activity.city}</p>
                  </div>
                  <div>
                    <p className="text-white/45">场馆</p>
                    <p className="mt-1 font-medium text-white">{activity.venueName}</p>
                  </div>
                  <div>
                    <p className="text-white/45">票种数量</p>
                    <p className="mt-1 font-medium text-white">{activity.ticketItems.length} 种</p>
                  </div>
                </div>
              </div>
            </section>

            <ReserveTicketPanel
              activityId={activity.activityId}
              saleStatus={activity.saleStatus}
              ticketItems={activity.ticketItems}
            />
          </>
        ) : null}
      </SectionShell>
    </div>
  );
}

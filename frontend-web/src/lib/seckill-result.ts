import { ApiBusinessError, AuthExpiredError } from "@/lib/http/api-client";
import type { ReservationResult } from "@/types/order";

/**
 * 前台抢票结果状态常量。
 * 同时覆盖提交阶段的业务错误、预扣处理中状态和订单创建后的用户侧结果状态。
 */
export const SECKILL_RESULT_STATUS = {
  RESERVED: "RESERVED",
  ORDER_CREATED: "ORDER_CREATED",
  ORDER_PAID: "ORDER_PAID",
  ORDER_COMPLETED: "ORDER_COMPLETED",
  ORDER_CLOSED: "ORDER_CLOSED",
  ORDER_CANCELLED: "ORDER_CANCELLED",
  NOT_FOUND: "NOT_FOUND",
  DUPLICATE: "DUPLICATE",
  OUT_OF_STOCK: "OUT_OF_STOCK",
  ACTIVITY_NOT_ON_SALE: "ACTIVITY_NOT_ON_SALE",
  ACTIVITY_NOT_FOUND: "ACTIVITY_NOT_FOUND",
  FAILED: "FAILED",
  UNKNOWN: "UNKNOWN",
} as const;

export type SeckillResultStatus =
  (typeof SECKILL_RESULT_STATUS)[keyof typeof SECKILL_RESULT_STATUS];

/**
 * 抢票结果页上下文。
 * 由 URL 参数、提交错误映射和后端结果查询共同复用，避免页面层散落状态判断。
 */
export interface SeckillResultContext {
  status: string;
  reservationId?: string;
  activityId?: string;
  ticketId?: string;
  ticketName?: string;
  expireAt?: string;
  message?: string;
  orderId?: string;
  orderNo?: string;
  orderStatus?: string;
}

/**
 * 抢票结果页展示模型。
 * 用于统一驱动标题、说明、强调色和后续动作提示。
 */
export interface SeckillResultViewModel {
  normalizedStatus: SeckillResultStatus;
  theme: "success" | "warning" | "danger" | "neutral";
  headline: string;
  summary: string;
  statusLabel: string;
  message: string;
  actionTitle: string;
  actionHints: string[];
  canRetryFromActivity: boolean;
  shouldShowExpiry: boolean;
}

const SECKILL_ERROR_STATUS_MAP: Record<number, SeckillResultStatus> = {
  3001: SECKILL_RESULT_STATUS.ACTIVITY_NOT_FOUND,
  3002: SECKILL_RESULT_STATUS.ACTIVITY_NOT_ON_SALE,
  3003: SECKILL_RESULT_STATUS.DUPLICATE,
  3004: SECKILL_RESULT_STATUS.OUT_OF_STOCK,
};

/**
 * 规范化结果页状态。
 * 未识别的状态统一回落到 UNKNOWN，避免页面层直接依赖后端偶发值。
 */
export function normalizeSeckillResultStatus(status?: string): SeckillResultStatus {
  const normalized = status?.trim().toUpperCase();
  const knownStatuses = new Set<string>(Object.values(SECKILL_RESULT_STATUS));

  if (!normalized || !knownStatuses.has(normalized)) {
    return SECKILL_RESULT_STATUS.UNKNOWN;
  }

  return normalized as SeckillResultStatus;
}

/**
 * 根据提交阶段错误构造结果页上下文。
 * 业务错误优先按稳定错误码映射，其余错误统一降级为 FAILED。
 */
export function classifyReserveSubmitError(error: unknown): SeckillResultContext {
  if (error instanceof AuthExpiredError) {
    throw error;
  }

  if (error instanceof ApiBusinessError) {
    return {
      status: SECKILL_ERROR_STATUS_MAP[error.code] ?? SECKILL_RESULT_STATUS.FAILED,
      message: buildBusinessErrorMessage(error.code),
    };
  }

  return {
    status: SECKILL_RESULT_STATUS.FAILED,
    message: "系统有点忙，这次抢票没有提交成功，请稍后再试。",
  };
}

/**
 * 将后端 reservationId 结果查询响应转换为结果页上下文。
 * 该转换让结果页优先展示真实订单事实，同时保留 URL 中的票档名称等前端上下文。
 */
export function mergeReservationResultContext(
  current: SeckillResultContext,
  result: ReservationResult,
): SeckillResultContext {
  return {
    ...current,
    status: result.resultStatus || current.status,
    reservationId: result.reservationId || current.reservationId,
    activityId: result.activityId ? String(result.activityId) : current.activityId,
    ticketId: result.ticketId ? String(result.ticketId) : current.ticketId,
    expireAt: result.expireAt || current.expireAt,
    orderId: result.orderId ? String(result.orderId) : current.orderId,
    orderNo: result.orderNo || current.orderNo,
    orderStatus: result.orderStatus || current.orderStatus,
  };
}

/**
 * 构造结果页展示模型。
 * 统一收敛文案、后续动作和过期时间提示，避免组件继续拼接分支逻辑。
 */
export function buildSeckillResultViewModel(
  context: SeckillResultContext,
): SeckillResultViewModel {
  const normalizedStatus = normalizeSeckillResultStatus(context.status);

  switch (normalizedStatus) {
    case SECKILL_RESULT_STATUS.RESERVED:
      return buildViewModel({
        normalizedStatus,
        theme: "success",
        headline: "预扣已受理，正在等待异步建单推进",
        summary:
          "这次点击已经进入后端抢票链路。当前展示的是预扣事实，订单创建、超时关闭和库存回补仍会继续异步推进。",
        statusLabel: "预扣处理中",
        message:
          context.message ||
          "页面会轻量查询最新结果。如果订单稍后创建成功，这里会自动切换到订单状态。",
        actionHints: [
          "保留当前 reservationId，便于继续追踪这次抢票。",
          "如果短时间仍是处理中，通常表示异步建单链路还在推进。",
          "长时间没有变化时，再结合服务日志和补偿任务排查。",
        ],
        shouldShowExpiry: true,
      });
    case SECKILL_RESULT_STATUS.ORDER_CREATED:
      return buildViewModel({
        normalizedStatus,
        theme: "success",
        headline: "订单已创建，等待后续支付或关闭",
        summary: "后端已经完成异步建单。当前订单仍处于待支付状态，后续会由支付、关闭和补偿链路继续推进。",
        statusLabel: "订单已创建",
        message: context.orderNo ? `订单号：${context.orderNo}` : "订单已创建，可以到我的订单继续查看。",
        actionHints: [
          "进入我的订单页查看订单列表中的真实记录。",
          "如果订单过期未支付，超时关闭链路会释放库存。",
          "重复刷新不会再次创建订单，后端会按幂等键防重。",
        ],
        shouldShowExpiry: true,
      });
    case SECKILL_RESULT_STATUS.ORDER_PAID:
    case SECKILL_RESULT_STATUS.ORDER_COMPLETED:
      return buildViewModel({
        normalizedStatus,
        theme: "success",
        headline:
          normalizedStatus === SECKILL_RESULT_STATUS.ORDER_COMPLETED
            ? "订单已完成"
            : "订单已支付，正在等待完成确认",
        summary: "当前结果来自真实订单状态，而不是 URL 上的临时参数。",
        statusLabel:
          normalizedStatus === SECKILL_RESULT_STATUS.ORDER_COMPLETED ? "订单已完成" : "订单已支付",
        message: context.orderNo ? `订单号：${context.orderNo}` : "可以到我的订单继续查看。",
        actionHints: ["查看我的订单确认最终状态。", "如状态长时间停留，可结合支付后置处理日志排查。"],
        shouldShowExpiry: false,
      });
    case SECKILL_RESULT_STATUS.ORDER_CLOSED:
    case SECKILL_RESULT_STATUS.ORDER_CANCELLED:
      return buildViewModel({
        normalizedStatus,
        theme: "warning",
        headline:
          normalizedStatus === SECKILL_RESULT_STATUS.ORDER_CANCELLED
            ? "订单已取消"
            : "订单已关闭",
        summary: "这次抢票已经形成订单，但订单当前不再是可继续支付或履约的状态。",
        statusLabel:
          normalizedStatus === SECKILL_RESULT_STATUS.ORDER_CANCELLED ? "订单已取消" : "订单已关闭",
        message: "如库存已经回补，可以返回活动详情重新尝试。",
        actionHints: ["进入我的订单核对订单状态。", "需要重试时先确认当前订单已结束，避免重复提交。"],
        shouldShowExpiry: false,
      });
    case SECKILL_RESULT_STATUS.NOT_FOUND:
      return buildViewModel({
        normalizedStatus,
        theme: "neutral",
        headline: "没有找到这次抢票结果",
        summary: "后端没有返回属于当前登录用户的预扣或订单事实，可能是链接过期、账号不一致或 reservationId 不存在。",
        statusLabel: "结果未找到",
        message: "请确认当前登录账号是否就是发起抢票的账号。",
        actionHints: ["回到我的订单查看当前账号已有订单。", "如果是旧链接，请从活动详情重新发起操作。"],
        canRetryFromActivity: true,
        shouldShowExpiry: false,
      });
    case SECKILL_RESULT_STATUS.DUPLICATE:
      return buildViewModel({
        normalizedStatus,
        theme: "warning",
        headline: "这张票你已经提交过一次了",
        summary: "后端已拦截重复抢票请求，避免同一用户在同一业务范围内重复成功下单。",
        statusLabel: "重复提交已被拦截",
        message: context.message || "先查看最近一次抢票结果，再决定是否需要回到活动详情页重新操作。",
        actionHints: [
          "优先查看最近一次抢票结果，不要连续重复点击。",
          "如果刚刚刷新过页面，当前提示通常说明上一笔请求已经进入处理链路。",
          "若要重新尝试，请先确认上一次结果已经结束。",
        ],
        shouldShowExpiry: false,
      });
    case SECKILL_RESULT_STATUS.OUT_OF_STOCK:
      return buildViewModel({
        normalizedStatus,
        theme: "warning",
        headline: "当前票档已经没有可抢库存了",
        summary: "库存不足时，后端会在预扣前直接拦截，避免出现超卖和误导性的成功提示。",
        statusLabel: "库存不足",
        message: context.message || "可以返回活动详情页换一个票档，或者稍后再观察库存是否发生变化。",
        actionHints: ["返回活动详情页查看其他票档。", "不建议连续高频刷新，以免只是重复看到同一库存结果。"],
        shouldShowExpiry: false,
      });
    case SECKILL_RESULT_STATUS.ACTIVITY_NOT_ON_SALE:
      return buildViewModel({
        normalizedStatus,
        theme: "warning",
        headline: "当前活动还不在可抢票状态",
        summary: "真正提交时后端会再次校验活动销售状态，避免旧页面绕过规则。",
        statusLabel: "活动当前不可抢",
        message: context.message || "请等待开售后再提交。",
        actionHints: ["回到活动详情页确认最新状态。", "尚未开售时不建议重复提交。"],
        shouldShowExpiry: false,
      });
    case SECKILL_RESULT_STATUS.ACTIVITY_NOT_FOUND:
      return buildViewModel({
        normalizedStatus,
        theme: "danger",
        headline: "活动信息已经失效或不存在",
        summary: "当前请求对应的活动或票档没有通过后端校验，因此这次抢票没有进入预扣链路。",
        statusLabel: "活动信息无效",
        message: context.message || "建议回到首页重新选择活动。",
        actionHints: ["回到首页重新浏览活动列表。", "如果是旧分享链接，建议重新进入最新详情页。"],
        canRetryFromActivity: false,
        shouldShowExpiry: false,
      });
    case SECKILL_RESULT_STATUS.FAILED:
      return buildViewModel({
        normalizedStatus,
        theme: "danger",
        headline: "这次抢票没有提交成功",
        summary: "当前提示更偏向系统侧失败或未知异常。页面会优先给出稳定反馈，而不是暴露内部错误栈。",
        statusLabel: "请求失败",
        message: context.message || "可以稍后重试，或先回到活动详情页重新发起一次新的抢票请求。",
        actionHints: ["偶发失败可以稍后再试。", "频繁失败时需要结合网关和服务日志继续联调排查。"],
        shouldShowExpiry: false,
      });
    default:
      return buildViewModel({
        normalizedStatus: SECKILL_RESULT_STATUS.UNKNOWN,
        theme: "neutral",
        headline: "当前结果还需要进一步确认",
        summary: "前台拿到了结果上下文，但状态不在当前版本稳定映射表里，因此先按待确认处理。",
        statusLabel: "状态待确认",
        message: context.message || "可以先保留 reservationId，再回到活动详情页或订单入口继续观察。",
        actionHints: ["优先保留预扣编号和活动上下文。", "如果这是新状态，需要在下一轮联调里补进正式映射。"],
        shouldShowExpiry: Boolean(context.expireAt),
      });
  }
}

/**
 * 判断是否存在最近一次抢票上下文。
 * 我的订单页用它决定是否附带最近一次结果入口。
 */
export function hasRecentReservationContext(context: SeckillResultContext): boolean {
  return Boolean(context.reservationId || context.activityId || context.ticketId || context.status);
}

/**
 * 构造带当前抢票上下文的订单入口地址。
 * 真实订单页仍可利用该上下文提供“回到结果页”的快捷入口。
 */
export function buildOrdersEntryHref(context: SeckillResultContext): string {
  const query = buildResultQuery(context);
  const serializedQuery = query.toString();
  return serializedQuery ? `/orders?${serializedQuery}` : "/orders";
}

/**
 * 构造结果页地址。
 * 订单页可用它把用户带回最近一次 reservationId 的真实结果感知页。
 */
export function buildResultHref(context: SeckillResultContext): string {
  const query = buildResultQuery(context);
  const serializedQuery = query.toString();
  return serializedQuery ? `/seckill/result?${serializedQuery}` : "/seckill/result";
}

/**
 * 构造结果页查询参数。
 * 只写入存在的字段，避免 URL 中出现大量空字符串。
 */
function buildResultQuery(context: SeckillResultContext): URLSearchParams {
  const query = new URLSearchParams();

  if (context.status) {
    query.set("status", context.status);
  }
  if (context.reservationId) {
    query.set("reservationId", context.reservationId);
  }
  if (context.activityId) {
    query.set("activityId", context.activityId);
  }
  if (context.ticketId) {
    query.set("ticketId", context.ticketId);
  }
  if (context.ticketName) {
    query.set("ticketName", context.ticketName);
  }
  if (context.expireAt) {
    query.set("expireAt", context.expireAt);
  }
  if (context.message) {
    query.set("message", context.message);
  }

  return query;
}

/**
 * 构造通用展示模型，并填充默认动作配置。
 */
function buildViewModel(
  input: Omit<SeckillResultViewModel, "actionTitle" | "canRetryFromActivity"> &
    Partial<Pick<SeckillResultViewModel, "actionTitle" | "canRetryFromActivity">>,
): SeckillResultViewModel {
  return {
    actionTitle: "下一步建议",
    canRetryFromActivity: true,
    ...input,
  };
}

/**
 * 根据稳定业务错误码返回用户可理解的提示。
 */
function buildBusinessErrorMessage(code: number): string {
  switch (code) {
    case 3001:
      return "活动信息已经失效，请回到首页重新选择活动。";
    case 3002:
      return "当前活动还不在可抢票状态，请等待开售后再试。";
    case 3003:
      return "你已经提交过这张票的抢票请求，请先查看最近一次结果。";
    case 3004:
      return "当前票档库存不足，可以换一个票档或稍后再试。";
    default:
      return "系统有点忙，这次抢票没有提交成功，请稍后再试。";
  }
}

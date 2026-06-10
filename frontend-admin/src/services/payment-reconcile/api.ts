import { apiRequest } from '@/services/http/client';

/**
 * 支付对账异常查询参数。
 * 对应后端 PaymentReconcileIssueQueryRequest 的三个过滤字段。
 */
export interface PaymentReconcileIssueQuery {
  paymentRequestId?: string;
  orderId?: number;
  issueStatus?: string;
}

/**
 * 支付对账异常详情。
 * 与后端 PaymentReconcileIssueResponse 对齐。
 */
export interface PaymentReconcileIssue {
  issueId: number;
  paymentRequestId: string;
  orderId: number;
  orderNo?: string;
  issueType: string;
  issueStatus: string;
  paymentStatus: string;
  orderStatus: string;
  latestErrorMessage?: string;
  firstDetectedAt?: string;
  lastDetectedAt?: string;
  resolvedAt?: string;
  manualAction?: string;
  manualOperator?: string;
  manualNote?: string;
  manualOperatedAt?: string;
}

/**
 * 查询支付对账异常列表。
 *
 * @param params 可选过滤条件
 * @returns 异常事实列表
 */
export function queryIssues(
  params: PaymentReconcileIssueQuery,
): Promise<PaymentReconcileIssue[]> {
  const query = new URLSearchParams();

  if (params.paymentRequestId) {
    query.set('paymentRequestId', params.paymentRequestId);
  }
  if (typeof params.orderId === 'number') {
    query.set('orderId', String(params.orderId));
  }
  if (params.issueStatus) {
    query.set('issueStatus', params.issueStatus);
  }

  const suffix = query.toString() ? `?${query.toString()}` : '';
  return apiRequest<PaymentReconcileIssue[]>(
    `/api/v1/internal/payment-reconcile/issues${suffix}`,
  );
}

/**
 * 查询异常详情。
 *
 * @param issueId 异常主键
 * @returns 异常详情
 */
export function queryIssueDetail(
  issueId: string,
): Promise<PaymentReconcileIssue> {
  return apiRequest<PaymentReconcileIssue>(
    `/api/v1/internal/payment-reconcile/issues/${issueId}`,
  );
}

/**
 * 人工治理请求体。
 * 后端要求 operator 必填，note 可选。
 */
export interface IssueActionRequest {
  operator: string;
  note?: string;
}

/**
 * 发送人工重试请求。
 *
 * @param issueId 异常主键
 * @param data 操作人和备注
 * @returns 更新后的异常详情
 */
export function retryIssue(
  issueId: string,
  data: IssueActionRequest,
): Promise<PaymentReconcileIssue> {
  return apiRequest<PaymentReconcileIssue>(
    `/api/v1/internal/payment-reconcile/issues/${issueId}/retry`,
    {
      method: 'POST',
      body: JSON.stringify(data),
    },
  );
}

/**
 * 发送人工忽略请求。
 *
 * @param issueId 异常主键
 * @param data 操作人和备注
 * @returns 更新后的异常详情
 */
export function ignoreIssue(
  issueId: string,
  data: IssueActionRequest,
): Promise<PaymentReconcileIssue> {
  return apiRequest<PaymentReconcileIssue>(
    `/api/v1/internal/payment-reconcile/issues/${issueId}/ignore`,
    {
      method: 'POST',
      body: JSON.stringify(data),
    },
  );
}

/**
 * 发送人工解决请求。
 *
 * @param issueId 异常主键
 * @param data 操作人和备注
 * @returns 更新后的异常详情
 */
export function resolveIssue(
  issueId: string,
  data: IssueActionRequest,
): Promise<PaymentReconcileIssue> {
  return apiRequest<PaymentReconcileIssue>(
    `/api/v1/internal/payment-reconcile/issues/${issueId}/resolve`,
    {
      method: 'POST',
      body: JSON.stringify(data),
    },
  );
}

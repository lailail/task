import { apiRequest } from '@/services/http/client';

/**
 * 补偿任务类型。
 * 用于在前端按任务域切换不同后端接口，同时保持统一表格结构。
 */
export type ReliableTaskType =
  | 'ORDER_CREATE'
  | 'ORDER_RESULT'
  | 'ORDER_COMPLETE'
  | 'STOCK_RELEASE'
  | 'PAYMENT_RESULT'
  | 'PAYMENT_RECONCILED';

/**
 * 补偿任务查询参数。
 * 与后端统一查询请求对齐，并补充分页参数。
 */
export interface ReliableTaskQuery {
  taskStatus?: string;
  businessKey?: string;
  eventKey?: string;
  current?: number;
  pageSize?: number;
}

/**
 * 补偿任务列表项。
 * 与后端统一补偿任务响应对象对齐。
 */
export interface ReliableTaskRecord {
  taskId: number;
  taskType: ReliableTaskType;
  eventKey?: string;
  eventType?: string;
  businessKey?: string;
  taskStatus?: string;
  retryCount?: number;
  maxRetryCount?: number;
  nextRetryAt?: string;
  lastSentAt?: string;
  lastErrorMessage?: string;
}

/**
 * 补偿任务分页响应。
 * 与后端统一分页结构对齐。
 */
export interface ReliableTaskPage {
  records: ReliableTaskRecord[];
  total: number;
  current: number;
  pageSize: number;
}

const TASK_ENDPOINT_MAP: Record<ReliableTaskType, string> = {
  ORDER_CREATE: '/api/v1/internal/order-create-tasks',
  ORDER_RESULT: '/api/v1/internal/order-result-tasks',
  ORDER_COMPLETE: '/api/v1/internal/order-complete-tasks',
  STOCK_RELEASE: '/api/v1/internal/stock-release-tasks',
  PAYMENT_RESULT: '/api/v1/internal/payment-result-tasks',
  PAYMENT_RECONCILED: '/api/v1/internal/payment-reconciled-tasks',
};

/**
 * 查询补偿任务分页列表。
 *
 * @param taskType 任务域类型
 * @param params 筛选条件与分页参数
 * @returns 对应任务域的分页结果
 */
export function queryReliableTasks(
  taskType: ReliableTaskType,
  params: ReliableTaskQuery,
): Promise<ReliableTaskPage> {
  const query = new URLSearchParams();

  if (params.taskStatus) {
    query.set('taskStatus', params.taskStatus);
  }
  if (params.businessKey) {
    query.set('businessKey', params.businessKey);
  }
  if (params.eventKey) {
    query.set('eventKey', params.eventKey);
  }
  if (typeof params.current === 'number') {
    query.set('current', String(params.current));
  }
  if (typeof params.pageSize === 'number') {
    query.set('pageSize', String(params.pageSize));
  }

  const suffix = query.toString() ? `?${query.toString()}` : '';
  return apiRequest<ReliableTaskPage>(`${TASK_ENDPOINT_MAP[taskType]}${suffix}`);
}

import PageState from '@/components/PageState';
import StatusTag from '@/components/StatusTag';
import {
  queryReliableTasks,
  type ReliableTaskQuery,
  type ReliableTaskRecord,
  type ReliableTaskType,
} from '@/services/reliable-task/api';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import { App, Button, Input, Select, Space, Tabs } from 'antd';
import dayjs from 'dayjs';
import { useMemo, useRef, useState } from 'react';
import type { ActionType, ProColumns } from '@ant-design/pro-components';

const TASK_TAB_ITEMS: { key: ReliableTaskType; label: string }[] = [
  { key: 'ORDER_CREATE', label: '下单请求补偿' },
  { key: 'ORDER_RESULT', label: '下单结果补偿' },
  { key: 'ORDER_COMPLETE', label: '订单完成补偿' },
  { key: 'STOCK_RELEASE', label: '库存释放补偿' },
  { key: 'PAYMENT_RESULT', label: '支付结果补偿' },
  { key: 'PAYMENT_RECONCILED', label: '支付收敛补偿' },
];

/**
 * 补偿任务查询页。
 * 用于按任务域查看可靠消息补偿任务的状态、重试次数和最近失败原因，体现后台治理能力。
 */
const CompensationTasksPage = () => {
  const actionRef = useRef<ActionType | undefined>(undefined);
  const { message } = App.useApp();
  const [activeTaskType, setActiveTaskType] =
    useState<ReliableTaskType>('ORDER_CREATE');
  const [filters, setFilters] = useState<ReliableTaskQuery>({});
  const [errorMessage, setErrorMessage] = useState('');

  /**
   * 生成当前任务域的名称。
   * 用于在副标题中明确当前页正在查看哪一类补偿任务。
   */
  const currentTaskLabel = useMemo(() => {
    return (
      TASK_TAB_ITEMS.find((item) => item.key === activeTaskType)?.label ??
      '补偿任务'
    );
  }, [activeTaskType]);

  const columns: ProColumns<ReliableTaskRecord>[] = [
    { title: '任务 ID', dataIndex: 'taskId', width: 120 },
    { title: '任务类型', dataIndex: 'taskType', width: 150 },
    { title: '事件键', dataIndex: 'eventKey', width: 220, ellipsis: true },
    { title: '事件类型', dataIndex: 'eventType', width: 220, ellipsis: true },
    { title: '业务键', dataIndex: 'businessKey', width: 220, ellipsis: true },
    {
      title: '任务状态',
      dataIndex: 'taskStatus',
      width: 120,
      render: (_, record) => <StatusTag value={record.taskStatus} />,
    },
    { title: '已重试次数', dataIndex: 'retryCount', width: 120 },
    { title: '最大重试次数', dataIndex: 'maxRetryCount', width: 120 },
    {
      title: '下次重试时间',
      dataIndex: 'nextRetryAt',
      width: 180,
      renderText: (value) =>
        value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
    {
      title: '最后发送时间',
      dataIndex: 'lastSentAt',
      width: 180,
      renderText: (value) =>
        value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
    {
      title: '最近失败原因',
      dataIndex: 'lastErrorMessage',
      width: 260,
      ellipsis: true,
      renderText: (value) => value || '-',
    },
  ];

  return (
    <PageContainer
      title="补偿任务"
      subTitle={`当前查看：${currentTaskLabel}。用于排查可靠消息补发是否堆积、是否重试、以及最近失败原因。`}
    >
      <Tabs
        activeKey={activeTaskType}
        items={TASK_TAB_ITEMS}
        onChange={(nextKey) => {
          setActiveTaskType(nextKey as ReliableTaskType);
          setFilters({});
          setErrorMessage('');
          actionRef.current?.reload();
        }}
      />
      <Space style={{ marginBottom: 16 }} wrap>
        <Input
          style={{ width: 220 }}
          placeholder="业务键"
          value={filters.businessKey}
          onChange={(event) => {
            setFilters((current) => ({
              ...current,
              businessKey: event.target.value || undefined,
            }));
          }}
        />
        <Input
          style={{ width: 220 }}
          placeholder="事件键"
          value={filters.eventKey}
          onChange={(event) => {
            setFilters((current) => ({
              ...current,
              eventKey: event.target.value || undefined,
            }));
          }}
        />
        <Select
          allowClear
          style={{ width: 180 }}
          placeholder="任务状态"
          value={filters.taskStatus}
          onChange={(value) => {
            setFilters((current) => ({
              ...current,
              taskStatus: value,
            }));
          }}
          options={[
            { label: '待发送', value: 'PENDING' },
            { label: '重试中', value: 'RETRYING' },
            { label: '已发送', value: 'SENT' },
            { label: '已耗尽', value: 'EXHAUSTED' },
          ]}
        />
        <Button type="primary" onClick={() => actionRef.current?.reload()}>
          查询
        </Button>
        <Button
          onClick={() => {
            setFilters({});
            actionRef.current?.reload();
          }}
        >
          重置
        </Button>
      </Space>
      <ProTable<ReliableTaskRecord>
        actionRef={actionRef}
        rowKey="taskId"
        search={false}
        columns={columns}
        pagination={{ pageSize: 10, showSizeChanger: true }}
        request={async (params) => {
          try {
            setErrorMessage('');
            const data = await queryReliableTasks(activeTaskType, {
              ...filters,
              current: params.current,
              pageSize: params.pageSize,
            });
            return {
              data: data.records,
              total: data.total,
              success: true,
            };
          } catch (error) {
            const nextMessage =
              error instanceof Error
                ? error.message
                : '补偿任务列表加载失败，请稍后重试';
            setErrorMessage(nextMessage);
            message.error(nextMessage);
            return {
              data: [],
              total: 0,
              success: true,
            };
          }
        }}
        locale={{
          emptyText: errorMessage ? (
            <PageState
              type="error"
              title="补偿任务列表加载失败"
              description={errorMessage}
              actionText="重新加载"
              onAction={() => actionRef.current?.reload()}
            />
          ) : (
            <PageState
              type="empty"
              description="当前没有匹配的补偿任务记录，请调整筛选条件后重试。"
            />
          ),
        }}
      />
    </PageContainer>
  );
};

export default CompensationTasksPage;

import { PageContainer, ProTable } from '@ant-design/pro-components';
import { history } from '@umijs/max';
import { Button, Input, Popconfirm, Select, Space, message } from 'antd';
import { useRef, useState } from 'react';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import StatusTag from '@/components/StatusTag';
import {
  ignoreIssue,
  queryIssues,
  resolveIssue,
  retryIssue,
  type PaymentReconcileIssue,
  type PaymentReconcileIssueQuery,
} from '@/services/payment-reconcile/api';
import { tokenStore } from '@/utils/token';

/**
 * 支付对账异常列表页。
 * 用于承接异常事实查询和人工治理动作，体现支付收敛与治理能力。
 */
const PaymentReconcileIssuesPage = () => {
  const actionRef = useRef<ActionType | undefined>(undefined);
  const [filters, setFilters] = useState<PaymentReconcileIssueQuery>({});

  /**
   * 构造人工操作请求体。
   * 第一轮先使用当前登录名作为操作人，备注由后续弹窗增强阶段补充。
   */
  const buildActionPayload = () => {
    return {
      operator: tokenStore.getDisplayName() || tokenStore.getUsername() || 'admin',
      note: '后台管理端手工处理',
    };
  };

  /**
   * 执行人工治理动作。
   * 所有操作完成后统一刷新表格，确保页面展示和后端状态收敛一致。
   *
   * @param action 治理接口
   * @param issueId 异常主键
   * @param successText 成功提示文案
   */
  const handleAction = async (
    action: (issueId: string, payload: ReturnType<typeof buildActionPayload>) => Promise<unknown>,
    issueId: number,
    successText: string,
  ) => {
    await action(String(issueId), buildActionPayload());
    message.success(successText);
    actionRef.current?.reload();
  };

  const columns: ProColumns<PaymentReconcileIssue>[] = [
    { title: '异常 ID', dataIndex: 'issueId', width: 120 },
    { title: '支付请求号', dataIndex: 'paymentRequestId', ellipsis: true },
    { title: '订单 ID', dataIndex: 'orderId', width: 120 },
    { title: '异常类型', dataIndex: 'issueType', width: 140 },
    {
      title: '异常状态',
      dataIndex: 'issueStatus',
      width: 120,
      render: (_, record) => <StatusTag value={record.issueStatus} />,
    },
    {
      title: '订单状态',
      dataIndex: 'orderStatus',
      width: 120,
      render: (_, record) => <StatusTag value={record.orderStatus} />,
    },
    {
      title: '最近发现时间',
      dataIndex: 'lastDetectedAt',
      width: 200,
    },
    {
      title: '操作',
      valueType: 'option',
      width: 260,
      render: (_, record) => {
        return [
          <a
            key="detail"
            onClick={() =>
              history.push(`/payment-reconcile/issues/${record.issueId}`)
            }
          >
            查看详情
          </a>,
          <Popconfirm
            key="retry"
            title="确认对该异常执行人工重试吗？"
            onConfirm={() => handleAction(retryIssue, record.issueId, '已提交人工重试')}
          >
            <Button type="link">重试</Button>
          </Popconfirm>,
          <Popconfirm
            key="ignore"
            title="确认对该异常执行人工忽略吗？"
            onConfirm={() => handleAction(ignoreIssue, record.issueId, '已提交人工忽略')}
          >
            <Button type="link">忽略</Button>
          </Popconfirm>,
          <Popconfirm
            key="resolve"
            title="确认对该异常标记为人工解决吗？"
            onConfirm={() =>
              handleAction(resolveIssue, record.issueId, '已提交人工解决')
            }
          >
            <Button type="link">解决</Button>
          </Popconfirm>,
        ];
      },
    },
  ];

  return (
    <PageContainer
      title="支付对账异常"
      subTitle="查看对账异常事实，并执行重试、忽略或人工解决操作。"
    >
      <Space style={{ marginBottom: 16 }} wrap>
        <Input
          style={{ width: 240 }}
          placeholder="支付请求号"
          value={filters.paymentRequestId}
          onChange={(event) => {
            setFilters((current) => ({
              ...current,
              paymentRequestId: event.target.value || undefined,
            }));
          }}
        />
        <Input
          style={{ width: 180 }}
          placeholder="订单 ID"
          value={filters.orderId ? String(filters.orderId) : ''}
          onChange={(event) => {
            const nextValue = event.target.value.trim();
            setFilters((current) => ({
              ...current,
              orderId: nextValue ? Number(nextValue) : undefined,
            }));
          }}
        />
        <Select
          allowClear
          style={{ width: 180 }}
          placeholder="异常状态"
          value={filters.issueStatus}
          onChange={(value) => {
            setFilters((current) => ({
              ...current,
              issueStatus: value,
            }));
          }}
          options={[
            { label: '待处理', value: 'OPEN' },
            { label: '重试中', value: 'RETRYING' },
            { label: '已解决', value: 'RESOLVED' },
            { label: '已忽略', value: 'IGNORED' },
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
      <ProTable<PaymentReconcileIssue>
        actionRef={actionRef}
        rowKey="issueId"
        search={false}
        pagination={{ pageSize: 10 }}
        columns={columns}
        request={async () => {
          const data = await queryIssues(filters);
          return {
            data,
            success: true,
          };
        }}
      />
    </PageContainer>
  );
};

export default PaymentReconcileIssuesPage;

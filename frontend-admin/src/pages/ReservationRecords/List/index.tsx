import PageState from '@/components/PageState';
import StatusTag from '@/components/StatusTag';
import {
  queryReservationRecords,
  type ReservationRecord,
  type ReservationRecordQuery,
} from '@/services/reservation-record/api';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import { App, Button, Input, Select, Space } from 'antd';
import dayjs from 'dayjs';
import { useRef, useState } from 'react';
import type { ActionType, ProColumns } from '@ant-design/pro-components';

/**
 * 预扣记录列表页。
 * 用于展示正式预扣事实，帮助后台排查库存预扣、确认和释放链路是否按预期收敛。
 */
const ReservationRecordsPage = () => {
  const actionRef = useRef<ActionType | undefined>(undefined);
  const { message } = App.useApp();
  const [filters, setFilters] = useState<ReservationRecordQuery>({});
  const [errorMessage, setErrorMessage] = useState('');

  /**
   * 把输入框内容安全转换为数字。
   * 筛选区只在输入有效数字时传给后端，避免把非法值直接带入查询参数。
   *
   * @param value 输入框原始内容
   * @returns 合法数字或空
   */
  const toNumberOrUndefined = (value: string): number | undefined => {
    const nextValue = value.trim();
    if (!nextValue) {
      return undefined;
    }
    const parsedValue = Number(nextValue);
    if (Number.isNaN(parsedValue)) {
      message.warning('请输入合法数字');
      return undefined;
    }
    return parsedValue;
  };

  const columns: ProColumns<ReservationRecord>[] = [
    { title: '预扣 ID', dataIndex: 'reservationId', width: 220, ellipsis: true },
    { title: '请求 ID', dataIndex: 'requestId', width: 220, ellipsis: true },
    { title: '用户 ID', dataIndex: 'userId', width: 120 },
    { title: '订单 ID', dataIndex: 'orderId', width: 120 },
    { title: '活动 ID', dataIndex: 'activityId', width: 120 },
    { title: '票种 ID', dataIndex: 'ticketId', width: 120 },
    { title: '数量', dataIndex: 'quantity', width: 80 },
    {
      title: '预扣状态',
      dataIndex: 'reservationStatus',
      width: 120,
      render: (_, record) => <StatusTag value={record.reservationStatus} />,
    },
    { title: '来源', dataIndex: 'source', width: 140 },
    { title: '原因', dataIndex: 'reason', width: 200, ellipsis: true },
    {
      title: '过期时间',
      dataIndex: 'expireAt',
      width: 180,
      renderText: (value) => (value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'),
    },
    {
      title: '释放时间',
      dataIndex: 'releasedAt',
      width: 180,
      renderText: (value) => (value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'),
    },
  ];

  return (
    <PageContainer
      title="预扣记录"
      subTitle="查看正式预扣事实，排查库存预扣、确认和释放链路是否已经收敛。"
    >
      <Space style={{ marginBottom: 16 }} wrap>
        <Input
          style={{ width: 220 }}
          placeholder="预扣 ID"
          value={filters.reservationId}
          onChange={(event) => {
            setFilters((current) => ({
              ...current,
              reservationId: event.target.value || undefined,
            }));
          }}
        />
        <Input
          style={{ width: 220 }}
          placeholder="请求 ID"
          value={filters.requestId}
          onChange={(event) => {
            setFilters((current) => ({
              ...current,
              requestId: event.target.value || undefined,
            }));
          }}
        />
        <Input
          style={{ width: 160 }}
          placeholder="用户 ID"
          value={filters.userId ? String(filters.userId) : ''}
          onChange={(event) => {
            setFilters((current) => ({
              ...current,
              userId: toNumberOrUndefined(event.target.value),
            }));
          }}
        />
        <Input
          style={{ width: 160 }}
          placeholder="活动 ID"
          value={filters.activityId ? String(filters.activityId) : ''}
          onChange={(event) => {
            setFilters((current) => ({
              ...current,
              activityId: toNumberOrUndefined(event.target.value),
            }));
          }}
        />
        <Input
          style={{ width: 160 }}
          placeholder="票种 ID"
          value={filters.ticketId ? String(filters.ticketId) : ''}
          onChange={(event) => {
            setFilters((current) => ({
              ...current,
              ticketId: toNumberOrUndefined(event.target.value),
            }));
          }}
        />
        <Select
          allowClear
          style={{ width: 180 }}
          placeholder="预扣状态"
          value={filters.reservationStatus}
          onChange={(value) => {
            setFilters((current) => ({
              ...current,
              reservationStatus: value,
            }));
          }}
          options={[
            { label: '已预扣', value: 'RESERVED' },
            { label: '已确认', value: 'CONFIRMED' },
            { label: '已释放', value: 'RELEASED' },
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
      <ProTable<ReservationRecord>
        actionRef={actionRef}
        rowKey="reservationId"
        search={false}
        columns={columns}
        pagination={{ pageSize: 10, showSizeChanger: true }}
        request={async (params) => {
          try {
            setErrorMessage('');
            const data = await queryReservationRecords({
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
            setErrorMessage(
              error instanceof Error
                ? error.message
                : '预扣记录列表加载失败，请稍后重试',
            );
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
              title="预扣记录列表加载失败"
              description={errorMessage}
              actionText="重新加载"
              onAction={() => actionRef.current?.reload()}
            />
          ) : (
            <PageState
              type="empty"
              description="当前没有匹配的预扣记录，请调整筛选条件后重试。"
            />
          ),
        }}
      />
    </PageContainer>
  );
};

export default ReservationRecordsPage;

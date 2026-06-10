import {
  PageContainer,
  ProDescriptions,
  ProForm,
  ProFormText,
} from '@ant-design/pro-components';
import PageState from '@/components/PageState';
import { useState } from 'react';
import PageSection from '@/components/PageSection';
import StatusTag from '@/components/StatusTag';
import {
  cancelOrder,
  queryOrderStatus,
  type OrderStatusResponse,
} from '@/services/order/api';
import { App, Button, Popconfirm } from 'antd';

/**
 * 订单状态单查页。
 * 当前页以单笔排查为主，并补上已有取消接口的真实联调入口，便于演示最小订单治理能力。
 */
const OrderStatusPage = () => {
  const { message } = App.useApp();
  const [data, setData] = useState<OrderStatusResponse>();
  const [errorMessage, setErrorMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [actionSubmitting, setActionSubmitting] = useState(false);
  const [searched, setSearched] = useState(false);

  /**
   * 重新查询当前订单状态。
   * 人工取消后统一复用这个入口刷新页面展示，避免前端手工猜测状态。
   *
   * @param orderId 订单主键
   */
  const reloadOrderStatus = async (orderId: number) => {
    const response = await queryOrderStatus(String(orderId));
    setData(response);
  };

  /**
   * 对待支付订单发起人工取消。
   * 当前页只透出现有后端能力，不自行扩展更多订单治理动作。
   *
   * @param orderId 订单主键
   */
  const handleCancelOrder = async (orderId: number) => {
    setActionSubmitting(true);
    try {
      await cancelOrder(String(orderId), {
        requestId: `admin-cancel-${orderId}-${Date.now()}`,
        reason: '后台管理端手工取消',
      });
      await reloadOrderStatus(orderId);
      message.success('订单取消成功');
    } catch (error) {
      message.error(error instanceof Error ? error.message : '订单取消失败');
    } finally {
      setActionSubmitting(false);
    }
  };

  return (
    <PageContainer
      title="订单状态查询"
      subTitle="按订单 ID 查看主状态，并对待支付订单执行最小人工取消联调。"
    >
      <PageSection title="查询条件">
        <ProForm<{ orderId: string }>
          submitter={{
            searchConfig: {
              submitText: '查询订单状态',
            },
            submitButtonProps: {
              loading: submitting,
            },
          }}
          onFinish={async (values) => {
            setSubmitting(true);
            setErrorMessage('');
            setSearched(true);

            try {
              const response = await queryOrderStatus(values.orderId);
              setData(response);
              return true;
            } catch (error) {
              setData(undefined);
              setErrorMessage(
                error instanceof Error ? error.message : '订单状态查询失败',
              );
              return false;
            } finally {
              setSubmitting(false);
            }
          }}
        >
          <ProFormText
            name="orderId"
            label="订单 ID"
            placeholder="请输入订单 ID"
            rules={[{ required: true, message: '请输入订单 ID' }]}
          />
        </ProForm>
      </PageSection>
      <div style={{ height: 16 }} />
      <PageSection title="查询结果">
        {!searched ? (
          <PageState
            type="empty"
            description="请输入订单 ID 后查询订单状态，当前页面用于后台排查单笔订单主状态。"
          />
        ) : null}
        {searched && submitting ? (
          <PageState type="loading" description="订单状态查询中" />
        ) : null}
        {searched && !submitting && errorMessage ? (
          <PageState
            type="error"
            title="订单状态查询失败"
            description={errorMessage}
          />
        ) : null}
        {searched && !submitting && !errorMessage && !data ? (
          <PageState
            type="empty"
            description="当前没有查询到订单状态结果，请确认订单 ID 是否正确。"
          />
        ) : null}
        {searched && !submitting && !errorMessage && data ? (
          <ProDescriptions<OrderStatusResponse>
            column={2}
            dataSource={data}
            columns={[
              { title: '订单 ID', dataIndex: 'orderId' },
              {
                title: '订单状态',
                dataIndex: 'orderStatus',
                render: (_, record) => <StatusTag value={record.orderStatus} />,
              },
              {
                title: '治理动作',
                valueType: 'option',
                render: (_, record) =>
                  record.orderStatus === 'CREATED' ? (
                    <Popconfirm
                      title="确认取消这笔待支付订单吗？"
                      onConfirm={() => handleCancelOrder(record.orderId)}
                    >
                      <Button type="link" loading={actionSubmitting}>
                        取消订单
                      </Button>
                    </Popconfirm>
                  ) : (
                    '当前状态无需人工取消'
                  ),
              },
            ]}
          />
        ) : null}
      </PageSection>
    </PageContainer>
  );
};

export default OrderStatusPage;

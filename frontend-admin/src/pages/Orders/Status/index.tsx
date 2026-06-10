import {
  PageContainer,
  ProDescriptions,
  ProForm,
  ProFormText,
} from '@ant-design/pro-components';
import { Alert } from 'antd';
import { useState } from 'react';
import PageSection from '@/components/PageSection';
import StatusTag from '@/components/StatusTag';
import {
  queryOrderStatus,
  type OrderStatusResponse,
} from '@/services/order/api';

/**
 * 订单状态单查页。
 * 当前后端只提供按订单号查询状态的内部接口，因此第一轮后台只实现单笔排查能力。
 */
const OrderStatusPage = () => {
  const [data, setData] = useState<OrderStatusResponse>();
  const [errorMessage, setErrorMessage] = useState('');

  return (
    <PageContainer
      title="订单状态查询"
      subTitle="按订单 ID 查看主状态，供支付对账和人工排障时快速定位。"
    >
      <PageSection title="查询条件">
        <ProForm<{ orderId: string }>
          submitter={{
            searchConfig: {
              submitText: '查询订单状态',
            },
          }}
          onFinish={async (values) => {
            setErrorMessage('');

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
        {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}
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
          ]}
        />
      </PageSection>
    </PageContainer>
  );
};

export default OrderStatusPage;

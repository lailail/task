import { PageContainer, ProDescriptions } from '@ant-design/pro-components';
import { useParams } from '@umijs/max';
import { Alert, Spin } from 'antd';
import { useEffect, useState } from 'react';
import PageSection from '@/components/PageSection';
import StatusTag from '@/components/StatusTag';
import {
  queryIssueDetail,
  type PaymentReconcileIssue,
} from '@/services/payment-reconcile/api';

/**
 * 支付对账异常详情页。
 * 用于查看异常事实、状态差异和最近一次人工处理轨迹。
 */
const PaymentReconcileIssueDetailPage = () => {
  const { issueId } = useParams<{ issueId: string }>();
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<PaymentReconcileIssue>();
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    /**
     * 加载异常详情。
     * 详情页负责展示状态差异和人工处置轨迹，因此需要完整字段集。
     */
    const loadIssueDetail = async () => {
      if (!issueId) {
        setErrorMessage('缺少异常标识');
        setLoading(false);
        return;
      }

      setLoading(true);
      setErrorMessage('');

      try {
        const response = await queryIssueDetail(issueId);
        setData(response);
      } catch (error) {
        setErrorMessage(
          error instanceof Error ? error.message : '异常详情加载失败',
        );
      } finally {
        setLoading(false);
      }
    };

    void loadIssueDetail();
  }, [issueId]);

  return (
    <PageContainer
      title="异常详情"
      subTitle="查看支付事实与订单事实差异，以及最近一次人工治理轨迹。"
    >
      <Spin spinning={loading}>
        {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}
        <PageSection title="异常事实">
          <ProDescriptions<PaymentReconcileIssue>
            column={2}
            dataSource={data}
            columns={[
              { title: '异常 ID', dataIndex: 'issueId' },
              { title: '支付请求号', dataIndex: 'paymentRequestId' },
              { title: '订单 ID', dataIndex: 'orderId' },
              { title: '订单编号', dataIndex: 'orderNo' },
              { title: '异常类型', dataIndex: 'issueType' },
              {
                title: '异常状态',
                dataIndex: 'issueStatus',
                render: (_, record) => <StatusTag value={record.issueStatus} />,
              },
              {
                title: '支付状态',
                dataIndex: 'paymentStatus',
                render: (_, record) => <StatusTag value={record.paymentStatus} />,
              },
              {
                title: '订单状态',
                dataIndex: 'orderStatus',
                render: (_, record) => <StatusTag value={record.orderStatus} />,
              },
              { title: '首次发现时间', dataIndex: 'firstDetectedAt' },
              { title: '最近发现时间', dataIndex: 'lastDetectedAt' },
              { title: '解决时间', dataIndex: 'resolvedAt' },
              { title: '最近错误信息', dataIndex: 'latestErrorMessage', span: 2 },
            ]}
          />
        </PageSection>
        <div style={{ height: 16 }} />
        <PageSection title="人工处理轨迹">
          <ProDescriptions<PaymentReconcileIssue>
            column={2}
            dataSource={data}
            columns={[
              { title: '人工动作', dataIndex: 'manualAction' },
              { title: '操作人', dataIndex: 'manualOperator' },
              { title: '处理时间', dataIndex: 'manualOperatedAt' },
              { title: '处理备注', dataIndex: 'manualNote', span: 2 },
            ]}
          />
        </PageSection>
      </Spin>
    </PageContainer>
  );
};

export default PaymentReconcileIssueDetailPage;

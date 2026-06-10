import {
  PageContainer,
  ProDescriptions,
  ProTable,
} from '@ant-design/pro-components';
import { useParams } from '@umijs/max';
import { Alert, Spin } from 'antd';
import { useEffect, useState } from 'react';
import PageSection from '@/components/PageSection';
import StatusTag from '@/components/StatusTag';
import { queryActivityDetail, type ActivityDetail } from '@/services/activity/api';
import { formatPrice } from '@/utils/currency';

/**
 * 活动详情页。
 * 用于查看单个活动的状态和票种库存，方便后续和抢票链路排查关联起来。
 */
const ActivityDetailPage = () => {
  const { activityId } = useParams<{ activityId: string }>();
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<ActivityDetail>();
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    /**
     * 加载活动详情。
     * 详情页只依赖一个真实接口，因此这里直接使用局部状态管理即可。
     */
    const loadDetail = async () => {
      if (!activityId) {
        setErrorMessage('缺少活动标识');
        setLoading(false);
        return;
      }

      setLoading(true);
      setErrorMessage('');

      try {
        const detail = await queryActivityDetail(activityId);
        setData(detail);
      } catch (error) {
        setErrorMessage(
          error instanceof Error ? error.message : '活动详情加载失败',
        );
      } finally {
        setLoading(false);
      }
    };

    void loadDetail();
  }, [activityId]);

  return (
    <PageContainer
      title="活动详情"
      subTitle="查看活动基础信息和票种库存快照，用于后台排查。"
    >
      <Spin spinning={loading}>
        {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}
        <PageSection title="活动基础信息">
          <ProDescriptions<ActivityDetail>
            dataSource={data}
            column={2}
            columns={[
              { title: '活动 ID', dataIndex: 'activityId' },
              { title: '活动名称', dataIndex: 'activityName' },
              { title: '城市', dataIndex: 'city' },
              { title: '场馆', dataIndex: 'venueName' },
              {
                title: '销售状态',
                dataIndex: 'saleStatus',
                render: (_, record) => <StatusTag value={record.saleStatus} />,
              },
            ]}
          />
        </PageSection>
        <div style={{ height: 16 }} />
        <PageSection title="票种与库存">
          <ProTable
            rowKey="ticketId"
            search={false}
            options={false}
            pagination={false}
            dataSource={data?.ticketItems || []}
            columns={[
              { title: '票种 ID', dataIndex: 'ticketId', width: 120 },
              { title: '票种名称', dataIndex: 'ticketName' },
              {
                title: '票价',
                dataIndex: 'price',
                renderText: (value) => formatPrice(value),
              },
              { title: '可售库存', dataIndex: 'availableStock' },
            ]}
          />
        </PageSection>
      </Spin>
    </PageContainer>
  );
};

export default ActivityDetailPage;

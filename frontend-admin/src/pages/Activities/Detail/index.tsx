import {
  PageContainer,
  ProDescriptions,
  ProTable,
} from '@ant-design/pro-components';
import { history, useParams } from '@umijs/max';
import { useEffect, useState } from 'react';
import PageState from '@/components/PageState';
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

  /**
   * 加载活动详情。
   * 详情页是后台排障入口，必须区分“接口失败”和“活动不存在”，避免治理动作基于错误上下文继续执行。
   */
  const loadDetail = async () => {
    if (!activityId) {
      setData(undefined);
      setErrorMessage('缺少活动标识，无法查询活动详情');
      setLoading(false);
      return;
    }

    setLoading(true);
    setErrorMessage('');

    try {
      const detail = await queryActivityDetail(activityId);
      setData(detail);
    } catch (error) {
      setData(undefined);
      setErrorMessage(
        error instanceof Error ? error.message : '活动详情加载失败，请稍后重试',
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadDetail();
  }, [activityId]);

  return (
    <PageContainer
      title="活动详情"
      subTitle="查看活动基础信息和票种库存快照，用于后台排查。"
    >
      {loading ? (
        <PageState type="loading" description="活动详情加载中" />
      ) : null}
      {!loading && errorMessage ? (
        <PageState
          type="error"
          title="活动详情加载失败"
          description={errorMessage}
          actionText="重新加载"
          onAction={() => void loadDetail()}
        />
      ) : null}
      {!loading && !errorMessage && !data ? (
        <PageState
          type="empty"
          description="没有查询到该活动详情，可能活动不存在或数据尚未准备完成。"
          actionText="返回活动列表"
          onAction={() => history.push('/activities')}
        />
      ) : null}
      {!loading && !errorMessage && data ? (
        <>
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
              dataSource={data.ticketItems}
              locale={{
                emptyText: '当前活动下暂无票种数据',
              }}
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
        </>
      ) : null}
    </PageContainer>
  );
};

export default ActivityDetailPage;

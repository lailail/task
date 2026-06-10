import { PageContainer, ProTable } from '@ant-design/pro-components';
import { history } from '@umijs/max';
import { useEffect, useState } from 'react';
import { Button } from 'antd';
import PageState from '@/components/PageState';
import type { ActivitySummary } from '@/services/activity/api';
import { queryActivities } from '@/services/activity/api';
import StatusTag from '@/components/StatusTag';

/**
 * 活动列表页。
 * 第一轮后台只展示当前后端已经提供的真实活动摘要字段，不扩展伪造聚合列。
 */
const ActivityListPage = () => {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<ActivitySummary[]>([]);
  const [errorMessage, setErrorMessage] = useState('');

  /**
   * 加载活动列表。
   * 列表页同时承担后台首页入口职责，因此要显式区分加载失败和无数据，避免误判成系统无活动。
   */
  const loadActivities = async () => {
    setLoading(true);
    setErrorMessage('');

    try {
      const response = await queryActivities();
      setData(response);
    } catch (error) {
      setData([]);
      setErrorMessage(
        error instanceof Error ? error.message : '活动列表加载失败，请稍后重试',
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadActivities();
  }, []);

  return (
    <PageContainer
      title="活动管理"
      subTitle="查看当前活动摘要信息，并进入活动详情页排查状态与票种库存。"
      extra={[
        <Button key="reload" onClick={() => void loadActivities()} loading={loading}>
          刷新列表
        </Button>,
      ]}
    >
      {errorMessage ? (
        <PageState
          type="error"
          title="活动列表加载失败"
          description={errorMessage}
          actionText="重新加载"
          onAction={() => void loadActivities()}
        />
      ) : null}
      {!errorMessage && !loading && data.length === 0 ? (
        <PageState
          type="empty"
          description="当前没有可展示的活动数据，请先准备活动和票种数据后再查看。"
          actionText="重新加载"
          onAction={() => void loadActivities()}
        />
      ) : null}
      {!errorMessage && (loading || data.length > 0) ? (
        <ProTable<ActivitySummary>
          rowKey="activityId"
          search={false}
          pagination={{ pageSize: 10 }}
          loading={loading}
          dataSource={data}
          locale={{
            emptyText: <PageState type="loading" description="活动列表加载中" />,
          }}
          columns={[
            {
              title: '活动 ID',
              dataIndex: 'activityId',
              width: 120,
            },
            {
              title: '活动名称',
              dataIndex: 'activityName',
              ellipsis: true,
            },
            {
              title: '城市',
              dataIndex: 'city',
            },
            {
              title: '场馆',
              dataIndex: 'venueName',
            },
            {
              title: '销售状态',
              dataIndex: 'saleStatus',
              render: (_, record) => <StatusTag value={record.saleStatus} />,
            },
            {
              title: '操作',
              valueType: 'option',
              render: (_, record) => {
                return [
                  <a
                    key="detail"
                    onClick={() => history.push(`/activities/${record.activityId}`)}
                  >
                    查看详情
                  </a>,
                ];
              },
            },
          ]}
        />
      ) : null}
    </PageContainer>
  );
};

export default ActivityListPage;

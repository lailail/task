import { PageContainer, ProTable } from '@ant-design/pro-components';
import { history } from '@umijs/max';
import type { ActivitySummary } from '@/services/activity/api';
import { queryActivities } from '@/services/activity/api';
import StatusTag from '@/components/StatusTag';

/**
 * 活动列表页。
 * 第一轮后台只展示当前后端已经提供的真实活动摘要字段，不扩展伪造聚合列。
 */
const ActivityListPage = () => {
  return (
    <PageContainer
      title="活动管理"
      subTitle="查看当前活动摘要信息，并进入活动详情页排查状态与票种库存。"
    >
      <ProTable<ActivitySummary>
        rowKey="activityId"
        search={false}
        pagination={{ pageSize: 10 }}
        request={async () => {
          const data = await queryActivities();
          return {
            data,
            success: true,
          };
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
    </PageContainer>
  );
};

export default ActivityListPage;

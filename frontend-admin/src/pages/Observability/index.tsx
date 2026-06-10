import { PageContainer } from '@ant-design/pro-components';
import { Card, Col, Row, Typography } from 'antd';
import { OBSERVABILITY_LINKS } from '@/constants/observability';

/**
 * 观测入口页。
 * 当前先集中展示常用本地观测地址，方便联调和排查，不在页面内重复造仪表盘。
 */
const ObservabilityPage = () => {
  return (
    <PageContainer
      title="观测入口"
      subTitle="快速进入指标采集、仪表盘和网关暴露端点。"
    >
      <Row gutter={[16, 16]}>
        {OBSERVABILITY_LINKS.map((item) => (
          <Col key={item.key} xs={24} md={12} xl={8}>
            <Card title={item.title}>
              <Typography.Paragraph type="secondary">
                {item.description}
              </Typography.Paragraph>
              <a href={item.url} rel="noreferrer" target="_blank">
                {item.url}
              </a>
            </Card>
          </Col>
        ))}
      </Row>
    </PageContainer>
  );
};

export default ObservabilityPage;

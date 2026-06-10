/**
 * 观测入口链接配置。
 * 当前先使用本地演示环境约定，后续如部署域名变化，可直接改这里而不是改页面代码。
 */
export const OBSERVABILITY_LINKS = [
  {
    key: 'prometheus',
    title: 'Prometheus',
    description: '查看采集到的指标原始数据。',
    url: 'http://localhost:9090',
  },
  {
    key: 'grafana',
    title: 'Grafana',
    description: '查看仪表盘和告警可视化结果。',
    url: 'http://localhost:3002',
  },
  {
    key: 'gateway-actuator',
    title: 'Gateway Actuator',
    description: '查看网关导出的 Prometheus 指标端点。',
    url: 'http://localhost:8080/actuator/prometheus',
  },
] as const;

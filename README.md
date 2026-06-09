# 高并发购票系统

本仓库是一个基于 `Java 17 + Spring Boot 3 + Spring Cloud Alibaba` 的学习型高并发购票系统，当前聚焦秒杀/抢票场景，不做座位编排，只处理标量库存。

## 当前完成情况

当前仓库已完成：

- Maven 多模块父工程
- `gateway-service`
- `user-service`
- `ticket-service`
- `seckill-service`
- `order-service`
- `job-service`
- 用户注册与登录基础接口
- 活动列表与详情查询
- `Phase 3` 抢票预扣闭环
- `Phase 4` 异步下单与预扣确认闭环
- `Phase 5` 失败释放链路
- `Phase 5` 超时关单与库存回补链路
- `Phase 5` 预扣回查入口
- `Phase 5` 最小补偿重试补发能力
- `Phase 5` 库存释放消息级补偿重试能力
- `Phase 5` 下单结果消息级补偿重试能力
- `Phase 5` 抢票侧下单请求消息级补偿重试能力
- `Phase 5` 可靠消息公共模板抽象收敛
- `Phase 6` Micrometer 指标暴露
- `Phase 6` Prometheus + Grafana 本地观测编排
- `Phase 6` 本地 baseline 压测脚本与首轮基线报告
- JWT `accessToken + refreshToken` 正式登录链路
- `gateway-service` 统一 JWT 验签与认证身份透传
- `payment-service` 支付结果可靠消息补偿、支付对账异常内部查询
- `ticket.payment.reconciled -> PAID/COMPLETED -> ticket.order.completed` 一致性终态链路

当前已具备的关键能力：

- 秒杀抢票入口
- Redis Lua 原子预扣库存
- 预扣记录正式落库
- 异步建单
- 抢票侧下单请求补偿任务登记与失败补发
- 建单成功后预扣确认
- 建单失败后库存释放
- 超时订单关闭
- `CONFIRMED -> RELEASED` 的库存回补
- 超时预扣记录回查与补发释放事件
- 预扣确认缺失或释放事件丢失场景下的最小补偿重试
- 释放事件发送失败后的补偿任务登记、定时补发与发送状态收敛
- 下单结果事件发送失败后的补偿任务登记、定时补发与发送状态收敛
- 在保留分表前提下统一可靠消息发布模板、补发模板和任务状态常量
- 支付对账异常沉淀与内部查询
- 支付收敛事件发送失败后的补偿任务登记、定时补发与发送状态收敛
- 订单完成事件发送失败后的补偿任务登记、定时补发与发送状态收敛
- 订单 `PAID -> COMPLETED` 一致性终态收口

## 本地基础设施

仓库已提供本地 `Docker Compose` 编排，当前包含：

- `MySQL 8`
- `Redis 7`
- `RabbitMQ`
- `Nacos`（可选治理组件）
- `Prometheus`（可选观测组件）
- `Grafana`（可选观测组件）

相关文件：

- `docker-compose.yml`
- `.env.example`
- `docker/mysql/init/010_schema.sql`
- `docker/mysql/init/020_seed_data.sql`
- `docker/mysql/init/030_stock_release_task.sql`
- `docker/mysql/init/040_order_result_task.sql`
- `docker/mysql/init/050_order_create_task.sql`
- `docker/mysql/init/060_payment_record.sql`
- `docker/mysql/init/070_payment_result_task.sql`
- `docker/mysql/init/080_payment_reconcile_issue.sql`
- `docker/mysql/init/090_payment_reconciled_task.sql`
- `docker/mysql/init/100_order_complete_task.sql`

## 快速启动

1. 复制环境变量模板：

```bash
cp .env.example .env
```

2. 启动基础设施：

```bash
docker compose up -d
```

3. 如果需要一起启动治理组件：

```bash
docker compose --profile governance up -d
```

4. 如果需要一起启动观测组件：

```bash
docker compose --profile observability up -d
```

5. 如果需要让服务真正接入 `Nacos`，还要额外打开下面两个开关：

```bash
TICKET_NACOS_DISCOVERY_ENABLED=true
TICKET_NACOS_CONFIG_ENABLED=true
```

默认地址约定：

```bash
TICKET_NACOS_SERVER_ADDR=127.0.0.1:8848
```

5. 检查编排配置：

```bash
docker compose config
```

## 当前阶段说明

- 当前默认消息队列基线仍是 `RabbitMQ`
- 当前已经完成“失败建单释放”和“超时关单释放”两条最小补偿链路
- 当前 `seckill-service` 已在 Redis 预扣成功后把 `stock_reservation_record` 正式落库，再发送下单事件，`job-service` 可继续把记录推进到 `CONFIRMED`
- 当前库存释放事件和下单结果事件都已具备消息级补偿重试，并已统一为代码层可靠消息模板
- 当前 `seckill-service` 侧的下单请求事件也已接入消息级补偿重试，并统一为代码层可靠消息模板
- 当前 `user-service` 已改为签发真实 JWT，登录响应同时返回 `accessToken`、`refreshToken`、过期时间与 `tokenType`
- 当前 `gateway-service` 已承担统一认证入口职责，支持匿名白名单、JWT 验签、清洗伪造身份头并向下游透传认证身份
- 当前 `seckill-service` 已改为只信任网关透传身份，抢票请求体不再接受前端直传 `userId`
- 当前仍保留 `stock_release_task` 与 `order_result_task` 两张物理任务表，暂未合并为统一消息表或事务外盒
- 当前新增 `payment_result_task`、`payment_reconciled_task`、`order_complete_task` 三张任务表，继续沿用“分事件物理分表、代码模板统一”的策略
- 当前已补齐 `Nacos` 的本地容器、地址变量、服务侧配置入口以及 `Nacos Discovery/Config` Starter
- 当前默认仍通过 `TICKET_NACOS_DISCOVERY_ENABLED=false`、`TICKET_NACOS_CONFIG_ENABLED=false` 关闭治理能力，避免影响现有主链路
- 当前已验证 `gateway-service`、`user-service`、`ticket-service` 可注册到 `Nacos`
- 当前已验证 `gateway-service` 可通过 `lb://ticket-service` 路由转发 `/api/v1/activities`
- 当前已验证 `gateway-service`、`user-service`、`ticket-service`、`seckill-service`、`order-service`、`job-service` 均暴露 `/actuator/prometheus`
- 当前已验证 `Prometheus` 可抓取六个核心服务指标，`Grafana` 可加载默认总览看板
- `Elasticsearch` 暂未进入 v1 主链路，不在当前最小部署范围内

补充说明：

- 当前所有服务统一通过 `optional:nacos:${spring.application.name}.${spring.cloud.nacos.config.file-extension}` 导入配置中心，显式打开 `Nacos Config` 时不会再因空 `dataId` 启动失败。
- 当前 `order-service`、`job-service` 所依赖的 RabbitMQ 队列和交换机已由应用启动时自动声明，不再依赖手工预建。
- 如果本地 MySQL 容器是在新增 `030_stock_release_task.sql`、`040_order_result_task.sql` 之前初始化的，需要手工补执行这两个 SQL 文件或重建数据卷，否则补偿调度器会因为任务表缺失持续报错。
- 首轮 `Phase 6` baseline 已输出到 `docs/05-运行报告/阶段6-基线输出.json` 与 `docs/05-运行报告/2026-06-05-阶段6-基线报告.md`。

## 常用验证命令

```bash
mvn -q -DskipTests compile
mvn -q test
mvn -q verify
docker compose config
```

## 关键文档

- `docs/文档导航.md`
- `AGENTS.md`
- `docs/07-路线与现状/总体计划.md`
- `docs/01-协作规范/开发规范.md`
- `docs/02-总体设计/系统架构.md`
- `docs/01-协作规范/工程约束.md`
- `docs/07-路线与现状/已知问题.md`
- `docs/02-总体设计/可观测性方案.md`
- `docs/03-领域设计/JWT认证设计.md`
- `docs/07-路线与现状/第一版后续路线图.md`

## 2026-06-05 增量说明

- 已新增 `payment-service` 模块。
- 已补齐模拟支付结果落库、支付结果事件发布、订单主动取消和最小对账回查。
- 当前支付域走最小闭环：
  - `payment-service` 记录支付事实
  - 可靠发送 `ticket.payment.result`
  - `order-service` 收敛到 `PAID` / `CANCELLED`
  - 未收敛记录由对账任务重发支付结果事件并沉淀 `payment_reconcile_issue`
  - 收敛成功后可靠发送 `ticket.payment.reconciled`
  - `order-service` 推进到 `COMPLETED` 并可靠发送 `ticket.order.completed`

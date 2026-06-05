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

## 本地基础设施

仓库已提供本地 `Docker Compose` 编排，当前包含：

- `MySQL 8`
- `Redis 7`
- `RabbitMQ`
- `Nacos`（可选治理组件）

相关文件：

- `docker-compose.yml`
- `.env.example`
- `docker/mysql/init/010_schema.sql`
- `docker/mysql/init/020_seed_data.sql`
- `docker/mysql/init/030_stock_release_task.sql`
- `docker/mysql/init/040_order_result_task.sql`
- `docker/mysql/init/050_order_create_task.sql`

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
- 当前仍保留 `stock_release_task` 与 `order_result_task` 两张物理任务表，暂未合并为统一消息表或事务外盒
- 当前已补齐 `Nacos` 的本地容器、地址变量、服务侧配置入口以及 `Nacos Discovery/Config` Starter
- 当前默认仍通过 `TICKET_NACOS_DISCOVERY_ENABLED=false`、`TICKET_NACOS_CONFIG_ENABLED=false` 关闭治理能力，避免影响现有主链路
- 当前已验证 `gateway-service`、`user-service`、`ticket-service` 可注册到 `Nacos`
- 当前已验证 `gateway-service` 可通过 `lb://ticket-service` 路由转发 `/api/v1/activities`
- `Elasticsearch` 暂未进入 v1 主链路，不在当前最小部署范围内

补充说明：

- 当前所有服务统一通过 `optional:nacos:${spring.application.name}.${spring.cloud.nacos.config.file-extension}` 导入配置中心，显式打开 `Nacos Config` 时不会再因空 `dataId` 启动失败。
- 当前 `order-service`、`job-service` 所依赖的 RabbitMQ 队列和交换机已由应用启动时自动声明，不再依赖手工预建。
- 如果本地 MySQL 容器是在新增 `030_stock_release_task.sql`、`040_order_result_task.sql` 之前初始化的，需要手工补执行这两个 SQL 文件或重建数据卷，否则补偿调度器会因为任务表缺失持续报错。

## 常用验证命令

```bash
mvn -q -DskipTests compile
mvn -q test
mvn -q verify
docker compose config
```

## 关键文档

- `AGENTS.md`
- `docs/spec/plan.md`
- `docs/spec/rules.md`
- `docs/spec/architecture.md`
- `docs/spec/constraints.md`
- `docs/spec/known-issues.md`

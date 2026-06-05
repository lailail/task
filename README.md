# 高并发购票系统

本仓库是一个基于 `Java 17 + Spring Boot 3 + Spring Cloud Alibaba` 的学习型高并发购票系统，当前重点用于演示秒杀/抢票场景下的主链路设计。

当前已完成：

- Maven 多模块父工程
- `gateway-service`
- `user-service`
- `ticket-service`
- `seckill-service`
- `order-service`
- `job-service`
- 用户注册与登录基础接口
- 活动列表与详情查询接口
- `Phase 3` 抢票预扣闭环

## 本地基础设施

当前仓库已补齐最小本地基础设施编排：

- `MySQL 8`
- `Redis 7`
- `RabbitMQ`
- `Nacos`（可选治理组件）

默认端口说明：

- `MySQL`：本地映射默认使用 `3307`，避免和你机器上已有的 `3306` 冲突
- `Redis`：`6379`
- `RabbitMQ AMQP`：`5672`
- `RabbitMQ 管理台`：`15672`

相关文件：

- `docker-compose.yml`
- `.env.example`
- `docker/mysql/init/010_schema.sql`
- `docker/mysql/init/020_seed_data.sql`

## 快速启动

1. 复制一份环境变量模板：

```bash
cp .env.example .env
```

2. 启动基础设施：

```bash
docker compose up -d
```

如果你当前要一起启动注册/配置中心，再执行：

```bash
docker compose --profile governance up -d
```

3. 检查配置：

```bash
docker compose config
```

## 当前说明

- 当前业务代码仍有部分仓储是内存过渡实现，数据库表和种子数据已经先补齐，用于后续 `Phase 4/5` 切换正式持久化。
- 当前默认消息队列基线仍是 `RabbitMQ`，不是 `RocketMQ`。
- `Elasticsearch` 暂未进入 v1 主链路，不在当前最小 `Docker Compose` 范围内。

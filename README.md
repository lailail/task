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
- `frontend-admin` 后台管理端第一轮骨架与核心治理页面
- `frontend-admin` 后台管理端关键页面真实联调收口与统一状态反馈
- `job-service` 预扣记录内部分页查询接口
- `frontend-admin` 预扣记录真实查询页第一轮落地
- 六类补偿任务内部分页查询接口
- `frontend-admin` 补偿任务查询页第一轮落地
- `frontend-web` 用户前台第一轮骨架与主链路页面

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
- 后台管理端登录、活动查询、订单状态单查、支付对账异常治理与观测入口
- 后台管理端预扣记录分页查询与状态筛选
- 后台管理端六类补偿任务分页查询与任务域切换
- 后台管理端认证失效统一清会话并跳回登录页
- 后台管理端活动、订单、支付对账关键页面统一加载态、空态、错误态
- 用户前台活动首页、活动详情、登录、注册、抢票结果、订单入口第一轮页面
- 用户前台统一认证会话、令牌刷新与网关代理接入
- 用户前台抢票结果第二轮细化：稳定错误态映射、结果说明强化、订单入口上下文承接
- `gateway-service` 本地直连联调模式，可在关闭 `Nacos Discovery` 时继续承接前台真实联调

## 前端管理端

当前仓库已新增 `frontend-admin`，用于承接第一版后台管理端演示。

当前已落地页面：

- 登录页
- 活动列表页
- 活动详情页
- 订单状态单查页
- 订单状态页最小人工取消入口
- 预扣记录页
- 补偿任务页
- 支付对账异常列表页
- 支付对账异常详情页
- 支付对账异常人工治理入口
- 观测入口页

当前已补齐的联调细节：

- `refreshToken` 失效后统一清理登录态并跳回登录页
- 活动列表页支持刷新、加载中、无数据、请求失败重试
- 活动详情页支持加载中、详情缺失、请求失败重试
- 订单状态单查页支持首屏引导、查询中、无结果、查询失败
- 订单状态页已接入待支付订单的人工取消动作
- 预扣记录页支持分页、状态筛选、空态与失败重试
- 补偿任务页支持六个任务域 Tab、统一分页、状态筛选、事件键/业务键筛选、空态与失败重试
- 支付对账异常列表页支持空态、请求失败重试、人工治理失败反馈
- 支付对账异常详情页支持加载中、详情缺失、请求失败重试

启动方式：

```bash
cd frontend-admin
npm install
npm test
npm run build
npm start
```

补充说明：

- 当前 `frontend-admin` 基于 `Ant Design Pro v6 + Umi Max 4`
- 当前开发代理已配置为 `/api -> http://localhost:8080`
- 当前 `gateway-service` 已覆盖 `user-service`、`ticket-service`、`seckill-service`、`order-service`、`payment-service`、`job-service` 的第一版 HTTP 路由
- 当前官方推荐 `Node >= 22`；本机已升级并验证 `Node 22.22.3`、`npm 11.7.0`
- 当前开发服务在本机实际启动于 `http://localhost:8001`，说明 `Umi` 会在默认端口被占用时自动选择下一个可用端口

后台联调最小服务集合与顺序：

1. 启动 `user-service`
2. 启动 `ticket-service`
3. 启动 `order-service`
4. 启动 `payment-service`
5. 启动 `gateway-service`
6. 启动 `frontend-admin`

当前可用于后台联调的关键接口入口：

- `GET http://localhost:8080/actuator/health`
- `GET http://localhost:8080/api/v1/activities`
- `GET http://localhost:8080/api/v1/internal/orders/1/status`
- `GET http://localhost:8080/api/v1/internal/reservation-records`
- `GET http://localhost:8080/api/v1/internal/order-create-tasks`
- `GET http://localhost:8080/api/v1/internal/order-result-tasks`
- `GET http://localhost:8080/api/v1/internal/order-complete-tasks`
- `GET http://localhost:8080/api/v1/internal/stock-release-tasks`
- `GET http://localhost:8080/api/v1/internal/payment-result-tasks`
- `GET http://localhost:8080/api/v1/internal/payment-reconciled-tasks`
- `GET http://localhost:8080/api/v1/internal/payment-reconcile/issues`

当前已验证的前端真实页面：

- 登录成功后可跳转到活动管理页
- 活动管理页可通过真实后端加载活动列表
- 补偿任务页可通过真实后端切换并加载六类补偿任务
- 浏览器控制台中的 `React Intl` 菜单缺失告警已清理
- 浏览器控制台中的 `antd message` 静态上下文告警已清理

当前前端阶段结论：

- `frontend-admin` 第一轮核心治理页已基本成型
- 后台真实联调已覆盖活动、订单状态、预扣记录、补偿任务、支付对账异常
- `frontend-web` 第一轮用户主链路已完成真实联调收口

下一步前端计划：

- 细化用户前台错误态、空态与抢票反馈
- 补充结果页与后续订单状态感知
- 等后端具备完整用户订单列表接口后，再升级“我的订单”页

## 用户前台

当前仓库已新增 `frontend-web`，用于承接第一版用户主链路演示。

当前已落地页面：

- 活动首页
- 活动详情页
- 登录页
- 注册页
- 抢票结果页
- 我的订单第一轮占位页

当前已落地的公共能力：

- 统一请求层
- `accessToken + refreshToken` 本地会话存储
- `401` 自动刷新令牌
- 刷新失败统一清理登录态并跳回登录页
- 服务端首屏取数
- 页面加载态、空态、错误态组件
- 网关本地直连联调模式

启动方式：

```bash
cd frontend-web
npm install
npm test
npm run lint
npm run build
npm run dev -- --port 3001
```

补充说明：

- 当前 `frontend-web` 基于 `Next.js 16 + TypeScript + Tailwind CSS v4 + shadcn/ui`
- 当前已通过 `npm test`、`npm run lint`、`npm run build`
- 当前默认运行地址为 `http://localhost:3001`
- 当前 `/api/**` 与 `/actuator/**` 已代理到 `gateway-service`
- 当前 `gateway-service` 已支持通过 `TICKET_GATEWAY_*_SERVICE_URI` 显式指定本地直连地址；关闭 `TICKET_NACOS_DISCOVERY_ENABLED` 后，仍可用 `http://127.0.0.1:8081/8082/8083...` 做前台联调
- 当前首页与活动详情依赖真实后端活动接口，后端未启动时会进入设计中的错误态
- 当前“我的订单”页仍是第一轮占位页，尚未接完整用户订单列表能力
- `user-service` 已显式固定 `server.servlet.encoding=UTF-8`；如果在 Windows PowerShell 里直调注册接口，仍建议同时使用 `application/json; charset=utf-8`，避免终端自身编码把中文昵称提前转成 `????`

用户前台联调最小服务集合与顺序：

1. 启动 `user-service`
2. 启动 `ticket-service`
3. 启动 `seckill-service`
4. 启动 `gateway-service`
5. 启动 `frontend-web`

如果本地这轮联调不经过 `Nacos Discovery`，可按下面方式启动网关：

```bash
TICKET_NACOS_DISCOVERY_ENABLED=false
TICKET_NACOS_CONFIG_ENABLED=false
TICKET_GATEWAY_USER_SERVICE_URI=http://127.0.0.1:8081
TICKET_GATEWAY_TICKET_SERVICE_URI=http://127.0.0.1:8082
TICKET_GATEWAY_SECKILL_SERVICE_URI=http://127.0.0.1:8083
TICKET_GATEWAY_ORDER_SERVICE_URI=http://127.0.0.1:8084
TICKET_GATEWAY_JOB_SERVICE_URI=http://127.0.0.1:8085
TICKET_GATEWAY_PAYMENT_SERVICE_URI=http://127.0.0.1:8086
```

如果要继续验证后续异步结果链路，再补启：

6. 启动 `order-service`
7. 启动 `job-service`

当前已验证的用户前台真实页面：

- 活动首页可通过真实后端加载活动列表与主活动卡片
- 活动详情页可通过真实后端加载票种与库存信息
- 未登录状态下点击“立即抢票”会跳转登录页，并带回跳参数
- 登录成功后可回到活动详情页
- 登录后提交抢票可通过真实后端进入结果页，并展示真实 `reservationId`、状态与过期时间
- 结果页已区分库存不足、重复提交、活动不可抢和系统失败等稳定反馈
- “我的订单”入口已可承接最近一次抢票上下文，但仍未接完整用户订单列表

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
- `docker/mysql/init/021_fix_demo_seed_utf8.sql`
- `docker/mysql/init/022_fix_demo_user_display_name_utf8.sql`
- `docker/mysql/init/023_fix_demo_activity_typo_utf8.sql`
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
## 2026-06-10 用户侧联调补充

本轮已补齐：

- 用户侧结果感知接口：`GET /api/v1/orders/reservations/{reservationId}`
- 我的订单真实接口：`GET /api/v1/orders`
- `frontend-web` 结果页真实状态刷新
- `frontend-web` 我的订单真实分页展示
- 用户链路压测脚本：`scripts/loadtest/run-user-flow-baseline.ps1`

验证结果：

- `mvn -q -pl order-service test` 通过。
- `cd frontend-web && npm test && npm run lint && npm run build` 通过。
- 小样本压测已输出到 `docs/05-运行报告/2026-06-10-user-flow-baseline.json`。
- 当前运行环境中 `/api/v1/orders/**` 经网关返回 `503`，需重启或重新部署 `order-service` 后复测。

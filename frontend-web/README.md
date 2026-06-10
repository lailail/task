# frontend-web

`frontend-web` 是本项目的用户前台工程，用来承接第一版“活动浏览 -> 登录/注册 -> 发起抢票 -> 感知结果”的真实演示链路。

## 当前已落地内容

- `Next.js 16 + TypeScript + App Router`
- `Tailwind CSS v4`
- `shadcn/ui`
- 统一请求层与服务端取数能力
- 本地登录态存储与 `accessToken/refreshToken` 刷新逻辑
- 已落地页面：
  - `/`
  - `/activities/[activityId]`
  - `/login`
  - `/register`
  - `/seckill/result`
  - `/orders`

## 运行方式

```bash
cd frontend-web
npm install
npm run dev -- --port 3001
```

默认访问地址：

- `http://localhost:3001`

## 构建与测试

```bash
npm test
npm run lint
npm run build
```

## 与后端联调约定

- 前端统一通过 `gateway-service` 访问后端
- 默认网关地址：`http://localhost:8080`
- `next.config.ts` 已把 `/api/**`、`/actuator/**` 代理到 `TICKET_WEB_GATEWAY_BASE_URL`
- `gateway-service` 已支持通过 `TICKET_GATEWAY_*_SERVICE_URI` 显式指定本地直连地址，便于在关闭 `Nacos Discovery` 时继续联调
- 抢票请求不再传 `userId`，身份以网关透传认证信息为准

建议的最小联调服务集合：

1. `user-service`
2. `ticket-service`
3. `seckill-service`
4. `gateway-service`
5. `frontend-web`

如果本地只想拉起用户前台最小服务集合，而不依赖 `Nacos Discovery`，可让网关改走本地直连：

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

如果要继续验证异步结果链路，再补启：

6. `order-service`
7. `job-service`

当前已完成的真实联调：

- 首页可加载真实活动列表
- 活动详情页可加载真实票种与库存
- 未登录抢票会跳转登录页并保留回跳
- 登录成功后可回到详情页继续提交
- 抢票提交后可进入真实结果页并展示预扣编号与状态
- 结果页已区分库存不足、重复提交、活动不可抢和系统失败等稳定反馈
- “我的订单”入口已能承接最近一次抢票上下文，便于继续结果感知

## 当前限制

- 首页和活动详情依赖后端真实活动接口；如果网关或票务服务未启动，会显示错误态或空态
- “我的订单”页当前仍是结果感知承接页，还没有接完整的用户订单列表能力
- 结果页当前承接的是预扣结果展示与结果说明强化，不是完整的订单追踪页
- 当前还没有面向前台的 reservation 结果查询接口，因此“重新确认当前结果”仍以结果页上下文承接为主
## 2026-06-10 用户订单联调更新

本轮 `frontend-web` 已接入：

- `GET /api/v1/orders/reservations/{reservationId}`
- `GET /api/v1/orders`

页面变化：

- `/seckill/result` 会在存在 `reservationId` 时查询真实结果，并对 `RESERVED` 做轻量轮询。
- `/orders` 会查询当前登录用户的真实订单分页，不再展示占位订单列表。

已验证：

- `npm test`
- `npm run lint`
- `npm run build`

运行环境提示：

- 如果 `/api/v1/orders/**` 经网关返回 `503`，优先确认 `order-service` 是否已经启动、注册到 Nacos，或是否已重启到包含本轮接口的最新代码。

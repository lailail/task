# 高并发购票系统

当前仓库已完成 `Phase 1` 骨架搭建，并已进入 `Phase 2` 基础能力开发。

当前已具备：

- 父工程依赖管理
- `common-core` 公共模块
- `gateway-service`
- `user-service`
- `ticket-service`
- `seckill-service`
- `order-service`
- `job-service`
- `user-service` 注册与登录接口
- `ticket-service` 活动列表与活动详情查询接口

后续将继续补齐：

- `seckill-service` 抢票入口
- Redis 库存预扣
- 异步下单
- 订单超时关闭与库存回补

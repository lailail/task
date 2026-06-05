USE ticket_system;

CREATE TABLE IF NOT EXISTS payment_record (
    payment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付主键',
    payment_request_id VARCHAR(64) NOT NULL COMMENT '支付请求标识',
    order_id BIGINT NOT NULL COMMENT '订单主键',
    order_no VARCHAR(64) NOT NULL COMMENT '订单编号',
    reservation_id VARCHAR(64) NOT NULL COMMENT '预扣标识',
    request_id VARCHAR(64) NOT NULL COMMENT '请求标识',
    user_id BIGINT NOT NULL COMMENT '用户主键',
    activity_id BIGINT NOT NULL COMMENT '活动主键',
    ticket_id BIGINT NOT NULL COMMENT '票种主键',
    quantity INT UNSIGNED NOT NULL COMMENT '购票数量',
    payment_status VARCHAR(32) NOT NULL COMMENT '支付状态',
    reconcile_status VARCHAR(32) NOT NULL COMMENT '对账状态',
    reason VARCHAR(128) NULL COMMENT '支付结果原因',
    paid_at DATETIME NULL COMMENT '支付完成时间',
    last_reconcile_at DATETIME NULL COMMENT '最近对账时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_payment_record_payment_request_id (payment_request_id),
    KEY idx_payment_record_order_id (order_id),
    KEY idx_payment_record_reconcile_status (reconcile_status)
) COMMENT='支付记录表';

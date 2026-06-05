USE ticket_system;

CREATE TABLE IF NOT EXISTS order_create_task (
    task_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '抢票侧下单请求补偿任务主键',
    event_key VARCHAR(64) NOT NULL COMMENT '下单请求事件幂等键',
    event_type VARCHAR(64) NOT NULL COMMENT '下单请求事件类型',
    business_key VARCHAR(64) NOT NULL COMMENT '业务主键，当前默认使用 reservationId',
    payload_json JSON NOT NULL COMMENT '下单请求事件载荷',
    task_status VARCHAR(32) NOT NULL COMMENT '任务状态：PENDING/RETRYING/SENT/EXHAUSTED',
    retry_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '已尝试重发次数',
    max_retry_count INT UNSIGNED NOT NULL DEFAULT 20 COMMENT '允许的最大重发次数',
    next_retry_at DATETIME NOT NULL COMMENT '下一次重发时间',
    last_sent_at DATETIME NULL COMMENT '最近一次成功发送时间',
    last_error_message VARCHAR(255) NULL COMMENT '最后一次发送错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_create_task_event_key (event_key),
    KEY idx_order_create_task_status_retry_at (task_status, next_retry_at)
) COMMENT='抢票侧下单请求补偿任务表';

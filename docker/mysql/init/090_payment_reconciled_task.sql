CREATE TABLE IF NOT EXISTS payment_reconciled_task (
    task_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付收敛补发任务主键',
    event_key VARCHAR(64) NOT NULL COMMENT '事件唯一键',
    event_type VARCHAR(64) NOT NULL COMMENT '事件类型',
    business_key VARCHAR(64) NOT NULL COMMENT '业务主键',
    payload_json TEXT NOT NULL COMMENT '事件载荷',
    task_status VARCHAR(32) NOT NULL COMMENT '任务状态',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '当前重试次数',
    max_retry_count INT NOT NULL DEFAULT 20 COMMENT '最大重试次数',
    next_retry_at DATETIME DEFAULT NULL COMMENT '下一次重试时间',
    last_sent_at DATETIME DEFAULT NULL COMMENT '最后发送时间',
    last_error_message VARCHAR(255) DEFAULT NULL COMMENT '最后一次错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_payment_reconciled_task_event_key (event_key),
    KEY idx_payment_reconciled_task_status_retry_time (task_status, next_retry_at)
) COMMENT='支付收敛事件补发任务表';

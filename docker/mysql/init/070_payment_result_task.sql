USE ticket_system;

CREATE TABLE IF NOT EXISTS payment_result_task (
    task_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付结果补偿任务主键',
    event_key VARCHAR(64) NOT NULL COMMENT '统一事件键',
    event_type VARCHAR(64) NOT NULL COMMENT '支付结果事件类型',
    business_key VARCHAR(64) NOT NULL COMMENT '业务主键，当前使用 payment_request_id',
    payload_json TEXT NOT NULL COMMENT '支付结果事件载荷 JSON',
    task_status VARCHAR(32) NOT NULL COMMENT '任务状态',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '当前重试次数',
    max_retry_count INT NOT NULL DEFAULT 20 COMMENT '最大重试次数',
    next_retry_at DATETIME NULL COMMENT '下一次补发时间',
    last_sent_at DATETIME NULL COMMENT '最后一次发送成功时间',
    last_error_message VARCHAR(512) NULL COMMENT '最后一次发送失败原因',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_payment_result_task_event_key (event_key),
    KEY idx_payment_result_task_status_retry (task_status, next_retry_at),
    KEY idx_payment_result_task_business_key (business_key)
) COMMENT='支付结果补偿任务表';

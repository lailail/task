USE ticket_system;

CREATE TABLE IF NOT EXISTS ticket_user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户主键',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '加密密码',
    display_name VARCHAR(64) NOT NULL COMMENT '展示名称',
    user_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '用户状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_ticket_user_username (username)
) COMMENT='用户表';

CREATE TABLE IF NOT EXISTS ticket_activity (
    activity_id BIGINT PRIMARY KEY COMMENT '活动主键',
    activity_name VARCHAR(128) NOT NULL COMMENT '活动名称',
    city VARCHAR(64) NOT NULL COMMENT '活动城市',
    venue_name VARCHAR(128) NOT NULL COMMENT '场馆名称',
    sale_status VARCHAR(32) NOT NULL COMMENT '销售状态',
    activity_start_time DATETIME NULL COMMENT '活动开始时间',
    activity_end_time DATETIME NULL COMMENT '活动结束时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='活动表';

CREATE TABLE IF NOT EXISTS ticket_item (
    ticket_id BIGINT PRIMARY KEY COMMENT '票种主键',
    activity_id BIGINT NOT NULL COMMENT '所属活动主键',
    ticket_name VARCHAR(64) NOT NULL COMMENT '票种名称',
    price_cent INT UNSIGNED NOT NULL COMMENT '票价，单位分',
    total_stock INT UNSIGNED NOT NULL COMMENT '总库存',
    available_stock INT UNSIGNED NOT NULL COMMENT '可售库存',
    ticket_status VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE' COMMENT '票种状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_ticket_item_activity FOREIGN KEY (activity_id) REFERENCES ticket_activity (activity_id)
) COMMENT='票种表';

CREATE TABLE IF NOT EXISTS stock_reservation_record (
    reservation_id VARCHAR(64) PRIMARY KEY COMMENT '预扣标识',
    request_id VARCHAR(64) NOT NULL COMMENT '请求标识',
    idempotency_key VARCHAR(64) NOT NULL COMMENT '幂等键',
    user_id BIGINT NOT NULL COMMENT '用户主键',
    activity_id BIGINT NOT NULL COMMENT '活动主键',
    ticket_id BIGINT NOT NULL COMMENT '票种主键',
    quantity INT UNSIGNED NOT NULL COMMENT '预扣数量',
    reservation_status VARCHAR(32) NOT NULL COMMENT '预扣状态',
    order_id BIGINT NULL COMMENT '订单主键',
    reason VARCHAR(128) NULL COMMENT '释放或失败原因',
    source VARCHAR(32) NOT NULL COMMENT '来源服务',
    expire_at DATETIME NOT NULL COMMENT '预扣过期时间',
    released_at DATETIME NULL COMMENT '释放时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_reservation_request_id (request_id),
    UNIQUE KEY uk_reservation_idempotency_key (idempotency_key),
    KEY idx_reservation_user_activity (user_id, activity_id)
) COMMENT='库存预扣记录表';

CREATE TABLE IF NOT EXISTS ticket_order (
    order_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单主键',
    order_no VARCHAR(64) NOT NULL COMMENT '订单编号',
    reservation_id VARCHAR(64) NOT NULL COMMENT '预扣标识',
    request_id VARCHAR(64) NOT NULL COMMENT '请求标识',
    idempotency_key VARCHAR(64) NOT NULL COMMENT '幂等键',
    user_id BIGINT NOT NULL COMMENT '用户主键',
    activity_id BIGINT NOT NULL COMMENT '活动主键',
    ticket_id BIGINT NOT NULL COMMENT '票种主键',
    quantity INT UNSIGNED NOT NULL COMMENT '购票数量',
    amount_cent INT UNSIGNED NOT NULL COMMENT '订单金额，单位分',
    order_status VARCHAR(32) NOT NULL COMMENT '订单状态',
    source VARCHAR(32) NOT NULL COMMENT '订单来源',
    expire_at DATETIME NOT NULL COMMENT '订单过期时间',
    paid_at DATETIME NULL COMMENT '支付时间',
    closed_at DATETIME NULL COMMENT '关闭时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_ticket_order_order_no (order_no),
    UNIQUE KEY uk_ticket_order_reservation_id (reservation_id),
    UNIQUE KEY uk_ticket_order_idempotency_key (idempotency_key),
    KEY idx_ticket_order_user_id (user_id),
    KEY idx_ticket_order_status_expire_at (order_status, expire_at)
) COMMENT='订单表';

CREATE TABLE IF NOT EXISTS order_event_log (
    event_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '事件主键',
    event_key VARCHAR(64) NOT NULL COMMENT '事件幂等键',
    event_type VARCHAR(64) NOT NULL COMMENT '事件类型',
    business_key VARCHAR(64) NOT NULL COMMENT '业务主键，例如 reservationId 或 orderId',
    payload_json JSON NOT NULL COMMENT '事件载荷',
    consume_status VARCHAR(32) NOT NULL COMMENT '消费状态',
    retry_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '重试次数',
    last_error_message VARCHAR(255) NULL COMMENT '最后一次错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_event_log_event_key (event_key),
    KEY idx_order_event_log_type_status (event_type, consume_status)
) COMMENT='订单事件日志表';

USE ticket_system;

SET @ddl = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'payment_reconcile_issue'
          AND COLUMN_NAME = 'manual_action'
    ),
    'SELECT 1',
    'ALTER TABLE payment_reconcile_issue ADD COLUMN manual_action VARCHAR(64) DEFAULT NULL COMMENT ''最近一次人工处置动作'' AFTER resolved_at'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'payment_reconcile_issue'
          AND COLUMN_NAME = 'manual_operator'
    ),
    'SELECT 1',
    'ALTER TABLE payment_reconcile_issue ADD COLUMN manual_operator VARCHAR(64) DEFAULT NULL COMMENT ''最近一次人工处置操作人'' AFTER manual_action'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'payment_reconcile_issue'
          AND COLUMN_NAME = 'manual_note'
    ),
    'SELECT 1',
    'ALTER TABLE payment_reconcile_issue ADD COLUMN manual_note VARCHAR(255) DEFAULT NULL COMMENT ''最近一次人工处置备注'' AFTER manual_operator'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'payment_reconcile_issue'
          AND COLUMN_NAME = 'manual_operated_at'
    ),
    'SELECT 1',
    'ALTER TABLE payment_reconcile_issue ADD COLUMN manual_operated_at DATETIME DEFAULT NULL COMMENT ''最近一次人工处置时间'' AFTER manual_note'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

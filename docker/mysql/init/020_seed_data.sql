USE ticket_system;

INSERT INTO ticket_activity (
    activity_id,
    activity_name,
    city,
    venue_name,
    sale_status,
    activity_start_time,
    activity_end_time
) VALUES
    (1001, '五月天上海演唱会', '上海', '上海体育场', 'ON_SALE', '2026-07-12 19:30:00', '2026-07-12 22:30:00'),
    (1002, '国家大剧院话剧专场', '北京', '国家大剧院', 'COMING_SOON', '2026-08-08 19:30:00', '2026-08-08 21:30:00')
ON DUPLICATE KEY UPDATE
    activity_name = VALUES(activity_name),
    city = VALUES(city),
    venue_name = VALUES(venue_name),
    sale_status = VALUES(sale_status),
    activity_start_time = VALUES(activity_start_time),
    activity_end_time = VALUES(activity_end_time);

INSERT INTO ticket_item (
    ticket_id,
    activity_id,
    ticket_name,
    price_cent,
    total_stock,
    available_stock,
    ticket_status
) VALUES
    (501, 1001, '看台票', 49900, 800, 800, 'AVAILABLE'),
    (502, 1001, '内场票', 129900, 200, 200, 'AVAILABLE'),
    (503, 1002, '一层座位票', 69900, 120, 120, 'AVAILABLE')
ON DUPLICATE KEY UPDATE
    activity_id = VALUES(activity_id),
    ticket_name = VALUES(ticket_name),
    price_cent = VALUES(price_cent),
    total_stock = VALUES(total_stock),
    available_stock = VALUES(available_stock),
    ticket_status = VALUES(ticket_status);

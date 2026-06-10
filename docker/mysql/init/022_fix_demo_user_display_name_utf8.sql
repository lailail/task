USE ticket_system;

UPDATE ticket_user
SET display_name = CONVERT(0xE5908EE58FB0E88194E8B083E8B4A6E58FB7 USING utf8mb4)
WHERE username = 'admin01';

UPDATE ticket_user
SET display_name = CONVERT(0xE88194E8B083E8B4A6E58FB7 USING utf8mb4)
WHERE username = 'admin111821';

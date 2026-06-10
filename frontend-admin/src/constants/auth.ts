/**
 * 后台登录态本地存储常量。
 * 统一收口 key 命名，避免页面和工具类各自硬编码浏览器存储字段。
 */
export const ACCESS_TOKEN_KEY = 'ticket_admin_access_token';
export const REFRESH_TOKEN_KEY = 'ticket_admin_refresh_token';
export const USERNAME_KEY = 'ticket_admin_username';
export const DISPLAY_NAME_KEY = 'ticket_admin_display_name';

/**
 * 后台路由常量。
 * 将登录页和首页入口抽到常量，避免跳转路径散落在多个页面。
 */
export const LOGIN_PATH = '/login';
export const HOME_PATH = '/activities';

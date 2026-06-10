/**
 * 用户前台登录态本地存储常量。
 * 统一收口 key 命名，避免请求层、页面和工具层各自硬编码。
 */
export const ACCESS_TOKEN_KEY = "ticket_web_access_token";
export const REFRESH_TOKEN_KEY = "ticket_web_refresh_token";
export const USERNAME_KEY = "ticket_web_username";
export const DISPLAY_NAME_KEY = "ticket_web_display_name";

/**
 * 用户前台路由常量。
 * 登录、注册、首页和订单页会在多个页面间复用，统一抽到常量避免漂移。
 */
export const HOME_PATH = "/";
export const LOGIN_PATH = "/login";
export const REGISTER_PATH = "/register";
export const ORDERS_PATH = "/orders";

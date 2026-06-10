/**
 * 统一接口成功码。
 * 当前后端统一使用 0 代表成功，前端请求层通过该常量解包。
 */
export const API_SUCCESS_CODE = 0;

/**
 * 刷新令牌接口路径。
 * 刷新逻辑只允许通过这里走统一入口，避免各页面分散实现。
 */
export const REFRESH_TOKEN_PATH = "/api/v1/users/token/refresh";

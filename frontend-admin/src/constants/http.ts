/**
 * 后端统一成功码。
 * 该值来自后端 common-core 的 ErrorCode.SUCCESS。
 */
export const API_SUCCESS_CODE = 0;

/**
 * 刷新令牌接口路径。
 * 请求层会用它规避自我递归刷新。
 */
export const REFRESH_TOKEN_PATH = '/api/v1/users/token/refresh';

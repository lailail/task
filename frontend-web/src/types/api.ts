/**
 * 后端统一响应包装对象。
 * 用户前台所有请求都先按该结构解包，再向上层暴露真正业务数据。
 */
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

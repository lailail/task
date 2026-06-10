/**
 * 后端统一响应包装。
 * 所有同步 HTTP 接口都按该结构解包，保证前端错误处理语义稳定。
 */
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

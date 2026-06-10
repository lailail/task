/**
 * 后台访问控制声明。
 * 第一版只按“是否已登录”控制页面可访问性，暂不引入复杂 RBAC。
 *
 * @param initialState 全局初始状态
 * @returns 页面访问能力集合
 */
export default function access(
  initialState: { isLogin?: boolean } | undefined,
): { canAdmin: boolean } {
  return {
    canAdmin: !!initialState?.isLogin,
  };
}

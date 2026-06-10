import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HOME_PATH, LOGIN_PATH } from '@/constants/auth';
import {
  apiRequest,
  ApiBusinessError,
  AuthExpiredError,
} from '@/services/http/client';
import { tokenStore } from '@/utils/token';

describe('apiRequest', () => {
  /**
   * 模拟浏览器地址对象。
   * 认证失效时请求层会直接跳回登录页，因此测试需要稳定接管 replace 调用。
   */
  const replaceMock = vi.fn();

  /**
   * 每个用例前重置 fetch mock 和本地登录态。
   * 这样可以准确验证认证头注入和 401 刷新流程。
   */
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
    replaceMock.mockReset();
    Object.defineProperty(window, 'location', {
      configurable: true,
      value: {
        pathname: '/activities',
        search: '',
        hash: '',
        replace: replaceMock,
      },
    });
  });

  it('应该附带认证头并解包成功响应', async () => {
    tokenStore.setSession({
      accessToken: 'access-1',
      refreshToken: 'refresh-1',
      username: 'admin',
      displayName: '管理员',
    });

    const fetchMock = vi
      .spyOn(globalThis, 'fetch')
      .mockResolvedValue(
        new Response(
          JSON.stringify({
            code: 0,
            message: 'success',
            data: { orderId: 1001 },
          }),
          { status: 200 },
        ),
      );

    const result = await apiRequest<{ orderId: number }>(
      '/api/v1/internal/orders/1001/status',
    );

    expect(result.orderId).toBe(1001);
    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({
      headers: expect.any(Headers),
    });
    expect(
      (fetchMock.mock.calls[0]?.[1] as RequestInit).headers instanceof Headers,
    ).toBe(true);
    const headers = (fetchMock.mock.calls[0]?.[1] as RequestInit)
      .headers as Headers;
    expect(headers.get('Authorization')).toBe('Bearer access-1');
  });

  it('应该在收到 401 后刷新令牌并重试原请求', async () => {
    tokenStore.setSession({
      accessToken: 'expired-token',
      refreshToken: 'refresh-1',
      username: 'admin',
      displayName: '管理员',
    });

    const fetchMock = vi
      .spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            code: 1004,
            message: 'token expired',
            data: null,
          }),
          { status: 401 },
        ),
      )
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            code: 0,
            message: 'success',
            data: {
              accessToken: 'new-access-token',
              refreshToken: 'new-refresh-token',
              username: 'admin',
              displayName: '管理员',
            },
          }),
          { status: 200 },
        ),
      )
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            code: 0,
            message: 'success',
            data: { orderId: 2002 },
          }),
          { status: 200 },
        ),
      );

    const result = await apiRequest<{ orderId: number }>(
      '/api/v1/internal/orders/2002/status',
    );

    expect(result.orderId).toBe(2002);
    expect(tokenStore.getAccessToken()).toBe('new-access-token');
    expect(tokenStore.getRefreshToken()).toBe('new-refresh-token');
    expect(fetchMock).toHaveBeenCalledTimes(3);
  });

  it('应该在后端返回业务失败码时抛出统一业务异常', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          code: 4001,
          message: 'order not found',
          data: null,
        }),
        { status: 200 },
      ),
    );

    await expect(
      apiRequest('/api/v1/internal/orders/404/status'),
    ).rejects.toBeInstanceOf(ApiBusinessError);
  });

  it('refresh token 失效时应该清理登录态并跳回登录页', async () => {
    tokenStore.setSession({
      accessToken: 'expired-token',
      refreshToken: 'expired-refresh-token',
      username: 'admin',
      displayName: '管理员',
    });

    vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            code: 1004,
            message: 'token expired',
            data: null,
          }),
          { status: 401 },
        ),
      )
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            code: 1005,
            message: 'refresh token expired',
            data: null,
          }),
          { status: 401 },
        ),
      );

    await expect(
      apiRequest('/api/v1/internal/orders/3003/status'),
    ).rejects.toBeInstanceOf(AuthExpiredError);

    expect(tokenStore.isLoggedIn()).toBe(false);
    expect(replaceMock).toHaveBeenCalledWith(LOGIN_PATH);
  });

  it('refresh token 失效时应该携带原始跳转地址', async () => {
    tokenStore.setSession({
      accessToken: 'expired-token',
      refreshToken: 'expired-refresh-token',
      username: 'admin',
      displayName: '管理员',
    });

    Object.defineProperty(window, 'location', {
      configurable: true,
      value: {
        pathname: '/orders/status',
        search: '?from=table',
        hash: '#result',
        replace: replaceMock,
      },
    });

    vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            code: 1004,
            message: 'token expired',
            data: null,
          }),
          { status: 401 },
        ),
      )
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            code: 1005,
            message: 'refresh token expired',
            data: null,
          }),
          { status: 401 },
        ),
      );

    await expect(
      apiRequest('/api/v1/internal/orders/3003/status'),
    ).rejects.toBeInstanceOf(AuthExpiredError);

    expect(replaceMock).toHaveBeenCalledWith(
      `${LOGIN_PATH}?redirect=${encodeURIComponent('/orders/status?from=table#result')}`,
    );
    expect(replaceMock).not.toHaveBeenCalledWith(HOME_PATH);
  });
});

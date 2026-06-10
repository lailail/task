import { beforeEach, describe, expect, it } from 'vitest';
import { tokenStore } from '@/utils/token';

describe('tokenStore', () => {
  /**
   * 每个用例前清空浏览器存储。
   * 避免不同测试之间共享登录态造成误判。
   */
  beforeEach(() => {
    localStorage.clear();
  });

  it('应该保存并读取后台登录态', () => {
    tokenStore.setSession({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      username: 'admin',
      displayName: '管理员',
    });

    expect(tokenStore.getAccessToken()).toBe('access-token');
    expect(tokenStore.getRefreshToken()).toBe('refresh-token');
    expect(tokenStore.getUsername()).toBe('admin');
    expect(tokenStore.getDisplayName()).toBe('管理员');
    expect(tokenStore.isLoggedIn()).toBe(true);
  });

  it('应该在清理后移除全部登录态', () => {
    tokenStore.setSession({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      username: 'admin',
      displayName: '管理员',
    });

    tokenStore.clear();

    expect(tokenStore.getAccessToken()).toBe('');
    expect(tokenStore.getRefreshToken()).toBe('');
    expect(tokenStore.getUsername()).toBe('');
    expect(tokenStore.getDisplayName()).toBe('');
    expect(tokenStore.isLoggedIn()).toBe(false);
  });
});

import type { RunTimeLayoutConfig } from '@umijs/max';
import { history } from '@umijs/max';
import { App, Button, Space, Typography } from 'antd';
import defaultSettings from '../config/defaultSettings';
import { HOME_PATH, LOGIN_PATH } from './constants/auth';
import { tokenStore } from './utils/token';

/**
 * 初始化全局状态。
 * 当前阶段只保留后台登录态和展示名称，避免把模板 demo 的用户模型直接带入项目。
 */
export async function getInitialState(): Promise<{
  isLogin: boolean;
  displayName: string;
}> {
  return {
    isLogin: tokenStore.isLoggedIn(),
    displayName: tokenStore.getDisplayName(),
  };
}

/**
 * 后台布局运行时配置。
 * 这里统一处理登录页跳转、页头退出和基础布局行为。
 */
export const layout: RunTimeLayoutConfig = ({ initialState, setInitialState }) => {
  return {
    ...defaultSettings,
    menuHeaderRender: undefined,
    rightContentRender: () => {
      if (!initialState?.isLogin) {
        return null;
      }

      return (
        <Space>
          <Typography.Text type="secondary">
            {initialState.displayName || '管理员'}
          </Typography.Text>
          <Button
            type="link"
            onClick={() => {
              tokenStore.clear();
              setInitialState({
                isLogin: false,
                displayName: '',
              });
              history.push(LOGIN_PATH);
            }}
          >
            退出登录
          </Button>
        </Space>
      );
    },
    footerRender: () => (
      <Typography.Text type="secondary">
        高并发购票系统后台 · 第一版治理控制台
      </Typography.Text>
    ),
    onPageChange: () => {
      const currentPath = history.location.pathname;

      if (!tokenStore.isLoggedIn() && currentPath !== LOGIN_PATH) {
        history.replace(`${LOGIN_PATH}?redirect=${encodeURIComponent(currentPath)}`);
      }

      if (tokenStore.isLoggedIn() && currentPath === LOGIN_PATH) {
        history.replace(HOME_PATH);
      }
    },
    childrenRender: (children) => {
      return <App>{children}</App>;
    },
  };
};

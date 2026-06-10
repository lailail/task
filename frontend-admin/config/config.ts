import { defineConfig } from '@umijs/max';
import defaultSettings from './defaultSettings';
import routes from './routes';

/**
 * 后台前端全局配置。
 * 这里统一约束路由、中文环境、网关代理和布局外观，避免业务页面各自散落环境判断。
 */
export default defineConfig({
  routes,
  npmClient: 'npm',
  hash: true,
  esbuildMinifyIIFE: true,
  fastRefresh: true,
  model: {},
  initialState: {},
  access: {},
  locale: {
    default: 'zh-CN',
    antd: true,
    baseNavigator: false,
  },
  title: '高并发购票系统后台',
  proxy: {
    '/api': {
      target: process.env.UMI_APP_API_BASE_URL || 'http://localhost:8080',
      changeOrigin: true,
    },
  },
  layout: {
    locale: false,
    ...defaultSettings,
  },
  antd: {
    appConfig: {},
    configProvider: {
      theme: {
        token: {
          fontFamily: '"Microsoft YaHei", "PingFang SC", sans-serif',
        },
      },
    },
  },
  request: {},
});

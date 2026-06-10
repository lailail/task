import type { ProLayoutProps } from '@ant-design/pro-components';

/**
 * @name
 */
const Settings: ProLayoutProps & {
  logo?: string;
} = {
  navTheme: 'light',
  colorPrimary: '#0f766e',
  layout: 'mix',
  contentWidth: 'Fluid',
  fixedHeader: true,
  fixSiderbar: true,
  colorWeak: false,
  title: '高并发购票系统后台',
  logo: undefined,
  iconfontUrl: '',
  token: {
    bgLayout: '#f5f7fa',
  },
};

export default Settings;

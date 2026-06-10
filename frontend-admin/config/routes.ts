export default [
  {
    path: '/login',
    layout: false,
    component: './Login',
  },
  {
    path: '/',
    redirect: '/activities',
  },
  {
    path: '/activities',
    name: '活动管理',
    icon: 'AppstoreOutlined',
    access: 'canAdmin',
    component: './Activities/List',
  },
  {
    path: '/activities/:activityId',
    name: '活动详情',
    hideInMenu: true,
    access: 'canAdmin',
    component: './Activities/Detail',
  },
  {
    path: '/orders/status',
    name: '订单状态查询',
    icon: 'SearchOutlined',
    access: 'canAdmin',
    component: './Orders/Status',
  },
  {
    path: '/payment-reconcile/issues',
    name: '支付对账异常',
    icon: 'WarningOutlined',
    access: 'canAdmin',
    component: './PaymentReconcile/Issues',
  },
  {
    path: '/payment-reconcile/issues/:issueId',
    name: '异常详情',
    hideInMenu: true,
    access: 'canAdmin',
    component: './PaymentReconcile/IssueDetail',
  },
  {
    path: '/observability',
    name: '观测入口',
    icon: 'DashboardOutlined',
    access: 'canAdmin',
    component: './Observability',
  },
  {
    component: './exception/404',
    layout: false,
    path: '/*',
  },
];

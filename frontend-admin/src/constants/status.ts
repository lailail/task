import type { TagProps } from 'antd';

/**
 * 统一状态标签颜色和文案定义。
 * 业务页只传状态值，不允许各自硬编码颜色，确保治理语义一致。
 */
export const STATUS_META: Record<
  string,
  {
    color: TagProps['color'];
    text: string;
  }
> = {
  NOT_STARTED: { color: 'default', text: '未开始' },
  ON_SALE: { color: 'processing', text: '售卖中' },
  SOLD_OUT: { color: 'error', text: '已售罄' },
  ENDED: { color: 'default', text: '已结束' },
  RESERVED: { color: 'processing', text: '已预扣' },
  CONFIRMED: { color: 'success', text: '已确认' },
  RELEASED: { color: 'warning', text: '已释放' },
  CREATED: { color: 'processing', text: '已创建' },
  PAID: { color: 'success', text: '已支付' },
  CANCELLED: { color: 'error', text: '已取消' },
  COMPLETED: { color: 'success', text: '已完成' },
  OPEN: { color: 'warning', text: '待处理' },
  RETRYING: { color: 'processing', text: '重试中' },
  RESOLVED: { color: 'success', text: '已解决' },
  IGNORED: { color: 'default', text: '已忽略' },
};

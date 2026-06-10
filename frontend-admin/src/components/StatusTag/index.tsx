import { Tag } from 'antd';
import { STATUS_META } from '@/constants/status';

/**
 * 状态标签组件属性。
 * 只接收业务状态值，由组件内部统一决定颜色和显示文案。
 */
export interface StatusTagProps {
  value?: string;
}

/**
 * 统一业务状态标签。
 * 这样列表页、详情页、治理页就不会各自定义不一致的颜色和文案。
 *
 * @param props 状态值
 * @returns 统一状态标签
 */
const StatusTag = ({ value }: StatusTagProps) => {
  const meta = value ? STATUS_META[value] : undefined;
  return <Tag color={meta?.color || 'default'}>{meta?.text || value || '-'}</Tag>;
};

export default StatusTag;

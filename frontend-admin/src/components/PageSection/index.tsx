import { Card } from 'antd';
import type { PropsWithChildren } from 'react';

/**
 * 页面分区容器属性。
 * 用于统一后台页面信息块的标题和间距。
 */
export interface PageSectionProps extends PropsWithChildren {
  title: string;
}

/**
 * 页面分区卡片。
 * 用于把一页中的不同信息块拆开，提升后台高密度信息的可读性。
 *
 * @param props 分区标题和内容
 * @returns 标准分区卡片
 */
const PageSection = ({ title, children }: PageSectionProps) => {
  return <Card title={title}>{children}</Card>;
};

export default PageSection;

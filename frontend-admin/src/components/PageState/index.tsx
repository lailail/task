import { Button, Empty, Result, Skeleton } from 'antd';

/**
 * 页面状态组件属性。
 * 用于统一承接后台页面的加载态、空态和错误态，避免每个页面重复拼接状态文案和按钮。
 */
export interface PageStateProps {
  type: 'loading' | 'empty' | 'error';
  title?: string;
  description: string;
  actionText?: string;
  onAction?: () => void;
}

/**
 * 页面状态组件。
 * 该组件只负责状态展示，不参与业务请求，页面通过传入文案和回调保持低耦合。
 *
 * @param props 状态类型、说明文案和可选操作入口
 * @returns 标准化的页面状态展示
 */
const PageState = ({
  type,
  title,
  description,
  actionText,
  onAction,
}: PageStateProps) => {
  /**
   * 统一生成状态操作按钮。
   * 只在页面显式提供回调时展示，避免空按钮或隐藏业务入口。
   */
  const action =
    actionText && onAction ? (
      <Button type="primary" onClick={onAction}>
        {actionText}
      </Button>
    ) : undefined;

  if (type === 'loading') {
    return (
      <Skeleton
        active
        paragraph={{
          rows: 5,
        }}
      />
    );
  }

  if (type === 'error') {
    return (
      <Result
        status="error"
        title={title || '加载失败'}
        subTitle={description}
        extra={action}
      />
    );
  }

  return <Empty description={description}>{action}</Empty>;
};

export default PageState;

import type { ReactNode } from "react";

/**
 * 站点内容外层容器。
 * 统一控制页面宽度、左右留白和纵向节奏，避免各页面自己定义一套布局。
 */
export function SectionShell({
  children,
  className = "",
}: {
  children: ReactNode;
  className?: string;
}) {
  return <main className={`mx-auto w-full max-w-7xl px-6 py-10 md:px-10 ${className}`}>{children}</main>;
}

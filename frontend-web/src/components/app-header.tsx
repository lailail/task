"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { Button, buttonVariants } from "@/components/ui/button";
import { HOME_PATH, LOGIN_PATH, ORDERS_PATH, REGISTER_PATH } from "@/constants/auth";
import { sessionStore, type UserSession } from "@/lib/auth/session-store";
import { cn } from "@/lib/utils";

/**
 * 站点顶部导航。
 * 统一负责品牌展示、用户登录态入口和主要页面跳转，避免每个页面各自拼导航。
 */
export function AppHeader() {
  const router = useRouter();
  const [session, setSession] = useState<UserSession | null>(() =>
    sessionStore.getSession(),
  );

  /**
   * 执行主动退出。
   * 主动退出必须统一清理本地登录态，并把用户送回首页。
   */
  function handleLogout() {
    sessionStore.clear();
    setSession(null);
    router.push(HOME_PATH);
    router.refresh();
  }

  return (
    <header className="sticky top-0 z-30 border-b border-black/10 bg-[rgba(250,247,240,0.86)] backdrop-blur-xl">
      <div className="mx-auto flex w-full max-w-7xl items-center justify-between px-6 py-4 md:px-10">
        <Link className="flex items-center gap-3" href={HOME_PATH}>
          <div className="h-3 w-3 rounded-full bg-[var(--brand-accent)]" />
          <div>
            <p className="text-[0.72rem] font-semibold uppercase tracking-[0.28em] text-black/45">
              Ticket Flash
            </p>
            <p className="text-base font-semibold tracking-[0.08em] text-black">
              抢票现场
            </p>
          </div>
        </Link>

        <nav className="hidden items-center gap-6 text-sm text-black/60 md:flex">
          <Link className="transition hover:text-black" href={HOME_PATH}>
            活动
          </Link>
          <Link className="transition hover:text-black" href={ORDERS_PATH}>
            我的订单
          </Link>
        </nav>

        <div className="flex items-center gap-3">
          {session ? (
            <>
              <div className="hidden text-right md:block">
                <p className="text-sm font-medium text-black">
                  {session.displayName || session.username}
                </p>
                <p className="text-xs text-black/50">@{session.username}</p>
              </div>
              <Button
                className="rounded-full"
                onClick={handleLogout}
                size="sm"
                variant="outline"
              >
                退出
              </Button>
            </>
          ) : (
            <>
              <Link
                className={cn(buttonVariants({ size: "sm", variant: "outline" }), "rounded-full")}
                href={REGISTER_PATH}
              >
                注册
              </Link>
              <Link
                className={cn(buttonVariants({ size: "sm" }), "rounded-full bg-black text-white")}
                href={LOGIN_PATH}
              >
                登录
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}

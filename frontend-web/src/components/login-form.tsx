"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { Button, buttonVariants } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { HOME_PATH, REGISTER_PATH } from "@/constants/auth";
import { sessionStore } from "@/lib/auth/session-store";
import { cn } from "@/lib/utils";
import { loginUser } from "@/services/users/api";
import { ApiBusinessError } from "@/lib/http/api-client";

/**
 * 登录表单。
 * 由服务端页面透传 redirect 目标，客户端只负责收集表单、登录和回跳。
 */
export function LoginForm({ redirect }: { redirect: string }) {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  /**
   * 提交登录。
   * 登录成功后统一写入 sessionStore，并优先回跳用户原始目标页面。
   */
  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setErrorMessage("");

    try {
      const result = await loginUser({
        username,
        password,
      });

      sessionStore.setSession({
        accessToken: result.accessToken,
        refreshToken: result.refreshToken,
        username: result.username,
        displayName: result.displayName,
      });

      router.push(redirect || HOME_PATH);
      router.refresh();
    } catch (error) {
      setErrorMessage(
        error instanceof ApiBusinessError
          ? error.message
          : error instanceof Error
            ? error.message
            : "登录失败，请稍后重试",
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="rounded-[2rem] border border-black/8 bg-white/78 p-8 shadow-[0_30px_100px_rgba(15,23,42,0.08)]">
      <form className="space-y-5" onSubmit={(event) => void handleSubmit(event)}>
        <div className="space-y-2">
          <label className="text-sm font-medium text-black" htmlFor="username">
            用户名
          </label>
          <Input
            id="username"
            onChange={(event) => setUsername(event.target.value)}
            placeholder="请输入注册用户名"
            value={username}
          />
        </div>
        <div className="space-y-2">
          <label className="text-sm font-medium text-black" htmlFor="password">
            密码
          </label>
          <Input
            id="password"
            onChange={(event) => setPassword(event.target.value)}
            placeholder="请输入密码"
            type="password"
            value={password}
          />
        </div>

        {errorMessage ? (
          <p className="rounded-2xl border border-red-500/20 bg-red-500/6 px-4 py-3 text-sm text-red-700">
            {errorMessage}
          </p>
        ) : null}

        <Button className="w-full rounded-full bg-black text-white" disabled={submitting} type="submit">
          {submitting ? "登录中..." : "立即登录"}
        </Button>

        <p className="text-sm text-black/55">
          还没有账号？
          <Link
            className={cn(buttonVariants({ variant: "link", size: "sm" }), "ml-1 h-auto px-0 text-black")}
            href={REGISTER_PATH}
          >
            去注册
          </Link>
        </p>
      </form>
    </section>
  );
}

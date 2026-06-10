"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { AppHeader } from "@/components/app-header";
import { SectionShell } from "@/components/section-shell";
import { Button, buttonVariants } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { LOGIN_PATH } from "@/constants/auth";
import { ApiBusinessError } from "@/lib/http/api-client";
import { cn } from "@/lib/utils";
import { registerUser } from "@/services/users/api";

/**
 * 注册页。
 * 当前只承接第一轮最小注册链路：用户名、密码、昵称，并在成功后跳回登录页。
 */
export default function RegisterPage() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  /**
   * 提交注册。
   * 注册成功后不直接登录，而是回到登录页，保持当前后端接口和前台流程简单清晰。
   */
  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setErrorMessage("");
    setSuccessMessage("");

    try {
      await registerUser({
        username,
        password,
        displayName,
      });

      setSuccessMessage("注册成功，正在跳转登录页...");
      window.setTimeout(() => {
        router.push(LOGIN_PATH);
      }, 700);
    } catch (error) {
      setErrorMessage(
        error instanceof ApiBusinessError
          ? error.message
          : error instanceof Error
            ? error.message
            : "注册失败，请稍后重试",
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="min-h-screen bg-[linear-gradient(180deg,#f9f6ef_0%,#f1ebe0_100%)]">
      <AppHeader />
      <SectionShell className="grid gap-10 py-14 lg:grid-cols-[1fr_1fr] lg:items-center">
        <section className="space-y-6">
          <p className="text-xs font-semibold uppercase tracking-[0.32em] text-black/42">
            Register / 注册
          </p>
          <h1 className="max-w-3xl text-4xl font-semibold leading-tight tracking-[0.04em] text-black md:text-6xl">
            先拿到入场身份，再进入抢票主链路。
          </h1>
          <p className="max-w-2xl text-sm leading-8 text-black/60 md:text-base">
            第一轮注册页不引入复杂资料和多步骤流程，只围绕最小用户创建与后续登录展开。
          </p>
        </section>

        <section className="rounded-[2rem] border border-black/8 bg-white/78 p-8 shadow-[0_30px_100px_rgba(15,23,42,0.08)]">
          <form className="space-y-5" onSubmit={(event) => void handleSubmit(event)}>
            <div className="space-y-2">
              <label className="text-sm font-medium text-black" htmlFor="displayName">
                昵称
              </label>
              <Input
                id="displayName"
                onChange={(event) => setDisplayName(event.target.value)}
                placeholder="请输入展示昵称"
                value={displayName}
              />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium text-black" htmlFor="username">
                用户名
              </label>
              <Input
                id="username"
                onChange={(event) => setUsername(event.target.value)}
                placeholder="建议使用 3 到 32 位用户名"
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
                placeholder="不少于 8 位"
                type="password"
                value={password}
              />
            </div>

            {errorMessage ? (
              <p className="rounded-2xl border border-red-500/20 bg-red-500/6 px-4 py-3 text-sm text-red-700">
                {errorMessage}
              </p>
            ) : null}

            {successMessage ? (
              <p className="rounded-2xl border border-emerald-500/20 bg-emerald-500/6 px-4 py-3 text-sm text-emerald-700">
                {successMessage}
              </p>
            ) : null}

            <Button className="w-full rounded-full bg-black text-white" disabled={submitting} type="submit">
              {submitting ? "注册中..." : "完成注册"}
            </Button>

            <p className="text-sm text-black/55">
              已经有账号？
              <Link
                className={cn(buttonVariants({ variant: "link", size: "sm" }), "ml-1 h-auto px-0 text-black")}
                href={LOGIN_PATH}
              >
                去登录
              </Link>
            </p>
          </form>
        </section>
      </SectionShell>
    </div>
  );
}

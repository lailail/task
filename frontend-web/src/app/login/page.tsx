import { AppHeader } from "@/components/app-header";
import { LoginForm } from "@/components/login-form";
import { SectionShell } from "@/components/section-shell";
import { HOME_PATH } from "@/constants/auth";

/**
 * 登录页。
 * 服务端只负责接收 redirect 查询参数，实际登录动作由客户端表单完成。
 */
export default async function LoginPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const resolvedSearchParams = await searchParams;
  const redirect =
    typeof resolvedSearchParams.redirect === "string"
      ? resolvedSearchParams.redirect
      : HOME_PATH;

  return (
    <div className="min-h-screen bg-[linear-gradient(180deg,#f7f2e7_0%,#f2ede3_100%)]">
      <AppHeader />
      <SectionShell className="grid gap-10 py-14 lg:grid-cols-[1.1fr_0.9fr] lg:items-center">
        <section className="space-y-6">
          <p className="text-xs font-semibold uppercase tracking-[0.32em] text-black/42">
            Sign In / 登录
          </p>
          <h1 className="max-w-3xl text-4xl font-semibold leading-tight tracking-[0.04em] text-black md:text-6xl">
            回到抢票现场，继续你的那一次提交。
          </h1>
          <p className="max-w-2xl text-sm leading-8 text-black/60 md:text-base">
            用户前台的登录页不承担运营口号，而是直接服务主链路：
            登录、拿到统一令牌、回到活动详情、继续发起真实抢票请求。
          </p>
        </section>

        <LoginForm redirect={redirect} />
      </SectionShell>
    </div>
  );
}

import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /**
   * 统一把前端相对路径 /api 代理到网关，避免浏览器直连跨域，
   * 同时保持前后台两个前端都以 gateway-service 作为唯一入口。
   */
  async rewrites() {
    const gatewayBaseUrl =
      process.env.TICKET_WEB_GATEWAY_BASE_URL ?? "http://localhost:8080";

    return [
      {
        source: "/api/:path*",
        destination: `${gatewayBaseUrl}/api/:path*`,
      },
      {
        source: "/actuator/:path*",
        destination: `${gatewayBaseUrl}/actuator/:path*`,
      },
    ];
  },
};

export default nextConfig;

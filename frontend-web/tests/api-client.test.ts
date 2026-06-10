import { ApiBusinessError, apiRequest } from "@/lib/http/api-client";
import { sessionStore } from "@/lib/auth/session-store";

describe("apiRequest", () => {
  beforeEach(() => {
    window.localStorage.clear();
    vi.restoreAllMocks();
  });

  it("应当解包成功响应并返回 data", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        status: 200,
        json: async () => ({
          code: 0,
          message: "success",
          data: { activityId: 1001, activityName: "五月天上海演唱会" },
        }),
      }),
    );

    const result = await apiRequest<{ activityId: number; activityName: string }>(
      "/api/v1/activities/1001",
      { auth: false },
    );

    expect(result.activityId).toBe(1001);
    expect(result.activityName).toBe("五月天上海演唱会");
  });

  it("遇到业务失败响应时应抛出稳定业务异常", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        status: 200,
        json: async () => ({
          code: 40901,
          message: "库存不足",
          data: null,
        }),
      }),
    );

    await expect(apiRequest("/api/v1/seckill/reservations")).rejects.toMatchObject<
      Partial<ApiBusinessError>
    >({
      name: "ApiBusinessError",
      code: 40901,
      message: "库存不足",
    });
  });

  it("应当在受保护请求中自动带上 Bearer 令牌", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      status: 200,
      json: async () => ({
        code: 0,
        message: "success",
        data: { ok: true },
      }),
    });

    vi.stubGlobal("fetch", fetchMock);
    sessionStore.setSession({
      accessToken: "access-token",
      refreshToken: "refresh-token",
      username: "demo_user",
      displayName: "演示用户",
    });

    await apiRequest("/api/v1/activities");

    expect(fetchMock).toHaveBeenCalledWith(
      "/api/v1/activities",
      expect.objectContaining({
        headers: expect.any(Headers),
      }),
    );

    const [, requestOptions] = fetchMock.mock.calls[0];
    const headers = requestOptions.headers as Headers;
    expect(headers.get("Authorization")).toBe("Bearer access-token");
  });
});

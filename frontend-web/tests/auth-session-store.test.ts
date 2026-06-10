import {
  sessionStore,
  type UserSession,
} from "@/lib/auth/session-store";

describe("sessionStore", () => {
  const sampleSession: UserSession = {
    accessToken: "access-token",
    refreshToken: "refresh-token",
    username: "demo_user",
    displayName: "演示用户",
  };

  beforeEach(() => {
    window.localStorage.clear();
  });

  it("应当完整写入并读出登录态", () => {
    sessionStore.setSession(sampleSession);

    expect(sessionStore.getSession()).toEqual(sampleSession);
    expect(sessionStore.getAccessToken()).toBe("access-token");
    expect(sessionStore.getRefreshToken()).toBe("refresh-token");
  });

  it("清理后不应继续返回旧登录态", () => {
    sessionStore.setSession(sampleSession);

    sessionStore.clear();

    expect(sessionStore.getSession()).toBeNull();
    expect(sessionStore.getAccessToken()).toBeNull();
    expect(sessionStore.getRefreshToken()).toBeNull();
  });
});

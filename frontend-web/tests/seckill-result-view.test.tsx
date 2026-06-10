import { render, screen, waitFor } from "@testing-library/react";
import { SeckillResultView } from "@/components/seckill-result-view";

vi.mock("next/navigation", () => ({
  useRouter: () => ({
    push: vi.fn(),
    refresh: vi.fn(),
  }),
}));

describe("SeckillResultView", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        status: 200,
        json: async () => ({
          code: 0,
          message: "success",
          data: {
            reservationId: "reservation-001",
            resultStatus: "ORDER_CREATED",
            orderId: 20001,
            orderNo: "ORD-20001",
            orderStatus: "CREATED",
            activityId: 1001,
            ticketId: 501,
            expireAt: "2026-06-10T12:30:00Z",
          },
        }),
      }),
    );
  });

  it("should query real reservation result and render order-created guidance", async () => {
    render(
      <SeckillResultView
        context={{
          status: "RESERVED",
          reservationId: "reservation-001",
          activityId: "1001",
          ticketId: "501",
          ticketName: "内场票",
          expireAt: "2026-06-10T12:30:00Z",
        }}
      />,
    );

    expect(await screen.findByText("订单已创建，等待后续支付或关闭")).toBeInTheDocument();
    expect(screen.getByText("ORD-20001")).toBeInTheDocument();

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith(
        "/api/v1/orders/reservations/reservation-001",
        expect.objectContaining({
          method: "GET",
        }),
      );
    });
  });

  it("should render duplicate warning without expiry block", () => {
    render(
      <SeckillResultView
        context={{
          status: "DUPLICATE",
          activityId: "1001",
          ticketId: "2001",
          ticketName: "内场票",
        }}
      />,
    );

    expect(screen.getByText("这张票你已经提交过一次了")).toBeInTheDocument();
    expect(screen.queryByText("结果过期时间")).not.toBeInTheDocument();
  });
});

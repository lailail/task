import { render, screen } from "@testing-library/react";
import { OrdersView } from "@/components/orders-view";

vi.mock("next/navigation", () => ({
  useRouter: () => ({
    push: vi.fn(),
    refresh: vi.fn(),
  }),
}));

describe("OrdersView", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it("should render real current-user orders", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        status: 200,
        json: async () => ({
          code: 0,
          message: "success",
          data: {
            current: 1,
            pageSize: 10,
            total: 1,
            records: [
              {
                orderId: 20001,
                orderNo: "ORD-20001",
                reservationId: "reservation-001",
                activityId: 1001,
                ticketId: 501,
                quantity: 1,
                amountCent: 68000,
                orderStatus: "CREATED",
                expireAt: "2026-06-10T12:30:00Z",
              },
            ],
          },
        }),
      }),
    );

    render(
      <OrdersView
        recentContext={{
          status: "RESERVED",
          reservationId: "reservation-001",
        }}
      />,
    );

    expect(await screen.findByText("ORD-20001")).toBeInTheDocument();
    expect(screen.getByText("reservationId: reservation-001")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "回到刚才的结果页" })).toHaveAttribute(
      "href",
      "/seckill/result?status=RESERVED&reservationId=reservation-001",
    );
  });
});

import { queryMyOrders, queryReservationResult } from "@/services/orders/api";

describe("orders api", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it("should query current user orders without frontend userId", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      status: 200,
      json: async () => ({
        code: 0,
        message: "success",
        data: {
          current: 1,
          pageSize: 10,
          total: 0,
          records: [],
        },
      }),
    });
    vi.stubGlobal("fetch", fetchMock);

    await queryMyOrders(1, 10);

    expect(fetchMock).toHaveBeenCalledWith(
      "/api/v1/orders?current=1&pageSize=10",
      expect.objectContaining({
        method: "GET",
      }),
    );
  });

  it("should query reservation result by encoded reservationId", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      status: 200,
      json: async () => ({
        code: 0,
        message: "success",
        data: {
          reservationId: "reservation/001",
          resultStatus: "RESERVED",
        },
      }),
    });
    vi.stubGlobal("fetch", fetchMock);

    await queryReservationResult("reservation/001");

    expect(fetchMock).toHaveBeenCalledWith(
      "/api/v1/orders/reservations/reservation%2F001",
      expect.objectContaining({
        method: "GET",
      }),
    );
  });
});

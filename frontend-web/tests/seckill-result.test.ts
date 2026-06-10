import { ApiBusinessError } from "@/lib/http/api-client";
import {
  SECKILL_RESULT_STATUS,
  buildResultHref,
  buildSeckillResultViewModel,
  classifyReserveSubmitError,
  hasRecentReservationContext,
  mergeReservationResultContext,
  normalizeSeckillResultStatus,
} from "@/lib/seckill-result";

describe("seckill result helpers", () => {
  it("should map stable seckill business codes to frontend result states", () => {
    expect(
      classifyReserveSubmitError(new ApiBusinessError(3004, "seckill stock not enough")),
    ).toMatchObject({
      status: SECKILL_RESULT_STATUS.OUT_OF_STOCK,
      message: "当前票档库存不足，可以换一个票档或稍后再试。",
    });

    expect(
      classifyReserveSubmitError(new ApiBusinessError(3003, "duplicate seckill request")),
    ).toMatchObject({
      status: SECKILL_RESULT_STATUS.DUPLICATE,
      message: "你已经提交过这张票的抢票请求，请先查看最近一次结果。",
    });
  });

  it("should downgrade unknown submit failures to a stable failed state", () => {
    expect(classifyReserveSubmitError(new Error("boom"))).toEqual({
      status: SECKILL_RESULT_STATUS.FAILED,
      message: "系统有点忙，这次抢票没有提交成功，请稍后再试。",
    });
  });

  it("should build guidance for real order-created result", () => {
    const viewModel = buildSeckillResultViewModel({
      status: "ORDER_CREATED",
      reservationId: "reservation-001",
      orderNo: "ORD-20001",
      activityId: "1001",
      ticketId: "2001",
      ticketName: "内场票",
      expireAt: "2026-06-10T12:30:00Z",
    });

    expect(viewModel.normalizedStatus).toBe(SECKILL_RESULT_STATUS.ORDER_CREATED);
    expect(viewModel.theme).toBe("success");
    expect(viewModel.shouldShowExpiry).toBe(true);
    expect(viewModel.headline).toContain("订单已创建");
  });

  it("should merge backend reservation result into existing context", () => {
    expect(
      mergeReservationResultContext(
        {
          status: "RESERVED",
          reservationId: "reservation-001",
          ticketName: "内场票",
        },
        {
          reservationId: "reservation-001",
          resultStatus: "ORDER_CREATED",
          orderId: 20001,
          orderNo: "ORD-20001",
          orderStatus: "CREATED",
          activityId: 1001,
          ticketId: 501,
          expireAt: "2026-06-10T12:30:00Z",
        },
      ),
    ).toMatchObject({
      status: "ORDER_CREATED",
      orderId: "20001",
      orderNo: "ORD-20001",
      orderStatus: "CREATED",
      activityId: "1001",
      ticketId: "501",
      ticketName: "内场票",
    });
  });

  it("should normalize unknown status and build recent result href", () => {
    expect(normalizeSeckillResultStatus("something-new")).toBe(SECKILL_RESULT_STATUS.UNKNOWN);
    expect(
      hasRecentReservationContext({
        status: "",
        reservationId: "reservation-001",
      }),
    ).toBe(true);
    expect(buildResultHref({ status: "RESERVED", reservationId: "reservation-001" })).toBe(
      "/seckill/result?status=RESERVED&reservationId=reservation-001",
    );
  });
});

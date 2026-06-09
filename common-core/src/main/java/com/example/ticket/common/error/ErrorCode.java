package com.example.ticket.common.error;

/**
 * 统一业务错误码定义。
 * 用于约束接口返回和异常处理时使用的稳定错误语义。
 */
public enum ErrorCode {
    SUCCESS(0, "success"),
    SYSTEM_ERROR(500, "system error"),
    USERNAME_ALREADY_EXISTS(1001, "username already exists"),
    INVALID_CREDENTIALS(1002, "invalid credentials"),
    TOKEN_INVALID(1003, "token invalid"),
    TOKEN_EXPIRED(1004, "token expired"),
    REFRESH_TOKEN_INVALID(1005, "refresh token invalid"),
    AUTHENTICATED_USER_MISSING(1006, "authenticated user missing"),
    ACTIVITY_NOT_FOUND(2001, "activity not found"),
    ORDER_NOT_FOUND(4001, "order not found"),
    ORDER_STATUS_INVALID(4002, "order status invalid"),
    ORDER_CANCEL_FORBIDDEN(4003, "order cancel forbidden"),
    ORDER_COMPLETE_FORBIDDEN(4004, "order complete forbidden"),
    PAYMENT_RECORD_NOT_FOUND(5001, "payment record not found"),
    PAYMENT_STATUS_INVALID(5002, "payment status invalid"),
    PAYMENT_RECONCILE_ISSUE_NOT_FOUND(5003, "payment reconcile issue not found"),
    PAYMENT_RECONCILE_ISSUE_STATUS_INVALID(5004, "payment reconcile issue status invalid"),
    SECKILL_ACTIVITY_NOT_FOUND(3001, "seckill activity not found"),
    SECKILL_ACTIVITY_NOT_ON_SALE(3002, "seckill activity not on sale"),
    SECKILL_DUPLICATE_REQUEST(3003, "duplicate seckill request"),
    SECKILL_STOCK_NOT_ENOUGH(3004, "seckill stock not enough");

    private final int code;
    private final String message;

    /**
     * 构造错误码枚举项。
     *
     * @param code 错误码数值
     * @param message 错误描述
     */
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取错误码数值。
     *
     * @return 错误码数值
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取错误码描述。
     *
     * @return 错误码描述
     */
    public String getMessage() {
        return message;
    }
}

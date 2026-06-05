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
    ACTIVITY_NOT_FOUND(2001, "activity not found");

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

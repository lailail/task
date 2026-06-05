package com.example.ticket.common.error;

/**
 * 业务异常。
 * 用于在服务层和控制层之间传递稳定的业务错误码与错误信息。
 */
public class BusinessException extends RuntimeException {
    private final int code;

    /**
     * 使用错误码枚举构建业务异常。
     *
     * @param errorCode 业务错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 获取业务错误码。
     *
     * @return 业务错误码
     */
    public int getCode() {
        return code;
    }
}

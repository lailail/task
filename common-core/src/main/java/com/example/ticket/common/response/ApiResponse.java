package com.example.ticket.common.response;

import com.example.ticket.common.error.ErrorCode;

/**
 * 统一接口响应包装对象。
 *
 * @param <T> 业务数据类型
 */
public class ApiResponse<T> {
    private final int code;
    private final String message;
    private final T data;

    /**
     * 构造统一响应对象。
     *
     * @param code 响应码
     * @param message 响应消息
     * @param data 响应数据
     */
    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 构造成功响应。
     *
     * @param data 响应数据
     * @return 成功响应对象
     * @param <T> 业务数据类型
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    /**
     * 根据错误码枚举构造失败响应。
     *
     * @param errorCode 业务错误码
     * @return 失败响应对象
     * @param <T> 业务数据类型
     */
    public static <T> ApiResponse<T> failure(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 根据显式错误信息构造失败响应。
     *
     * @param code 响应码
     * @param message 响应消息
     * @return 失败响应对象
     * @param <T> 业务数据类型
     */
    public static <T> ApiResponse<T> failure(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 获取响应码。
     *
     * @return 响应码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取响应消息。
     *
     * @return 响应消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 获取响应数据。
     *
     * @return 响应数据
     */
    public T getData() {
        return data;
    }
}


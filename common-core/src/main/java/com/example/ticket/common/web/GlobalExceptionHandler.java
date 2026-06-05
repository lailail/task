package com.example.ticket.common.web;

import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 * 负责把控制层和服务层抛出的异常统一转换成稳定的 HTTP 状态码与响应体，避免把内部异常细节直接暴露给客户端。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常并映射为稳定的 HTTP 状态与响应体。
     *
     * @param exception 业务异常
     * @return 统一失败响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        // 业务错误码和 HTTP 状态码的映射必须稳定，后续前端和调用方会依赖这些语义。
        HttpStatus status = resolveBusinessStatus(exception);
        return ResponseEntity.status(status)
                .body(ApiResponse.failure(exception.getCode(), exception.getMessage()));
    }

    /**
     * 处理参数校验异常。
     *
     * @param exception 参数校验异常
     * @return 统一失败响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        // 当前阶段优先返回第一个字段校验错误，先保证调用方能稳定拿到可读提示。
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("invalid request");
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(HttpStatus.BAD_REQUEST.value(), message));
    }

    /**
     * 处理未识别的系统异常。
     *
     * @param exception 未识别异常
     * @return 统一失败响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        // 未识别异常统一收口，避免把内部堆栈信息透传到外部接口。
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(ErrorCode.SYSTEM_ERROR));
    }

    /**
     * 根据业务异常解析对应的 HTTP 状态码。
     *
     * @param exception 业务异常
     * @return 对应的 HTTP 状态码
     */
    private HttpStatus resolveBusinessStatus(BusinessException exception) {
        if (exception.getCode() == ErrorCode.ACTIVITY_NOT_FOUND.getCode()) {
            return HttpStatus.NOT_FOUND;
        }
        if (exception.getCode() == ErrorCode.INVALID_CREDENTIALS.getCode()) {
            return HttpStatus.UNAUTHORIZED;
        }
        return HttpStatus.BAD_REQUEST;
    }
}

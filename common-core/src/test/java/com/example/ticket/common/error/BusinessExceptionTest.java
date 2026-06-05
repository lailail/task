package com.example.ticket.common.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessExceptionTest {

    @Test
    void should_expose_error_code_and_message() {
        BusinessException exception = new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);

        assertEquals(ErrorCode.USERNAME_ALREADY_EXISTS.getCode(), exception.getCode());
        assertEquals(ErrorCode.USERNAME_ALREADY_EXISTS.getMessage(), exception.getMessage());
    }
}

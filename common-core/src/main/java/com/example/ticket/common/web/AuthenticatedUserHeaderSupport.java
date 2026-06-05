package com.example.ticket.common.web;

import com.example.ticket.common.auth.AuthHeaderConstants;
import com.example.ticket.common.auth.AuthenticatedUser;
import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 已认证用户请求头工具。
 * 用于在业务服务中统一解析网关透传的身份头，避免各控制器自行处理字段名和异常语义。
 */
public final class AuthenticatedUserHeaderSupport {

    /**
     * 禁止实例化工具类。
     */
    private AuthenticatedUserHeaderSupport() {
    }

    /**
     * 从请求头中提取已认证用户。
     *
     * @param request HTTP 请求
     * @return 已认证用户
     */
    public static AuthenticatedUser extractAuthenticatedUser(HttpServletRequest request) {
        String userIdHeader = request.getHeader(AuthHeaderConstants.AUTHENTICATED_USER_ID);
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new BusinessException(ErrorCode.AUTHENTICATED_USER_MISSING);
        }

        try {
            AuthenticatedUser authenticatedUser = new AuthenticatedUser();
            authenticatedUser.setUserId(Long.valueOf(userIdHeader));
            authenticatedUser.setUsername(request.getHeader(AuthHeaderConstants.AUTHENTICATED_USERNAME));
            authenticatedUser.setDisplayName(request.getHeader(AuthHeaderConstants.AUTHENTICATED_DISPLAY_NAME));
            authenticatedUser.setTokenId(request.getHeader(AuthHeaderConstants.AUTHENTICATED_TOKEN_ID));
            return authenticatedUser;
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.AUTHENTICATED_USER_MISSING);
        }
    }
}

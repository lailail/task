package com.example.ticket.common.auth;

import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 令牌支持组件。
 * 用于集中处理 token 的签发、验签和声明提取，避免用户服务与网关服务各自维护一套规则。
 */
public class JwtTokenSupport {
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    /**
     * 构造 JWT 支持组件。
     *
     * @param jwtProperties JWT 配置属性
     */
    public JwtTokenSupport(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成访问令牌。
     *
     * @param authenticatedUser 已认证用户
     * @return 访问令牌
     */
    public String createAccessToken(AuthenticatedUser authenticatedUser) {
        Instant issuedAt = Instant.now();
        Instant expireAt = issuedAt.plusSeconds(jwtProperties.getAccessTokenExpireSeconds());
        return Jwts.builder()
                .subject(String.valueOf(authenticatedUser.getUserId()))
                .issuer(jwtProperties.getIssuer())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expireAt))
                .claim(JwtClaimConstants.USERNAME, authenticatedUser.getUsername())
                .claim(JwtClaimConstants.DISPLAY_NAME, authenticatedUser.getDisplayName())
                .claim(JwtClaimConstants.TOKEN_TYPE, JwtTokenType.ACCESS.getValue())
                .signWith(secretKey)
                .compact();
    }

    /**
     * 生成刷新令牌。
     *
     * @param authenticatedUser 已认证用户
     * @return 刷新令牌
     */
    public String createRefreshToken(AuthenticatedUser authenticatedUser) {
        Instant issuedAt = Instant.now();
        Instant expireAt = issuedAt.plusSeconds(jwtProperties.getRefreshTokenExpireSeconds());
        return Jwts.builder()
                .subject(String.valueOf(authenticatedUser.getUserId()))
                .issuer(jwtProperties.getIssuer())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expireAt))
                .claim(JwtClaimConstants.USERNAME, authenticatedUser.getUsername())
                .claim(JwtClaimConstants.DISPLAY_NAME, authenticatedUser.getDisplayName())
                .claim(JwtClaimConstants.TOKEN_TYPE, JwtTokenType.REFRESH.getValue())
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析并校验 JWT。
     *
     * @param token JWT 字符串
     * @return 已解析令牌
     */
    public ParsedJwtToken parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(jwtProperties.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return toParsedToken(claims);
        } catch (ExpiredJwtException exception) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

    /**
     * 获取访问令牌有效期秒数。
     *
     * @return 访问令牌有效期秒数
     */
    public long getAccessTokenExpireSeconds() {
        return jwtProperties.getAccessTokenExpireSeconds();
    }

    /**
     * 获取刷新令牌有效期秒数。
     *
     * @return 刷新令牌有效期秒数
     */
    public long getRefreshTokenExpireSeconds() {
        return jwtProperties.getRefreshTokenExpireSeconds();
    }

    /**
     * 把声明对象转换成稳定的认证结果。
     *
     * @param claims JWT 声明
     * @return 已解析令牌
     */
    private ParsedJwtToken toParsedToken(Claims claims) {
        JwtTokenType tokenType = JwtTokenType.fromValue(claims.get(JwtClaimConstants.TOKEN_TYPE, String.class));
        if (tokenType == null) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserId(Long.valueOf(claims.getSubject()));
        authenticatedUser.setUsername(claims.get(JwtClaimConstants.USERNAME, String.class));
        authenticatedUser.setDisplayName(claims.get(JwtClaimConstants.DISPLAY_NAME, String.class));
        authenticatedUser.setTokenId(claims.getId());

        ParsedJwtToken parsedJwtToken = new ParsedJwtToken();
        parsedJwtToken.setAuthenticatedUser(authenticatedUser);
        parsedJwtToken.setTokenType(tokenType);
        parsedJwtToken.setIssuedAt(claims.getIssuedAt().toInstant());
        parsedJwtToken.setExpireAt(claims.getExpiration().toInstant());
        return parsedJwtToken;
    }
}

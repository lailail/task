package com.example.ticket.user.controller;

import com.example.ticket.user.dto.UserDTO;
import com.example.ticket.user.request.UserRefreshTokenRequest;
import com.example.ticket.user.response.UserLoginResponse;
import com.example.ticket.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户控制器集成测试。
 * 用于固定 HTTP 层的请求与响应契约，不让仓储实现变动影响控制层验证。
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    /**
     * 初始化控制层测试使用的服务桩数据。
     */
    @BeforeEach
    void setUp() {
        UserDTO user = new UserDTO();
        user.setUserId(1L);
        user.setUsername("alice");
        user.setDisplayName("前台中文昵称");

        UserLoginResponse loginResponse = new UserLoginResponse();
        loginResponse.setUserId(1L);
        loginResponse.setUsername("alice");
        loginResponse.setDisplayName("前台中文昵称");
        loginResponse.setAccessToken("access-token");
        loginResponse.setRefreshToken("refresh-token");
        loginResponse.setTokenType("Bearer");

        when(userService.register(any())).thenReturn(user);
        when(userService.login(any())).thenReturn(loginResponse);
        when(userService.refreshToken(any(UserRefreshTokenRequest.class))).thenReturn(loginResponse);
    }

    /**
     * 注册接口应在中文昵称场景下返回统一成功响应。
     */
    @Test
    void should_register_user_with_chinese_display_name_via_http() throws Exception {
        org.mockito.ArgumentCaptor<com.example.ticket.user.request.UserRegisterRequest> requestCaptor =
                org.mockito.ArgumentCaptor.forClass(com.example.ticket.user.request.UserRegisterRequest.class);
        MvcResult result = mockMvc.perform(post("/api/v1/users/register")
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "alice",
                                  "password": "password123",
                                  "displayName": "前台中文昵称"
                                }
                                """.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("alice"))
                .andExpect(jsonPath("$.data.displayName").value("前台中文昵称"))
                .andReturn();

        verify(userService).register(requestCaptor.capture());
        String requestBody = result.getRequest().getContentAsString();
        String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertEquals("前台中文昵称", requestCaptor.getValue().getDisplayName());
        org.junit.jupiter.api.Assertions.assertEquals(StandardCharsets.UTF_8.name(), result.getRequest().getCharacterEncoding());
        org.junit.jupiter.api.Assertions.assertEquals(StandardCharsets.UTF_8.name(), result.getResponse().getCharacterEncoding());
        org.junit.jupiter.api.Assertions.assertTrue(requestBody.contains("\"displayName\": \"前台中文昵称\""));
        org.junit.jupiter.api.Assertions.assertTrue(responseBody.contains("\"displayName\":\"前台中文昵称\""));
    }

    /**
     * 登录接口应返回统一成功响应和正式令牌。
     */
    @Test
    void should_login_user_via_http() throws Exception {
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "alice",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("alice"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    /**
     * 刷新接口应返回统一成功响应和新令牌。
     */
    @Test
    void should_refresh_token_via_http() throws Exception {
        mockMvc.perform(post("/api/v1/users/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "refresh-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    /**
     * 当服务层抛出用户名重复错误时，应映射为稳定业务响应。
     */
    @Test
    void should_return_business_error_when_username_exists() throws Exception {
        String payload = """
                {
                  "username": "alice",
                  "password": "password123",
                  "displayName": "Alice"
                }
                """;
        when(userService.register(any())).thenThrow(new com.example.ticket.common.error.BusinessException(
                com.example.ticket.common.error.ErrorCode.USERNAME_ALREADY_EXISTS
        ));

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1001));
    }
}

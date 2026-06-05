package com.example.ticket.ticket.controller;

import com.example.ticket.ticket.dto.ActivityDTO;
import com.example.ticket.ticket.dto.TicketItemDTO;
import com.example.ticket.ticket.service.TicketQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 活动查询控制器集成测试。
 * 用于固定 HTTP 契约，不让底层仓储切换影响控制层验证。
 */
@SpringBootTest
@AutoConfigureMockMvc
class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketQueryService ticketQueryService;

    /**
     * 列表接口应返回统一成功响应。
     */
    @Test
    void should_list_activities_via_http() throws Exception {
        when(ticketQueryService.listActivities()).thenReturn(List.of(buildActivity()));

        mockMvc.perform(get("/api/v1/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].activityName").value("五月天上海演唱会"));
    }

    /**
     * 详情接口应返回统一成功响应。
     */
    @Test
    void should_return_activity_detail_via_http() throws Exception {
        when(ticketQueryService.getActivityDetail(1001L)).thenReturn(buildActivity());

        mockMvc.perform(get("/api/v1/activities/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.activityId").value(1001))
                .andExpect(jsonPath("$.data.ticketItems[0].ticketName").exists());
    }

    /**
     * 活动不存在时，应映射为稳定业务错误。
     */
    @Test
    void should_return_not_found_business_error_for_missing_activity() throws Exception {
        when(ticketQueryService.getActivityDetail(9999L)).thenThrow(new com.example.ticket.common.error.BusinessException(
                com.example.ticket.common.error.ErrorCode.ACTIVITY_NOT_FOUND
        ));

        mockMvc.perform(get("/api/v1/activities/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(2001));
    }

    /**
     * 构造演示活动对象。
     *
     * @return 活动对象
     */
    private ActivityDTO buildActivity() {
        TicketItemDTO ticketItem = new TicketItemDTO();
        ticketItem.setTicketId(501L);
        ticketItem.setTicketName("看台票");
        ticketItem.setPrice(49900);
        ticketItem.setAvailableStock(800);

        ActivityDTO activity = new ActivityDTO();
        activity.setActivityId(1001L);
        activity.setActivityName("五月天上海演唱会");
        activity.setCity("上海");
        activity.setVenueName("上海体育场");
        activity.setSaleStatus("ON_SALE");
        activity.setTicketItems(List.of(ticketItem));
        return activity;
    }
}

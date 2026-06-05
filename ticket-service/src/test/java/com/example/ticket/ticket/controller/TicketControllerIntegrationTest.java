package com.example.ticket.ticket.controller;

import com.example.ticket.ticket.support.TicketSampleData;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_list_activities_via_http() throws Exception {
        mockMvc.perform(get("/api/v1/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].activityName").value(TicketSampleData.CONCERT_ACTIVITY_NAME));
    }

    @Test
    void should_return_activity_detail_via_http() throws Exception {
        mockMvc.perform(get("/api/v1/activities/" + TicketSampleData.CONCERT_ACTIVITY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.activityId").value(TicketSampleData.CONCERT_ACTIVITY_ID))
                .andExpect(jsonPath("$.data.ticketItems[0].ticketName").exists());
    }

    @Test
    void should_return_not_found_business_error_for_missing_activity() throws Exception {
        mockMvc.perform(get("/api/v1/activities/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(2001));
    }
}

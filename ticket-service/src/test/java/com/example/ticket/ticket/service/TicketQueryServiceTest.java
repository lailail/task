package com.example.ticket.ticket.service;

import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.ticket.dto.ActivityDTO;
import com.example.ticket.ticket.repository.InMemoryTicketRepository;
import com.example.ticket.ticket.service.impl.TicketQueryServiceImpl;
import com.example.ticket.ticket.support.TicketSampleData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketQueryServiceTest {

    private TicketQueryService service;

    @BeforeEach
    void setUp() {
        service = new TicketQueryServiceImpl(new InMemoryTicketRepository());
    }

    @Test
    void should_list_available_activities() {
        List<ActivityDTO> activities = service.listActivities();

        assertFalse(activities.isEmpty());
        assertEquals(TicketSampleData.CONCERT_ACTIVITY_NAME, activities.get(0).getActivityName());
    }

    @Test
    void should_return_activity_detail_when_activity_exists() {
        ActivityDTO activity = service.getActivityDetail(TicketSampleData.CONCERT_ACTIVITY_ID);

        assertEquals(TicketSampleData.CONCERT_ACTIVITY_ID, activity.getActivityId());
        assertFalse(activity.getTicketItems().isEmpty());
    }

    @Test
    void should_throw_when_activity_is_missing() {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.getActivityDetail(9999L));
        assertEquals(ErrorCode.ACTIVITY_NOT_FOUND.getCode(), exception.getCode());
    }
}

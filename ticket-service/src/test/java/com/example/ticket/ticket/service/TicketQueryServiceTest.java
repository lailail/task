package com.example.ticket.ticket.service;

import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.ticket.dto.ActivityDTO;
import com.example.ticket.ticket.dto.TicketItemDTO;
import com.example.ticket.ticket.repository.TicketRepository;
import com.example.ticket.ticket.service.impl.TicketQueryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * 活动查询服务单元测试。
 * 用于验证活动查询编排逻辑不依赖具体仓储实现。
 */
@ExtendWith(MockitoExtension.class)
class TicketQueryServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    private TicketQueryService service;

    /**
     * 构造被测活动查询服务。
     * 当前测试只关心查询编排和错误语义，因此仓储使用接口 mock。
     */
    @BeforeEach
    void setUp() {
        service = new TicketQueryServiceImpl(ticketRepository);
    }

    /**
     * 仓储返回活动列表时，应原样返回给调用方。
     */
    @Test
    void should_list_available_activities() {
        when(ticketRepository.findAllActivities()).thenReturn(List.of(buildActivity()));

        List<ActivityDTO> activities = service.listActivities();

        assertFalse(activities.isEmpty());
        assertEquals("五月天上海演唱会", activities.get(0).getActivityName());
    }

    /**
     * 活动存在时，应返回活动详情。
     */
    @Test
    void should_return_activity_detail_when_activity_exists() {
        when(ticketRepository.findActivityById(1001L)).thenReturn(Optional.of(buildActivity()));

        ActivityDTO activity = service.getActivityDetail(1001L);

        assertEquals(1001L, activity.getActivityId());
        assertFalse(activity.getTicketItems().isEmpty());
    }

    /**
     * 活动不存在时，应返回稳定业务错误。
     */
    @Test
    void should_throw_when_activity_is_missing() {
        when(ticketRepository.findActivityById(9999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.getActivityDetail(9999L));
        assertEquals(ErrorCode.ACTIVITY_NOT_FOUND.getCode(), exception.getCode());
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

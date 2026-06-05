package com.example.ticket.job.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.event.stock.StockEventConstants;
import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.job.domain.JobStockReleaseTaskDO;
import com.example.ticket.job.gateway.StockReleaseMessageSender;
import com.example.ticket.job.gateway.impl.MybatisStockReleaseTaskStore;
import com.example.ticket.job.mapper.JobStockReleaseTaskMapper;
import com.example.ticket.job.service.impl.StockReleaseRetryServiceImpl;
import com.example.ticket.job.support.JobConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 库存释放补发服务单元测试。
 * 用于固定“只补发到期任务，成功后收敛状态，失败后推进下一次补发时间”的调度边界。
 */
@ExtendWith(MockitoExtension.class)
class StockReleaseRetryServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private JobStockReleaseTaskMapper stockReleaseTaskMapper;

    @Mock
    private StockReleaseMessageSender stockReleaseMessageSender;

    private StockReleaseRetryService stockReleaseRetryService;

    /**
     * 构造被测补发服务。
     * 当前测试通过 mock 任务表与 MQ 发送器，隔离补发调度决策。
     */
    @BeforeEach
    void setUp() {
        stockReleaseRetryService = new StockReleaseRetryServiceImpl(
                new MybatisStockReleaseTaskStore(stockReleaseTaskMapper),
                stockReleaseMessageSender,
                objectMapper,
                100,
                30,
                20
        );
    }

    /**
     * 到期补发任务发送成功时，应把状态推进为已发送并回写最后发送时间。
     *
     * @throws JsonProcessingException 当构造任务载荷时抛出异常
     */
    @Test
    void should_mark_retry_task_sent_when_retry_succeeds() throws JsonProcessingException {
        JobStockReleaseTaskDO task = buildRetryingTask();
        when(stockReleaseTaskMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<JobStockReleaseTaskDO>().setRecords(List.of(task)));

        stockReleaseRetryService.retryDueTasks(LocalDateTime.of(2026, 6, 5, 19, 0, 0));

        ArgumentCaptor<StockReleaseEvent> eventCaptor = ArgumentCaptor.forClass(StockReleaseEvent.class);
        ArgumentCaptor<JobStockReleaseTaskDO> updateCaptor = ArgumentCaptor.forClass(JobStockReleaseTaskDO.class);
        verify(stockReleaseMessageSender).send(eventCaptor.capture());
        verify(stockReleaseTaskMapper).updateById(updateCaptor.capture());
        assertEquals("release-event-001", eventCaptor.getValue().getEventId());
        assertEquals(JobConstants.STOCK_RELEASE_TASK_STATUS_SENT, updateCaptor.getValue().getTaskStatus());
        assertNotNull(updateCaptor.getValue().getLastSentAt());
    }

    /**
     * 到期补发任务再次发送失败时，应累加重试次数并保留下一次补发时间。
     *
     * @throws JsonProcessingException 当构造任务载荷时抛出异常
     */
    @Test
    void should_reschedule_retry_task_when_retry_fails() throws JsonProcessingException {
        JobStockReleaseTaskDO task = buildRetryingTask();
        when(stockReleaseTaskMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<JobStockReleaseTaskDO>().setRecords(List.of(task)));
        doThrow(new IllegalStateException("mq timeout"))
                .when(stockReleaseMessageSender)
                .send(any(StockReleaseEvent.class));

        stockReleaseRetryService.retryDueTasks(LocalDateTime.of(2026, 6, 5, 19, 0, 0));

        ArgumentCaptor<JobStockReleaseTaskDO> updateCaptor = ArgumentCaptor.forClass(JobStockReleaseTaskDO.class);
        verify(stockReleaseTaskMapper).updateById(updateCaptor.capture());
        assertEquals(JobConstants.STOCK_RELEASE_TASK_STATUS_RETRYING, updateCaptor.getValue().getTaskStatus());
        assertEquals(3, updateCaptor.getValue().getRetryCount());
        assertEquals("mq timeout", updateCaptor.getValue().getLastErrorMessage());
        assertNotNull(updateCaptor.getValue().getNextRetryAt());
    }

    /**
     * 构造待补发任务。
     *
     * @return 待补发任务
     * @throws JsonProcessingException 当序列化事件载荷时抛出异常
     */
    private JobStockReleaseTaskDO buildRetryingTask() throws JsonProcessingException {
        JobStockReleaseTaskDO task = new JobStockReleaseTaskDO();
        task.setTaskId(1L);
        task.setEventId("release-event-001");
        task.setEventType(StockEventConstants.ORDER_TIMEOUT_RELEASE);
        task.setBusinessKey("reservation-001");
        task.setPayloadJson(objectMapper.writeValueAsString(buildReleaseEvent()));
        task.setTaskStatus(JobConstants.STOCK_RELEASE_TASK_STATUS_RETRYING);
        task.setRetryCount(2);
        task.setNextRetryAt(LocalDateTime.of(2026, 6, 5, 18, 0, 0));
        return task;
    }

    /**
     * 构造库存释放事件。
     *
     * @return 用于补发的库存释放事件
     */
    private StockReleaseEvent buildReleaseEvent() {
        StockReleaseEvent event = new StockReleaseEvent();
        event.setEventId("release-event-001");
        event.setEventType(StockEventConstants.ORDER_TIMEOUT_RELEASE);
        event.setOccurredAt(Instant.parse("2026-06-05T13:00:00Z"));
        event.setRequestId("req-001");
        event.setIdempotencyKey("idem-001");
        event.setReservationId("reservation-001");
        event.setOrderId(20001L);
        event.setActivityId(1001L);
        event.setTicketId(501L);
        event.setUserId(10001L);
        event.setQuantity(1);
        event.setReason(JobConstants.STOCK_RELEASE_REASON_ORDER_TIMEOUT);
        event.setSource(JobConstants.SOURCE_JOB_SERVICE);
        return event;
    }
}

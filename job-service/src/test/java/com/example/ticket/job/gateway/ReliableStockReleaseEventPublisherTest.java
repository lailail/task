package com.example.ticket.job.gateway;

import com.example.ticket.common.event.stock.StockEventConstants;
import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.job.domain.JobStockReleaseTaskDO;
import com.example.ticket.job.gateway.impl.ReliableStockReleaseEventPublisher;
import com.example.ticket.job.gateway.impl.MybatisStockReleaseTaskStore;
import com.example.ticket.job.mapper.JobStockReleaseTaskMapper;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * 可靠库存释放事件发布器单元测试。
 * 用于固定“先登记补偿事实，再尝试发送；发送失败时保留可补发记录”的发布约束。
 */
@ExtendWith(MockitoExtension.class)
class ReliableStockReleaseEventPublisherTest {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private JobStockReleaseTaskMapper stockReleaseTaskMapper;

    @Mock
    private StockReleaseMessageSender stockReleaseMessageSender;

    private ReliableStockReleaseEventPublisher reliableStockReleaseEventPublisher;

    /**
     * 构造被测可靠发布器。
     * 当前测试通过 mock 持久化层与 MQ 发送层，隔离补偿记录编排本身。
     */
    @BeforeEach
    void setUp() {
        reliableStockReleaseEventPublisher = new ReliableStockReleaseEventPublisher(
                new MybatisStockReleaseTaskStore(stockReleaseTaskMapper),
                stockReleaseMessageSender,
                objectMapper,
                30,
                20
        );
    }

    /**
     * 首次发布成功时，应先写入补偿记录，再把状态收敛为已发送。
     *
     * @throws JsonProcessingException 当断言载荷序列化结果时抛出异常
     */
    @Test
    void should_create_task_and_mark_sent_when_publish_succeeds() throws JsonProcessingException {
        StockReleaseEvent event = buildReleaseEvent();

        reliableStockReleaseEventPublisher.publish(event);

        ArgumentCaptor<JobStockReleaseTaskDO> insertCaptor = ArgumentCaptor.forClass(JobStockReleaseTaskDO.class);
        ArgumentCaptor<JobStockReleaseTaskDO> updateCaptor = ArgumentCaptor.forClass(JobStockReleaseTaskDO.class);
        verify(stockReleaseTaskMapper).insert(insertCaptor.capture());
        verify(stockReleaseTaskMapper).updateById(updateCaptor.capture());

        JobStockReleaseTaskDO insertedTask = insertCaptor.getValue();
        JobStockReleaseTaskDO updatedTask = updateCaptor.getValue();
        assertEquals("release-event-001", insertedTask.getEventId());
        assertEquals(StockEventConstants.ORDER_TIMEOUT_RELEASE, insertedTask.getEventType());
        assertEquals(JobConstants.STOCK_RELEASE_TASK_STATUS_PENDING, insertedTask.getTaskStatus());
        assertNotNull(insertedTask.getNextRetryAt());
        assertEquals(JobConstants.STOCK_RELEASE_TASK_STATUS_SENT, updatedTask.getTaskStatus());
        assertNotNull(updatedTask.getLastSentAt());
        StockReleaseEvent restoredEvent = objectMapper.readValue(insertedTask.getPayloadJson(), StockReleaseEvent.class);
        assertEquals("reservation-001", restoredEvent.getReservationId());
    }

    /**
     * 首次发布失败时，应保留待补发记录并记录最近错误，避免事件静默丢失。
     */
    @Test
    void should_mark_task_retrying_when_publish_fails() {
        StockReleaseEvent event = buildReleaseEvent();
        doThrow(new IllegalStateException("mq unavailable"))
                .when(stockReleaseMessageSender)
                .send(event);

        reliableStockReleaseEventPublisher.publish(event);

        ArgumentCaptor<JobStockReleaseTaskDO> updateCaptor = ArgumentCaptor.forClass(JobStockReleaseTaskDO.class);
        verify(stockReleaseTaskMapper).insert(any(JobStockReleaseTaskDO.class));
        verify(stockReleaseTaskMapper).updateById(updateCaptor.capture());
        assertEquals(JobConstants.STOCK_RELEASE_TASK_STATUS_RETRYING, updateCaptor.getValue().getTaskStatus());
        assertEquals(1, updateCaptor.getValue().getRetryCount());
        assertEquals("mq unavailable", updateCaptor.getValue().getLastErrorMessage());
        assertNotNull(updateCaptor.getValue().getNextRetryAt());
    }

    /**
     * 构造库存释放事件。
     *
     * @return 用于测试的库存释放事件
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

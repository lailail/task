package com.example.ticket.order.consumer;

import com.example.ticket.common.event.payment.PaymentResultEvent;
import com.example.ticket.order.service.OrderPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 支付结果事件消费者。
 * 用于承接支付域回推的支付结果，并把状态流转细节委托给订单服务层。
 */
@Component
public class PaymentResultEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(PaymentResultEventConsumer.class);

    private final OrderPaymentService orderPaymentService;

    /**
     * 构造支付结果事件消费者。
     *
     * @param orderPaymentService 订单支付结果处理服务
     */
    public PaymentResultEventConsumer(OrderPaymentService orderPaymentService) {
        this.orderPaymentService = orderPaymentService;
    }

    /**
     * 消费支付结果事件。
     *
     * @param event 支付结果事件
     */
    @RabbitListener(queues = "${ticket.order.mq.payment-result-queue}")
    public void consume(PaymentResultEvent event) {
        log.info(
                "收到支付结果事件，eventId={}, paymentRequestId={}, orderId={}, requestId={}, eventType={}",
                event.getEventId(),
                event.getPaymentRequestId(),
                event.getOrderId(),
                event.getRequestId(),
                event.getEventType()
        );
        orderPaymentService.handlePaymentResult(event);
        log.info("支付结果事件处理完成，eventId={}, paymentRequestId={}, orderId={}", event.getEventId(), event.getPaymentRequestId(), event.getOrderId());
    }
}

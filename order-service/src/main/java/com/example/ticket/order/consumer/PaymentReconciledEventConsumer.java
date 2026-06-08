package com.example.ticket.order.consumer;

import com.example.ticket.common.event.payment.PaymentReconciledEvent;
import com.example.ticket.order.service.OrderPaymentService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 支付收敛事件消费者。
 * 用于承接支付域确认收敛后的正式事件，并委托订单服务推进完成态。
 */
@Component
public class PaymentReconciledEventConsumer {
    private final OrderPaymentService orderPaymentService;

    /**
     * 构造支付收敛事件消费者。
     *
     * @param orderPaymentService 订单支付处理服务
     */
    public PaymentReconciledEventConsumer(OrderPaymentService orderPaymentService) {
        this.orderPaymentService = orderPaymentService;
    }

    /**
     * 消费支付收敛事件。
     *
     * @param event 支付收敛事件
     */
    @RabbitListener(queues = "${ticket.order.mq.payment-reconciled-queue}")
    public void consume(PaymentReconciledEvent event) {
        orderPaymentService.handlePaymentReconciled(event);
    }
}

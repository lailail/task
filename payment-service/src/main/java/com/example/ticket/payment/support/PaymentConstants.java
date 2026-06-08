package com.example.ticket.payment.support;

import com.example.ticket.common.constant.OrderStatusConstants;
import com.example.ticket.common.event.payment.PaymentEventConstants;
import com.example.ticket.common.reliable.ReliableMessageTaskStatus;

/**
 * 支付模块常量。
 * 用于集中维护支付记录状态、对账状态和跨域状态映射，避免业务字面量散落。
 */
public final class PaymentConstants {
    public static final String PAYMENT_STATUS_SUCCESS = "SUCCESS";
    public static final String PAYMENT_STATUS_FAILED = "FAILED";
    public static final String PAYMENT_STATUS_EXPIRED = "EXPIRED";

    public static final String RECONCILE_STATUS_PENDING = "PENDING";
    public static final String RECONCILE_STATUS_PROCESSING = "PROCESSING";
    public static final String RECONCILE_STATUS_DONE = "DONE";
    public static final String PAYMENT_RESULT_TASK_STATUS_PENDING = ReliableMessageTaskStatus.PENDING;
    public static final String PAYMENT_RESULT_TASK_STATUS_RETRYING = ReliableMessageTaskStatus.RETRYING;
    public static final String PAYMENT_RESULT_TASK_STATUS_SENT = ReliableMessageTaskStatus.SENT;
    public static final String PAYMENT_RESULT_TASK_STATUS_EXHAUSTED = ReliableMessageTaskStatus.EXHAUSTED;
    public static final String PAYMENT_RECONCILED_TASK_STATUS_PENDING = ReliableMessageTaskStatus.PENDING;
    public static final String PAYMENT_RECONCILED_TASK_STATUS_RETRYING = ReliableMessageTaskStatus.RETRYING;
    public static final String PAYMENT_RECONCILED_TASK_STATUS_SENT = ReliableMessageTaskStatus.SENT;
    public static final String PAYMENT_RECONCILED_TASK_STATUS_EXHAUSTED = ReliableMessageTaskStatus.EXHAUSTED;
    public static final String RECONCILE_ISSUE_STATUS_OPEN = "OPEN";
    public static final String RECONCILE_ISSUE_STATUS_RESOLVED = "RESOLVED";
    public static final String RECONCILE_ISSUE_TYPE_ORDER_STATUS_MISMATCH = "ORDER_STATUS_MISMATCH";
    public static final String RECONCILE_ORDER_STATUS_NOT_FOUND = "ORDER_NOT_FOUND";

    public static final String PAYMENT_SOURCE_PAYMENT_SERVICE = PaymentEventConstants.SOURCE_PAYMENT_SERVICE;

    /**
     * 禁止实例化常量类。
     */
    private PaymentConstants() {
    }

    /**
     * 把支付事件类型映射为支付记录状态。
     *
     * @param eventType 支付事件类型
     * @return 支付记录状态
     */
    public static String mapEventTypeToPaymentStatus(String eventType) {
        if (PaymentEventConstants.PAYMENT_SUCCEEDED.equals(eventType)) {
            return PAYMENT_STATUS_SUCCESS;
        }
        if (PaymentEventConstants.PAYMENT_FAILED.equals(eventType)) {
            return PAYMENT_STATUS_FAILED;
        }
        if (PaymentEventConstants.PAYMENT_EXPIRED.equals(eventType)) {
            return PAYMENT_STATUS_EXPIRED;
        }
        throw new IllegalArgumentException("unsupported payment event type: " + eventType);
    }

    /**
     * 判断当前订单状态是否已经和支付事实收敛。
     *
     * @param paymentStatus 支付记录状态
     * @param orderStatus 订单状态
     * @return 是否已收敛
     */
    public static boolean isOrderConverged(String paymentStatus, String orderStatus) {
        if (PAYMENT_STATUS_SUCCESS.equals(paymentStatus)) {
            return OrderStatusConstants.PAID.equals(orderStatus)
                    || OrderStatusConstants.COMPLETED.equals(orderStatus);
        }
        if (PAYMENT_STATUS_FAILED.equals(paymentStatus) || PAYMENT_STATUS_EXPIRED.equals(paymentStatus)) {
            return OrderStatusConstants.CANCELLED.equals(orderStatus);
        }
        return false;
    }
}

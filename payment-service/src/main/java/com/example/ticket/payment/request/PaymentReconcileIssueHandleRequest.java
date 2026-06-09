package com.example.ticket.payment.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 支付对账异常人工处置请求对象。
 * 用于承接人工重试、人工忽略和人工解决时需要记录的最小治理信息。
 */
public class PaymentReconcileIssueHandleRequest {
    @NotBlank
    private String operator;
    private String note;

    /**
     * 获取操作人。
     *
     * @return 操作人
     */
    public String getOperator() {
        return operator;
    }

    /**
     * 设置操作人。
     *
     * @param operator 操作人
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    /**
     * 获取备注。
     *
     * @return 备注
     */
    public String getNote() {
        return note;
    }

    /**
     * 设置备注。
     *
     * @param note 备注
     */
    public void setNote(String note) {
        this.note = note;
    }
}

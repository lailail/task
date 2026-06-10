package com.example.ticket.payment.controller;

import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;
import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.payment.service.PaymentTaskQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付域补偿任务内部查询控制器。
 * 用于向后台治理页暴露支付域补偿任务查询接口，不承载支付对账异常人工治理逻辑。
 */
@Validated
@RestController
@RequestMapping("/api/v1/internal")
public class PaymentTaskInternalController {
    private static final Logger log = LoggerFactory.getLogger(PaymentTaskInternalController.class);

    private final PaymentTaskQueryService paymentTaskQueryService;

    /**
     * 构造支付域补偿任务内部查询控制器。
     *
     * @param paymentTaskQueryService 支付域补偿任务查询服务
     */
    public PaymentTaskInternalController(PaymentTaskQueryService paymentTaskQueryService) {
        this.paymentTaskQueryService = paymentTaskQueryService;
    }

    /**
     * 分页查询支付结果补偿任务。
     *
     * @param request 查询条件与分页参数
     * @return 统一补偿任务分页结果
     */
    @GetMapping("/payment-result-tasks")
    public ApiResponse<ReliableMessageTaskPageResponse> queryPaymentResultTasks(ReliableMessageTaskQueryRequest request) {
        log.info(
                "收到支付结果补偿任务分页查询请求，taskStatus={}, businessKey={}, eventKey={}, current={}, pageSize={}",
                request.getTaskStatus(),
                request.getBusinessKey(),
                request.getEventKey(),
                request.getCurrent(),
                request.getPageSize()
        );
        ReliableMessageTaskPageResponse response = paymentTaskQueryService.queryPaymentResultTasks(request);
        log.info(
                "支付结果补偿任务分页查询完成，current={}, pageSize={}, total={}, currentPageCount={}",
                response.getCurrent(),
                response.getPageSize(),
                response.getTotal(),
                response.getRecords().size()
        );
        return ApiResponse.success(response);
    }

    /**
     * 分页查询支付收敛补偿任务。
     *
     * @param request 查询条件与分页参数
     * @return 统一补偿任务分页结果
     */
    @GetMapping("/payment-reconciled-tasks")
    public ApiResponse<ReliableMessageTaskPageResponse> queryPaymentReconciledTasks(ReliableMessageTaskQueryRequest request) {
        log.info(
                "收到支付收敛补偿任务分页查询请求，taskStatus={}, businessKey={}, eventKey={}, current={}, pageSize={}",
                request.getTaskStatus(),
                request.getBusinessKey(),
                request.getEventKey(),
                request.getCurrent(),
                request.getPageSize()
        );
        ReliableMessageTaskPageResponse response = paymentTaskQueryService.queryPaymentReconciledTasks(request);
        log.info(
                "支付收敛补偿任务分页查询完成，current={}, pageSize={}, total={}, currentPageCount={}",
                response.getCurrent(),
                response.getPageSize(),
                response.getTotal(),
                response.getRecords().size()
        );
        return ApiResponse.success(response);
    }
}

package com.example.ticket.order.controller;

import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;
import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.order.service.OrderTaskQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单域补偿任务内部查询控制器。
 * 用于向后台治理页暴露订单域补偿任务查询接口，不承载订单状态流转和人工治理逻辑。
 */
@Validated
@RestController
@RequestMapping("/api/v1/internal")
public class OrderTaskInternalController {
    private static final Logger log = LoggerFactory.getLogger(OrderTaskInternalController.class);

    private final OrderTaskQueryService orderTaskQueryService;

    /**
     * 构造订单域补偿任务内部查询控制器。
     *
     * @param orderTaskQueryService 订单域补偿任务查询服务
     */
    public OrderTaskInternalController(OrderTaskQueryService orderTaskQueryService) {
        this.orderTaskQueryService = orderTaskQueryService;
    }

    /**
     * 分页查询下单结果补偿任务。
     *
     * @param request 查询条件与分页参数
     * @return 统一补偿任务分页结果
     */
    @GetMapping("/order-result-tasks")
    public ApiResponse<ReliableMessageTaskPageResponse> queryOrderResultTasks(ReliableMessageTaskQueryRequest request) {
        log.info(
                "收到下单结果补偿任务分页查询请求，taskStatus={}, businessKey={}, eventKey={}, current={}, pageSize={}",
                request.getTaskStatus(),
                request.getBusinessKey(),
                request.getEventKey(),
                request.getCurrent(),
                request.getPageSize()
        );
        ReliableMessageTaskPageResponse response = orderTaskQueryService.queryOrderResultTasks(request);
        log.info(
                "下单结果补偿任务分页查询完成，current={}, pageSize={}, total={}, currentPageCount={}",
                response.getCurrent(),
                response.getPageSize(),
                response.getTotal(),
                response.getRecords().size()
        );
        return ApiResponse.success(response);
    }

    /**
     * 分页查询订单完成补偿任务。
     *
     * @param request 查询条件与分页参数
     * @return 统一补偿任务分页结果
     */
    @GetMapping("/order-complete-tasks")
    public ApiResponse<ReliableMessageTaskPageResponse> queryOrderCompleteTasks(ReliableMessageTaskQueryRequest request) {
        log.info(
                "收到订单完成补偿任务分页查询请求，taskStatus={}, businessKey={}, eventKey={}, current={}, pageSize={}",
                request.getTaskStatus(),
                request.getBusinessKey(),
                request.getEventKey(),
                request.getCurrent(),
                request.getPageSize()
        );
        ReliableMessageTaskPageResponse response = orderTaskQueryService.queryOrderCompleteTasks(request);
        log.info(
                "订单完成补偿任务分页查询完成，current={}, pageSize={}, total={}, currentPageCount={}",
                response.getCurrent(),
                response.getPageSize(),
                response.getTotal(),
                response.getRecords().size()
        );
        return ApiResponse.success(response);
    }
}

package com.example.ticket.seckill.controller;

import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;
import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.seckill.service.OrderCreateTaskQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 下单请求补偿任务内部查询控制器。
 * 用于向后台治理页暴露抢票侧补偿任务查询接口，不承载任何补发或状态迁移逻辑。
 */
@Validated
@RestController
@RequestMapping("/api/v1/internal/order-create-tasks")
public class OrderCreateTaskInternalController {
    private static final Logger log = LoggerFactory.getLogger(OrderCreateTaskInternalController.class);

    private final OrderCreateTaskQueryService orderCreateTaskQueryService;

    /**
     * 构造下单请求补偿任务内部查询控制器。
     *
     * @param orderCreateTaskQueryService 下单请求补偿任务查询服务
     */
    public OrderCreateTaskInternalController(OrderCreateTaskQueryService orderCreateTaskQueryService) {
        this.orderCreateTaskQueryService = orderCreateTaskQueryService;
    }

    /**
     * 分页查询下单请求补偿任务。
     *
     * @param request 查询条件与分页参数
     * @return 统一补偿任务分页结果
     */
    @GetMapping
    public ApiResponse<ReliableMessageTaskPageResponse> queryOrderCreateTasks(ReliableMessageTaskQueryRequest request) {
        log.info(
                "收到下单请求补偿任务分页查询请求，taskStatus={}, businessKey={}, eventKey={}, current={}, pageSize={}",
                request.getTaskStatus(),
                request.getBusinessKey(),
                request.getEventKey(),
                request.getCurrent(),
                request.getPageSize()
        );
        ReliableMessageTaskPageResponse response = orderCreateTaskQueryService.queryOrderCreateTasks(request);
        log.info(
                "下单请求补偿任务分页查询完成，current={}, pageSize={}, total={}, currentPageCount={}",
                response.getCurrent(),
                response.getPageSize(),
                response.getTotal(),
                response.getRecords().size()
        );
        return ApiResponse.success(response);
    }
}

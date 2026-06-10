package com.example.ticket.job.controller;

import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;
import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.job.service.StockReleaseTaskQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存释放补偿任务内部查询控制器。
 * 用于向后台治理页暴露库存释放补偿任务查询接口，不承载补发、回补和状态迁移逻辑。
 */
@Validated
@RestController
@RequestMapping("/api/v1/internal/stock-release-tasks")
public class StockReleaseTaskInternalController {
    private static final Logger log = LoggerFactory.getLogger(StockReleaseTaskInternalController.class);

    private final StockReleaseTaskQueryService stockReleaseTaskQueryService;

    /**
     * 构造库存释放补偿任务内部查询控制器。
     *
     * @param stockReleaseTaskQueryService 库存释放补偿任务查询服务
     */
    public StockReleaseTaskInternalController(StockReleaseTaskQueryService stockReleaseTaskQueryService) {
        this.stockReleaseTaskQueryService = stockReleaseTaskQueryService;
    }

    /**
     * 分页查询库存释放补偿任务。
     *
     * @param request 查询条件与分页参数
     * @return 统一补偿任务分页结果
     */
    @GetMapping
    public ApiResponse<ReliableMessageTaskPageResponse> queryStockReleaseTasks(ReliableMessageTaskQueryRequest request) {
        log.info(
                "收到库存释放补偿任务分页查询请求，taskStatus={}, businessKey={}, eventKey={}, current={}, pageSize={}",
                request.getTaskStatus(),
                request.getBusinessKey(),
                request.getEventKey(),
                request.getCurrent(),
                request.getPageSize()
        );
        ReliableMessageTaskPageResponse response = stockReleaseTaskQueryService.queryStockReleaseTasks(request);
        log.info(
                "库存释放补偿任务分页查询完成，current={}, pageSize={}, total={}, currentPageCount={}",
                response.getCurrent(),
                response.getPageSize(),
                response.getTotal(),
                response.getRecords().size()
        );
        return ApiResponse.success(response);
    }
}

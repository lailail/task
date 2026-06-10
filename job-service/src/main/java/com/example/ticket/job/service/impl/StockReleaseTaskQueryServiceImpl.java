package com.example.ticket.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.reliable.query.ReliableMessageTaskPageResponse;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQueryRequest;
import com.example.ticket.common.reliable.query.ReliableMessageTaskQuerySupport;
import com.example.ticket.common.reliable.query.ReliableMessageTaskTypes;
import com.example.ticket.job.domain.JobStockReleaseTaskDO;
import com.example.ticket.job.mapper.JobStockReleaseTaskMapper;
import com.example.ticket.job.service.StockReleaseTaskQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 库存释放补偿任务查询服务实现。
 * 用于收口库存释放补偿任务的筛选、分页和统一响应映射逻辑。
 */
@Service
public class StockReleaseTaskQueryServiceImpl implements StockReleaseTaskQueryService {
    private static final Logger log = LoggerFactory.getLogger(StockReleaseTaskQueryServiceImpl.class);

    private final JobStockReleaseTaskMapper stockReleaseTaskMapper;

    /**
     * 构造库存释放补偿任务查询服务。
     *
     * @param stockReleaseTaskMapper 库存释放补偿任务 Mapper
     */
    public StockReleaseTaskQueryServiceImpl(JobStockReleaseTaskMapper stockReleaseTaskMapper) {
        this.stockReleaseTaskMapper = stockReleaseTaskMapper;
    }

    /**
     * 分页查询库存释放补偿任务。
     *
     * @param request 查询条件与分页参数
     * @return 统一补偿任务分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public ReliableMessageTaskPageResponse queryStockReleaseTasks(ReliableMessageTaskQueryRequest request) {
        long current = ReliableMessageTaskQuerySupport.normalizeCurrent(request.getCurrent());
        long pageSize = ReliableMessageTaskQuerySupport.normalizePageSize(request.getPageSize());
        log.info(
                "开始查询库存释放补偿任务，taskStatus={}, businessKey={}, eventKey={}, current={}, pageSize={}",
                request.getTaskStatus(),
                request.getBusinessKey(),
                request.getEventKey(),
                current,
                pageSize
        );
        Page<JobStockReleaseTaskDO> page = stockReleaseTaskMapper.selectPage(
                new Page<>(current, pageSize),
                buildQueryWrapper(request)
        );
        ReliableMessageTaskPageResponse response = ReliableMessageTaskQuerySupport.toPageResponse(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                ReliableMessageTaskTypes.STOCK_RELEASE
        );
        log.info(
                "库存释放补偿任务查询完成，current={}, pageSize={}, total={}, currentPageCount={}",
                response.getCurrent(),
                response.getPageSize(),
                response.getTotal(),
                response.getRecords().size()
        );
        return response;
    }

    /**
     * 构造库存释放补偿任务查询条件。
     * 这里通过统一任务视图的 eventKey 映射到底层 eventId，避免查询层感知物理字段差异。
     *
     * @param request 查询请求
     * @return 查询条件
     */
    private LambdaQueryWrapper<JobStockReleaseTaskDO> buildQueryWrapper(ReliableMessageTaskQueryRequest request) {
        LambdaQueryWrapper<JobStockReleaseTaskDO> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getTaskStatus())) {
            queryWrapper.eq(JobStockReleaseTaskDO::getTaskStatus, request.getTaskStatus().trim());
        }
        if (StringUtils.hasText(request.getBusinessKey())) {
            queryWrapper.eq(JobStockReleaseTaskDO::getBusinessKey, request.getBusinessKey().trim());
        }
        if (StringUtils.hasText(request.getEventKey())) {
            queryWrapper.eq(JobStockReleaseTaskDO::getEventId, request.getEventKey().trim());
        }
        queryWrapper.orderByDesc(JobStockReleaseTaskDO::getNextRetryAt)
                .orderByDesc(JobStockReleaseTaskDO::getTaskId);
        return queryWrapper;
    }
}

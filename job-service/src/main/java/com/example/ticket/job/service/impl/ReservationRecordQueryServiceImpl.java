package com.example.ticket.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.job.domain.JobReservationRecordDO;
import com.example.ticket.job.mapper.JobReservationRecordMapper;
import com.example.ticket.job.request.ReservationRecordQueryRequest;
import com.example.ticket.job.response.ReservationRecordPageResponse;
import com.example.ticket.job.response.ReservationRecordResponse;
import com.example.ticket.job.service.ReservationRecordQueryService;
import com.example.ticket.job.support.JobConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 预扣记录查询服务实现。
 * 用于收口后台页的预扣事实查询逻辑，统一处理筛选条件、分页边界和响应映射。
 */
@Service
public class ReservationRecordQueryServiceImpl implements ReservationRecordQueryService {
    private static final Logger log = LoggerFactory.getLogger(ReservationRecordQueryServiceImpl.class);

    private final JobReservationRecordMapper reservationRecordMapper;

    /**
     * 构造预扣记录查询服务。
     *
     * @param reservationRecordMapper 预扣记录 Mapper
     */
    public ReservationRecordQueryServiceImpl(JobReservationRecordMapper reservationRecordMapper) {
        this.reservationRecordMapper = reservationRecordMapper;
    }

    /**
     * 分页查询预扣记录。
     * 这里只暴露后台治理页需要的字段，并统一约束分页参数，避免出现无界查询。
     *
     * @param request 查询条件和分页参数
     * @return 分页后的预扣记录视图
     */
    @Override
    @Transactional(readOnly = true)
    public ReservationRecordPageResponse queryReservationRecords(ReservationRecordQueryRequest request) {
        long current = normalizeCurrent(request.getCurrent());
        long pageSize = normalizePageSize(request.getPageSize());

        log.info(
                "开始查询预扣记录，reservationId={}, requestId={}, userId={}, activityId={}, ticketId={}, reservationStatus={}, current={}, pageSize={}",
                request.getReservationId(),
                request.getRequestId(),
                request.getUserId(),
                request.getActivityId(),
                request.getTicketId(),
                request.getReservationStatus(),
                current,
                pageSize
        );

        LambdaQueryWrapper<JobReservationRecordDO> queryWrapper = buildQueryWrapper(request);
        Page<JobReservationRecordDO> page = reservationRecordMapper.selectPage(
                new Page<>(current, pageSize),
                queryWrapper
        );

        ReservationRecordPageResponse response = new ReservationRecordPageResponse();
        response.setRecords(page.getRecords().stream().map(this::mapResponse).toList());
        response.setTotal(page.getTotal());
        response.setCurrent(page.getCurrent());
        response.setPageSize(page.getSize());

        log.info(
                "预扣记录查询完成，current={}, pageSize={}, total={}, currentPageCount={}",
                response.getCurrent(),
                response.getPageSize(),
                response.getTotal(),
                response.getRecords().size()
        );
        return response;
    }

    /**
     * 构造预扣记录查询条件。
     * 只拼接显式传入的过滤项，避免把空条件误拼成错误查询。
     *
     * @param request 查询请求
     * @return MyBatis-Plus 查询条件
     */
    private LambdaQueryWrapper<JobReservationRecordDO> buildQueryWrapper(ReservationRecordQueryRequest request) {
        LambdaQueryWrapper<JobReservationRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getReservationId())) {
            queryWrapper.eq(JobReservationRecordDO::getReservationId, request.getReservationId().trim());
        }
        if (StringUtils.hasText(request.getRequestId())) {
            queryWrapper.eq(JobReservationRecordDO::getRequestId, request.getRequestId().trim());
        }
        if (request.getUserId() != null) {
            queryWrapper.eq(JobReservationRecordDO::getUserId, request.getUserId());
        }
        if (request.getActivityId() != null) {
            queryWrapper.eq(JobReservationRecordDO::getActivityId, request.getActivityId());
        }
        if (request.getTicketId() != null) {
            queryWrapper.eq(JobReservationRecordDO::getTicketId, request.getTicketId());
        }
        if (StringUtils.hasText(request.getReservationStatus())) {
            queryWrapper.eq(JobReservationRecordDO::getReservationStatus, request.getReservationStatus().trim());
        }
        queryWrapper.orderByDesc(JobReservationRecordDO::getExpireAt)
                .orderByDesc(JobReservationRecordDO::getReservationId);
        return queryWrapper;
    }

    /**
     * 把持久化对象映射为后台页响应对象。
     *
     * @param record 预扣记录持久化对象
     * @return 预扣记录响应对象
     */
    private ReservationRecordResponse mapResponse(JobReservationRecordDO record) {
        ReservationRecordResponse response = new ReservationRecordResponse();
        response.setReservationId(record.getReservationId());
        response.setRequestId(record.getRequestId());
        response.setUserId(record.getUserId());
        response.setOrderId(record.getOrderId());
        response.setActivityId(record.getActivityId());
        response.setTicketId(record.getTicketId());
        response.setQuantity(record.getQuantity());
        response.setReservationStatus(record.getReservationStatus());
        response.setSource(record.getSource());
        response.setReason(record.getReason());
        response.setExpireAt(record.getExpireAt());
        response.setReleasedAt(record.getReleasedAt());
        return response;
    }

    /**
     * 规范化页码，避免非法页码导致查询语义漂移。
     *
     * @param current 原始页码
     * @return 合法页码
     */
    private long normalizeCurrent(Long current) {
        if (current == null || current < JobConstants.DEFAULT_PAGE_CURRENT) {
            return JobConstants.DEFAULT_PAGE_CURRENT;
        }
        return current;
    }

    /**
     * 规范化分页大小，避免后台误发大页触发无界查询风险。
     *
     * @param pageSize 原始分页大小
     * @return 合法分页大小
     */
    private long normalizePageSize(Long pageSize) {
        if (pageSize == null || pageSize < 1) {
            return JobConstants.DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, JobConstants.MAX_PAGE_SIZE);
    }
}

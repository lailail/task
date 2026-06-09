package com.example.ticket.ticket.controller;

import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.ticket.dto.ActivityDTO;
import com.example.ticket.ticket.response.ActivityDetailResponse;
import com.example.ticket.ticket.response.ActivitySummaryResponse;
import com.example.ticket.ticket.service.TicketQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 活动查询控制器。
 * 当前阶段只提供活动列表和活动详情查询，不承载库存预扣或抢票业务逻辑。
 */
@RestController
@RequestMapping("/api/v1/activities")
public class TicketController {
    private static final Logger log = LoggerFactory.getLogger(TicketController.class);

    private final TicketQueryService ticketQueryService;

    /**
     * 构造活动查询控制器。
     *
     * @param ticketQueryService 活动查询服务
     */
    public TicketController(TicketQueryService ticketQueryService) {
        this.ticketQueryService = ticketQueryService;
    }

    /**
     * 返回活动摘要列表，供活动页或列表页展示使用。
     */
    @GetMapping
    public ApiResponse<List<ActivitySummaryResponse>> listActivities() {
        log.info("收到活动列表查询请求");
        // 控制层只负责把领域查询结果转换成接口响应对象，避免把 DTO 直接暴露给外部调用方。
        List<ActivitySummaryResponse> data = ticketQueryService.listActivities().stream()
                .map(this::toSummaryResponse)
                .toList();
        log.info("活动列表查询完成，count={}", data.size());
        return ApiResponse.success(data);
    }

    /**
     * 返回指定活动的详情和票种信息。
     */
    @GetMapping("/{activityId}")
    public ApiResponse<ActivityDetailResponse> getActivityDetail(@PathVariable Long activityId) {
        log.info("收到活动详情查询请求，activityId={}", activityId);
        ActivityDetailResponse response = toDetailResponse(ticketQueryService.getActivityDetail(activityId));
        log.info(
                "活动详情查询完成，activityId={}, ticketItemCount={}",
                activityId,
                response.getTicketItems() == null ? 0 : response.getTicketItems().size()
        );
        return ApiResponse.success(response);
    }

    /**
     * 活动列表接口只返回摘要字段，避免一次性把不必要的详情字段和票种集合都下发给调用方。
     */
    private ActivitySummaryResponse toSummaryResponse(ActivityDTO activity) {
        ActivitySummaryResponse response = new ActivitySummaryResponse();
        response.setActivityId(activity.getActivityId());
        response.setActivityName(activity.getActivityName());
        response.setCity(activity.getCity());
        response.setVenueName(activity.getVenueName());
        response.setSaleStatus(activity.getSaleStatus());
        return response;
    }

    /**
     * 活动详情接口当前直接附带票种集合，后续如果字段继续增长，可再拆成更细粒度视图对象。
     */
    private ActivityDetailResponse toDetailResponse(ActivityDTO activity) {
        ActivityDetailResponse response = new ActivityDetailResponse();
        response.setActivityId(activity.getActivityId());
        response.setActivityName(activity.getActivityName());
        response.setCity(activity.getCity());
        response.setVenueName(activity.getVenueName());
        response.setSaleStatus(activity.getSaleStatus());
        response.setTicketItems(activity.getTicketItems());
        return response;
    }
}

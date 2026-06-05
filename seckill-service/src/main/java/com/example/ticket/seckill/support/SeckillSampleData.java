package com.example.ticket.seckill.support;

import com.example.ticket.seckill.dto.SeckillActivityDTO;

import java.util.List;

/**
 * 抢票服务演示活动数据。
 * 当前阶段仍是过渡实现，用于让抢票链路先具备本地可测能力，后续会替换为正式活动数据源。
 */
public final class SeckillSampleData {

    public static final Long CONCERT_ACTIVITY_ID = 1001L;
    public static final Long CONCERT_TICKET_ID = 501L;
    public static final Long DRAMA_ACTIVITY_ID = 1002L;
    public static final Long DRAMA_TICKET_ID = 503L;

    /**
     * 禁止实例化演示数据工具类。
     */
    private SeckillSampleData() {
    }

    /**
     * 构造当前阶段使用的演示抢票活动数据。
     *
     * @return 演示活动列表
     */
    public static List<SeckillActivityDTO> buildActivities() {
        return List.of(
                buildActivity(CONCERT_ACTIVITY_ID, CONCERT_TICKET_ID, "五月天上海演唱会", SeckillConstants.SALE_STATUS_ON_SALE, 800),
                buildActivity(DRAMA_ACTIVITY_ID, DRAMA_TICKET_ID, "国家大剧院话剧专场", SeckillConstants.SALE_STATUS_COMING_SOON, 120)
        );
    }

    /**
     * 构造单个演示活动。
     *
     * @param activityId 活动标识
     * @param ticketId 票种标识
     * @param activityName 活动名称
     * @param saleStatus 销售状态
     * @param availableStock 可售库存
     * @return 抢票活动对象
     */
    private static SeckillActivityDTO buildActivity(
            Long activityId,
            Long ticketId,
            String activityName,
            String saleStatus,
            Integer availableStock
    ) {
        SeckillActivityDTO activity = new SeckillActivityDTO();
        activity.setActivityId(activityId);
        activity.setTicketId(ticketId);
        activity.setActivityName(activityName);
        activity.setSaleStatus(saleStatus);
        activity.setAvailableStock(availableStock);
        return activity;
    }
}

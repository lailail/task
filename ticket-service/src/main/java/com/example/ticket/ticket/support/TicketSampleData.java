package com.example.ticket.ticket.support;

import com.example.ticket.ticket.dto.ActivityDTO;
import com.example.ticket.ticket.dto.TicketItemDTO;

import java.util.List;

/**
 * Phase 2 演示活动数据。
 * 当前阶段仍使用内存数据仓储来稳定查询接口和分层结构，后续会替换为数据库与缓存实现。
 */
public final class TicketSampleData {

    public static final Long CONCERT_ACTIVITY_ID = 1001L;
    public static final Long DRAMA_ACTIVITY_ID = 1002L;
    public static final Long STANDARD_TICKET_ID = 501L;
    public static final Long VIP_TICKET_ID = 502L;
    public static final Long DRAMA_TICKET_ID = 503L;

    public static final String SALE_STATUS_ON_SALE = "ON_SALE";
    public static final String SALE_STATUS_COMING_SOON = "COMING_SOON";

    public static final String CONCERT_ACTIVITY_NAME = "五月天上海演唱会";
    public static final String DRAMA_ACTIVITY_NAME = "国家大剧院话剧专场";
    public static final String SHANGHAI_CITY = "上海";
    public static final String BEIJING_CITY = "北京";
    public static final String SHANGHAI_STADIUM = "上海体育场";
    public static final String NATIONAL_CENTER_FOR_PERFORMING_ARTS = "国家大剧院";
    public static final String STANDARD_TICKET_NAME = "看台票";
    public static final String VIP_TICKET_NAME = "内场票";
    public static final String DRAMA_TICKET_NAME = "一层座位票";

    /**
     * 禁止实例化演示数据工具类。
     */
    private TicketSampleData() {
    }

    /**
     * 构造当前阶段使用的演示活动数据。
     *
     * @return 演示活动列表
     */
    public static List<ActivityDTO> buildActivities() {
        ActivityDTO concert = buildActivity(
                CONCERT_ACTIVITY_ID,
                CONCERT_ACTIVITY_NAME,
                SHANGHAI_CITY,
                SHANGHAI_STADIUM,
                SALE_STATUS_ON_SALE,
                List.of(
                        buildTicket(STANDARD_TICKET_ID, STANDARD_TICKET_NAME, 499, 800),
                        buildTicket(VIP_TICKET_ID, VIP_TICKET_NAME, 1299, 200)
                )
        );

        ActivityDTO drama = buildActivity(
                DRAMA_ACTIVITY_ID,
                DRAMA_ACTIVITY_NAME,
                BEIJING_CITY,
                NATIONAL_CENTER_FOR_PERFORMING_ARTS,
                SALE_STATUS_COMING_SOON,
                List.of(buildTicket(DRAMA_TICKET_ID, DRAMA_TICKET_NAME, 699, 120))
        );

        return List.of(concert, drama);
    }

    /**
     * 构造单个活动对象。
     *
     * @param activityId 活动标识
     * @param activityName 活动名称
     * @param city 城市
     * @param venueName 场馆名称
     * @param saleStatus 销售状态
     * @param ticketItems 票种列表
     * @return 活动对象
     */
    private static ActivityDTO buildActivity(
            Long activityId,
            String activityName,
            String city,
            String venueName,
            String saleStatus,
            List<TicketItemDTO> ticketItems
    ) {
        ActivityDTO activity = new ActivityDTO();
        activity.setActivityId(activityId);
        activity.setActivityName(activityName);
        activity.setCity(city);
        activity.setVenueName(venueName);
        activity.setSaleStatus(saleStatus);
        activity.setTicketItems(ticketItems);
        return activity;
    }

    /**
     * 构造单个票种对象。
     *
     * @param ticketId 票种标识
     * @param ticketName 票种名称
     * @param price 价格
     * @param availableStock 可售库存
     * @return 票种对象
     */
    private static TicketItemDTO buildTicket(Long ticketId, String ticketName, Integer price, Integer availableStock) {
        TicketItemDTO ticketItem = new TicketItemDTO();
        ticketItem.setTicketId(ticketId);
        ticketItem.setTicketName(ticketName);
        ticketItem.setPrice(price);
        ticketItem.setAvailableStock(availableStock);
        return ticketItem;
    }
}

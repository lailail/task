package com.example.ticket.ticket.response;

import com.example.ticket.ticket.dto.TicketItemDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 活动详情响应对象。
 * 在活动摘要字段基础上增加票种列表，供详情页使用。
 */
public class ActivityDetailResponse extends ActivitySummaryResponse {
    private List<TicketItemDTO> ticketItems = new ArrayList<>();

    /**
     * 获取票种列表。
     *
     * @return 票种列表
     */
    public List<TicketItemDTO> getTicketItems() {
        return ticketItems;
    }

    /**
     * 设置票种列表。
     *
     * @param ticketItems 票种列表
     */
    public void setTicketItems(List<TicketItemDTO> ticketItems) {
        this.ticketItems = ticketItems;
    }
}

package com.example.ticket.ticket.dto;

/**
 * 票种传输对象。
 * 用于描述单个票种的基础信息和可售库存。
 */
public class TicketItemDTO {
    private Long ticketId;
    private String ticketName;
    private Integer price;
    private Integer availableStock;

    /**
     * 获取票种标识。
     *
     * @return 票种标识
     */
    public Long getTicketId() {
        return ticketId;
    }

    /**
     * 设置票种标识。
     *
     * @param ticketId 票种标识
     */
    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    /**
     * 获取票种名称。
     *
     * @return 票种名称
     */
    public String getTicketName() {
        return ticketName;
    }

    /**
     * 设置票种名称。
     *
     * @param ticketName 票种名称
     */
    public void setTicketName(String ticketName) {
        this.ticketName = ticketName;
    }

    /**
     * 获取票价。
     *
     * @return 票价
     */
    public Integer getPrice() {
        return price;
    }

    /**
     * 设置票价。
     *
     * @param price 票价
     */
    public void setPrice(Integer price) {
        this.price = price;
    }

    /**
     * 获取可售库存。
     *
     * @return 可售库存
     */
    public Integer getAvailableStock() {
        return availableStock;
    }

    /**
     * 设置可售库存。
     *
     * @param availableStock 可售库存
     */
    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }
}

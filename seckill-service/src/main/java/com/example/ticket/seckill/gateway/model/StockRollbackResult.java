package com.example.ticket.seckill.gateway.model;

/**
 * 库存预扣回滚结果对象。
 * 用于承接 Redis 预扣补偿脚本的返回结果，避免服务层直接依赖 Lua 返回字面量。
 */
public class StockRollbackResult {
    private final String resultCode;

    /**
     * 构造库存预扣回滚结果对象。
     *
     * @param resultCode 回滚结果码
     */
    private StockRollbackResult(String resultCode) {
        this.resultCode = resultCode;
    }

    /**
     * 构造回滚成功结果。
     *
     * @param resultCode 回滚结果码
     * @return 回滚结果
     */
    public static StockRollbackResult of(String resultCode) {
        return new StockRollbackResult(resultCode);
    }

    /**
     * 获取回滚结果码。
     *
     * @return 回滚结果码
     */
    public String getResultCode() {
        return resultCode;
    }
}

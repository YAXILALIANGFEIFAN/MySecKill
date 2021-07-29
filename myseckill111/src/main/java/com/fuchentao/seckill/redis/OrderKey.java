package com.fuchentao.seckill.redis;

public class OrderKey extends BasePrefix{

    private static final int orderKeyExpireSeconds = 60 * 5;

    public OrderKey(int expireSeconds, String prefix) {
        super(orderKeyExpireSeconds, prefix);
    }

    public static OrderKey getSeckillOrderBySeckillUserGoodsId =
            new OrderKey(orderKeyExpireSeconds, "SeckillOrderSeckillUserGoodsId");
}

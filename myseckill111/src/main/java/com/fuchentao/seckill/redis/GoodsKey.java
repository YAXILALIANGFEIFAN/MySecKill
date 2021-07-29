package com.fuchentao.seckill.redis;


public class GoodsKey extends BasePrefix{

    //设置GoodsKey的有效期，单位秒
    private static final int goodsKeyExpireSeconds = 60 * 10;

    private GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static GoodsKey getGoodsList =
            new GoodsKey(goodsKeyExpireSeconds, "goodsList");

    public static GoodsKey getGoodsDetail =
            new GoodsKey(goodsKeyExpireSeconds, "goodsDetail");

    public static GoodsKey getSeckillGoodsStockCount =
            new GoodsKey(goodsKeyExpireSeconds, "seckillGoodsStockCount");
}

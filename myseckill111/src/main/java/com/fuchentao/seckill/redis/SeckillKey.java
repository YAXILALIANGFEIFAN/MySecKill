package com.fuchentao.seckill.redis;


public class SeckillKey extends BasePrefix{

    //设置SeckillKey的有效期，单位秒
    private static final int seckillKeyExpireSeconds = 60 * 5;

    private SeckillKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static SeckillKey isSeckillGoodsOver =
            new SeckillKey(seckillKeyExpireSeconds, "seckillGoodsOver");

    public static SeckillKey getSeckillPath =
            new SeckillKey(seckillKeyExpireSeconds, "seckillPath");

}

package com.fuchentao.seckill.redis;


public class SeckillUserKey extends BasePrefix{

    //设置token的有效期，单位秒
    private static final int tokenExpireSeconds = 60 * 5;

    private SeckillUserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    //设置秒杀用户的token
    public static SeckillUserKey token =
            new SeckillUserKey(tokenExpireSeconds, "token");

    public static SeckillUserKey getSeckillUser =
            new SeckillUserKey(tokenExpireSeconds, "id");
}

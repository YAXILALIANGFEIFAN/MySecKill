package com.fuchentao.seckill.access;

import com.fuchentao.seckill.domain.SeckillUser;

public class SeckillUserContext {

    private static ThreadLocal<SeckillUser> seckillUserHolder = new ThreadLocal<>();

    public static void setSeckillUser(SeckillUser seckillUser) {
        seckillUserHolder.set(seckillUser);
    }

    public static SeckillUser getSeckillUser() {
        return seckillUserHolder.get();
    }
}

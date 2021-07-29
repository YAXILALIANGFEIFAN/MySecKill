package com.fuchentao.seckill.rabbitmq;

import com.fuchentao.seckill.domain.SeckillUser;

//把用户的秒杀请求封装成一个类  里面包含秒杀用户的信息和商品信id
public class SeckillMessage {

    private SeckillUser seckillUser;
    private long goodsId;

    public SeckillUser getSeckillUser() {
        return seckillUser;
    }

    public void setSeckillUser(SeckillUser seckillUser) {
        this.seckillUser = seckillUser;
    }

    public long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(long goodsId) {
        this.goodsId = goodsId;
    }
}

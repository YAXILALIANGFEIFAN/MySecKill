package com.fuchentao.seckill.vo;

/*
GoodsVo 是Goods类和SeckillGoods类的并集
OrderInfo 对应MySQL数据库中的order_info 表
 */
import com.fuchentao.seckill.domain.OrderInfo;

public class OrderDetailVo {

    private GoodsVo goodsVo;
    private OrderInfo orderInfo;

    public GoodsVo getGoodsVo() {
        return goodsVo;
    }

    public void setGoodsVo(GoodsVo goodsVo) {
        this.goodsVo = goodsVo;
    }

    public OrderInfo getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(OrderInfo orderInfo) {
        this.orderInfo = orderInfo;
    }
}

package com.fuchentao.seckill.service;

import com.fuchentao.seckill.dao.GoodsDao;
import com.fuchentao.seckill.dao.OrderDao;
import com.fuchentao.seckill.domain.OrderInfo;
import com.fuchentao.seckill.domain.SeckillOrder;
import com.fuchentao.seckill.domain.SeckillUser;
import com.fuchentao.seckill.redis.OrderKey;
import com.fuchentao.seckill.redis.RedisService;
import com.fuchentao.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class OrderService {

    @Autowired(required = false)
    OrderDao orderDao;

    @Autowired
    RedisService redisService;

    //根据用户信息和商品id 获取秒杀订单 如果有秒杀订单，写进redis并且返回
    public SeckillOrder getSeckillOrderBySeckillUserGoodsId
                                            (Long seckillUserId, long goodsId) {

        SeckillOrder seckillOrder = redisService.get
                (OrderKey.getSeckillOrderBySeckillUserGoodsId,
                        "" + seckillUserId + "_" + goodsId,
                                SeckillOrder.class);

        if (seckillOrder != null) {
            return seckillOrder;
        }
        return orderDao.getSeckillOrderBySeckillUserGoodsId(seckillUserId, goodsId);
    }

    //根据orderId查找order_info表中的数据
    public OrderInfo getOrderInfoById(long orderId) {
        return orderDao.getOrderInfoById(orderId);
    }

    //在 order_info 和 seckill_order 中各创建一条记录 这是一个原子操作
    @Transactional
    public OrderInfo createOrderInfoSeckillOrder
                                (SeckillUser seckillUser, GoodsVo goodsVo) {

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goodsVo.getId());
        orderInfo.setGoodsName(goodsVo.getGoodsName());
        orderInfo.setGoodsPrice(goodsVo.getSeckillPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);//创建订单而未支付
        orderInfo.setUserId(seckillUser.getId());
        orderDao.insertOrderInfo(orderInfo);

        //对seckill_order添加唯一索引，防止单个用户同时发起多个秒杀
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setGoodsId(goodsVo.getId());
        //订单号 orderInfo.getId()是dao层插入操作返回的数据
        seckillOrder.setOrderId(orderInfo.getId());
        seckillOrder.setUserId(seckillUser.getId());
        orderDao.insertSeckillOrder(seckillOrder);

        redisService.set(OrderKey.getSeckillOrderBySeckillUserGoodsId,
                         "" + seckillUser.getId() + "_" + goodsVo.getId(),
                          seckillOrder);
        return orderInfo;
    }

}

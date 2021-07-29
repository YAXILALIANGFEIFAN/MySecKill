package com.fuchentao.seckill.rabbitmq;

import com.fuchentao.seckill.domain.OrderInfo;
import com.fuchentao.seckill.domain.SeckillOrder;
import com.fuchentao.seckill.domain.SeckillUser;
import com.fuchentao.seckill.redis.RedisService;
import com.fuchentao.seckill.result.CodeMsg;
import com.fuchentao.seckill.result.Result;
import com.fuchentao.seckill.service.GoodsService;
import com.fuchentao.seckill.service.OrderService;
import com.fuchentao.seckill.service.SeckillService;
import com.fuchentao.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SeckillService seckillService;

    @RabbitListener(queues = MQConfig.seckillQueue)
    public void receiveSeckillQueue(String message) {
        log.info("receice message:" + message);
        //将消息队列中的消息反序列化成SeckillMessage格式，并且获取秒杀用户和商品信息
        SeckillMessage seckillMessage =
                RedisService.stringToBean(message, SeckillMessage.class);
        SeckillUser seckillUser = seckillMessage.getSeckillUser();
        long goodsId = seckillMessage.getGoodsId();

        //执行秒杀流程，先判断数据库中还有没有库存
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        if (goodsVo.getStockCount() <= 0) {
            return;
        }

        //判断是否秒杀过这个商品，防止一个人秒杀多个商品
        SeckillOrder seckillOrder = orderService.
                getSeckillOrderBySeckillUserGoodsId(seckillUser.getId(), goodsId);
        if (seckillOrder != null) {
            return;
        }

        //执行秒杀操作 减库存 下订单 生成秒杀订单 用事务实现 返回订单详情页
        seckillService.seckill(seckillUser, goodsVo);
        return;
    }

    @RabbitListener(queues = MQConfig.directQueue)
    public void receive(String message) {
        log.info("receice message:" + message);
    }

    @RabbitListener(queues = MQConfig.topicQueueOne)
    public void receiveTopicQueueOne(String message) {
        log.info("receice topicQueueOne message:" + message);
    }

    @RabbitListener(queues = MQConfig.topicQueueTwo)
    public void receiveTopicQueueTwo(String message) {
        log.info("receice topicQueueTwo message:" + message);
    }

    @RabbitListener(queues = MQConfig.headersQueueOne)
    public void receiveHeadersQueueOne(byte[] message) {
        log.info("receice headersQueueOne message:" + new String(message));
    }

    @RabbitListener(queues = MQConfig.headersQueueTwo)
    public void receiveHeadersQueueTwo(byte[] message) {
        log.info("receice headersQueueTwo message:" + new String(message));
    }
}

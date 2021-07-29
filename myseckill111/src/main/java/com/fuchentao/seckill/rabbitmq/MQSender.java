package com.fuchentao.seckill.rabbitmq;

import com.fuchentao.seckill.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQSender {

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    AmqpTemplate amqpTemplate;

    //秒杀请求的消息队列用的是Direct模式
    public void sendSeckillMessage(SeckillMessage seckillMessage) {
        String msg = RedisService.beanToString(seckillMessage);
        log.info("send message:" + msg);
        amqpTemplate.convertAndSend(MQConfig.seckillQueue, msg);
    }

    public void send(Object message) {
        String msg = RedisService.beanToString(message);
        log.info("send message:" + msg);
        amqpTemplate.convertAndSend(MQConfig.directQueue, msg);
    }

    public void sendTopic(Object message) {
        String msg = RedisService.beanToString(message);
        log.info("send topic message:" + msg);
        //向topicExchange发送msg 匹配字符串指定为"topic.kaiche"
        amqpTemplate.convertAndSend
                            (MQConfig.topicExchange, "topic.kaiche", msg);
        amqpTemplate.convertAndSend
                            (MQConfig.topicExchange, "topic.kaichetupian", msg + "图片");
        //可以看到topicQueueOne无法收到第二条消息
    }

    public void sendFanout(Object message) {
        String msg = RedisService.beanToString(message);
        log.info("send fanout message:" + msg);
        amqpTemplate.convertAndSend
                (MQConfig.fanoutExchange, "", msg);
        amqpTemplate.convertAndSend
                (MQConfig.fanoutExchange, "", msg + "图片");
    }

    public void sendHeaders(Object message) {
        String msgStr = RedisService.beanToString(message);
        log.info("send headers message:" + msgStr);

        MessageProperties msgProperties = new MessageProperties();
        msgProperties.setHeader("headersOne", "valueOne");
        msgProperties.setHeader("headersThree", "valueThree");
        Message msgMessage = new Message(msgStr.getBytes(), msgProperties);

        amqpTemplate.convertAndSend
                (MQConfig.headersExchange, "", msgMessage);
        //可以看到topicQueueOne无法收到消息
    }

}

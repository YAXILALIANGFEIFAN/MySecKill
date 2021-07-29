package com.fuchentao.seckill.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;


@Configuration
public class MQConfig {

    public static final String seckillQueue = "seckillQueue";
    public static final String directQueue = "directQueue";
    public static final String topicQueueOne = "topicQueueOne";
    public static final String topicQueueTwo = "topicQueueTwo";
    public static final String headersQueueOne = "headersQueueOne";
    public static final String headersQueueTwo = "headersQueueTwo";

    public static final String topicExchange = "topicExchange";
    public static final String fanoutExchange = "fanoutExchange";
    public static final String headersExchange = "headersExchange";


    @Bean
    public Queue seckillQueue() {
        return new Queue(seckillQueue, true);
    }


    /**
     * RabnbitMQ的Direct模式，指定队列名称，发送消息接收消息
     */
    @Bean
    public Queue queue() {
        return new Queue(directQueue, true);
    }


    /**
     * RabnbitMQ的Topic模式，发送消息先发送给TopicExchange交换机
     * 交换机把按一定的规则把消息放进队列TopicQueue中，这种规则叫做绑定
     */
    @Bean
    public Queue topicQueueOne() {
        //消息队列的名字和 是否执行持久化
        return new Queue(topicQueueOne, true);
    }

    @Bean
    public Queue topicQueueTwo() {
        //消息队列的名字和 是否执行持久化
        return new Queue(topicQueueTwo, true);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(topicExchange);
    }

    @Bean
    public Binding topicBindingOne() {
        //把topicQueue绑定在路由topicExchange()上，并且指定匹配时的字符串(routingKey)
        //routingKey可以采用通配符，例如"topic.#"表示所有topic前缀的字符串
        return BindingBuilder.
                    bind(topicQueueOne()).to(topicExchange()).with("topic.kaiche");
    }

    @Bean
    public Binding topicBindingTwo() {
        //routingKey可以采用通配符，例如"topic.#"表示所有topic前缀的字符串
        return BindingBuilder.
                bind(topicQueueTwo()).to(topicExchange()).with("topic.#");
    }

    /**
     * RabnbitMQ的Fanout模式，暴力广播，凡是与FanoutExchange绑定的Queue都将收到消息
     * 这里顺手用之前创建的TopicQueue演示，不再另外创建Queue
     */
    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(fanoutExchange);
    }

    @Bean
    public Binding FanoutBindingOne() {
        return BindingBuilder.bind(topicQueueOne()).to(fanoutExchange());
    }

    @Bean
    public Binding FanoutBindingTwo() {
        return BindingBuilder.bind(topicQueueTwo()).to(fanoutExchange());
    }

    /**
     * RabnbitMQ的Headers模式与 Direct、Topic、Fanout不同，
     * 它是通过匹配 AMQP 协议消息的 Header而非RoutingKey，有点像HTTP的Headers
     * Headers模式与 Topic 类似，但是性能方面比后者差很多，所以在实际项目用的少
     */
    @Bean
    public Queue headersQueueOne() {
        //消息队列的名字和 是否执行持久化
        return new Queue(headersQueueOne, true);
    }

    @Bean
    public Queue headersQueueTwo() {
        return new Queue(headersQueueTwo, true);
    }

    @Bean
    public HeadersExchange headersExchange() {
        return new HeadersExchange(headersExchange);
    }

    @Bean
    public Binding headersBindingOne() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("headersOne", "valueOne");
        hashMap.put("headersTwo", "valueTwo");
        return BindingBuilder.
                    bind(headersQueueOne()).to(headersExchange()).
                            whereAll(hashMap).match();
    }

    @Bean
    public Binding headersBindingTwo() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("headersOne", "valueOne");
        hashMap.put("headersTwo", "valueTwo");
        return BindingBuilder.
                    bind(headersQueueTwo()).to(headersExchange()).
                            whereAny(hashMap).match();
    }
}

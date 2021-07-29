package com.fuchentao.seckill.redis;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Service
//jedisPoolFactory()方法要从RedisService中独立出来，否则会造成循环依赖
//RedisPoolFactory这个类名不能改成jedisPoolFactory
//因为改了的话容易被认为是构造方法，而构造方法是没有返回值的
public class RedisPoolFactory {

    @Autowired
    RedisConfig redisConfig;

    //先定义一个java Bean 通过jedisPoolFactory返回一个JedisPool对象
    @Bean
    public JedisPool jedisPoolFactory() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(redisConfig.getPoolMaxIdle());
        jedisPoolConfig.setMaxTotal(redisConfig.getPoolMaxTotal());
        //这里涉及到毫秒和秒的转换
        jedisPoolConfig.setMaxWaitMillis(redisConfig.getPoolMaxWait() * 1000);

        JedisPool jedisPool = new JedisPool(jedisPoolConfig,
                                            redisConfig.getHost(),
                                            redisConfig.getPort(),
                                            redisConfig.getTimeout() * 1000,
                                            redisConfig.getPassword(),
                                            0);
        return jedisPool;
    }

}

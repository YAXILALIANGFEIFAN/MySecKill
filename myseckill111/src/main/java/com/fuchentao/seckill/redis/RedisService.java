package com.fuchentao.seckill.redis;


import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


/*
MySQL是在dao层执行数据库的操作
redis是在service层执行操作

 */
@Service
public class RedisService {

    @Autowired
    JedisPool jedisPool;

    /**
     * 获取redis中的key对应的value
     * redis中的key是由方法中的prefix和key两个参数拼接而成
     * @param prefix
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
        //获取一个Jedis客户端
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String str = jedis.get(prefix.getPrefix() + key);
            T t = stringToBean(str, clazz);
            return t;
        }
        finally {
            returnToPool(jedis);
        }
    }

    /**
     * 在redis中设置key对应的value
     * redis中的key是由方法中的prefix和key两个参数拼接而成
     * @param prefix
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    public <T> boolean set(KeyPrefix prefix, String key, T value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String str = beanToString(value);
            //执行set语句操作redis 必须保证非空
            if (str == null || str.length() <= 0) {
                return false;
            }

            //设置过期时间
            if (prefix.expireSeconds() <= 0) {
                jedis.set(prefix.getPrefix() + key, str);
            }
            else {
                jedis.setex(prefix.getPrefix() + key,
                             prefix.expireSeconds(),
                             str);
            }
            return true;
        }
        finally {
            returnToPool(jedis);
        }
    }

    /**
     * 判断redis中指定的key是否存在，这里的泛型标记<T>其实可以去掉
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> boolean exists(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.exists(prefix.getPrefix() + key);
        }
        finally {
            returnToPool(jedis);
        }
    }

    /**
     * 删除redis中指定的key
     * @param prefix
     * @param key
     * @return
     */
    public boolean deleteKey(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Long result = jedis.del(prefix.getPrefix() + key);
            return result > 0;
        }
        finally {
            returnToPool(jedis);
        }
    }

    /**
     * 这里的泛型标记<T>其实可以去掉，主要调用jedis.incr()方法
     * jedis.incr()和jedis.decr()都是原子操作
     *
     * 给 redis 中指定的 key 对应的 value 加1，
     * 如果对应的 value 不能执行加1 操作(比如文字等类型就不能加1) 那么直接返回提醒报错
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 incr 操作
     * 返回值返回的是加1之后的 value 值
     * 注意，在 redis 中 value 是 String 类型，
     * redis 执行 INCR 语句得到的 value 还是 String 类型
     * 但是 jedis.incr() 已经帮忙处理好了，所以直接用 Long 类型接收返回值
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long incr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.incr(prefix.getPrefix() + key);
        }
        finally {
            returnToPool(jedis);
        }
    }

    /**
     * 请先查看incr()方法的注释，主要调用jedis.decr()方法
     * 给 redis 中指定的 key 对应的 value 减1，
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 decr 操作
     * 返回值返回的是减1之后的 value 值
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long decr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.decr(prefix.getPrefix() + key);
        }
        finally {
            returnToPool(jedis);
        }
    }

    /*
    把读到的结果转成指定的泛型T  这里的泛型只能是普通的泛型，不支持List类型
    先判断是不是基本数据类型，如果是就手动转换，否则调用JSON的序列化与反序列化
    */
    public static <T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.length() <= 0 || clazz == null) {
            return null;
        }
        if (clazz == int.class || clazz == Integer.class) {
            return (T)Integer.valueOf(str);
        }
        else if (clazz == long.class || clazz == Long.class) {
            return (T)Long.valueOf(str);
        }
        else if (clazz == String.class) {
            return (T)str;
        }
        else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }

    }

    //把泛型T转换成String类型的数据
    public static <T> String beanToString(T value) {
        if (value == null) {
            return null;
        }
        //执行参数校验，判断参数的类型
        Class<?> clazz = value.getClass();
        if (clazz == int.class || clazz == Integer.class) {
            return "" + value;
        }
        else if (clazz == long.class || clazz == Long.class) {
            return "" + value;
        }
        else if (clazz == String.class) {
            return (String)value;
        }
        else {
            return JSON.toJSONString(value);
        }
    }

    //把jedis客户端返回给jedisPool
    private void returnToPool(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

}

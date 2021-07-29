package com.fuchentao.seckill.dao;


import com.fuchentao.seckill.domain.OrderInfo;
import com.fuchentao.seckill.domain.SeckillOrder;
import com.fuchentao.seckill.vo.GoodsVo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderDao {

    @Select("select * from seckill_order " +
            "where user_id = #{seckillUserId} and goods_id = #{goodsId}")
    public SeckillOrder getSeckillOrderBySeckillUserGoodsId
                                (@Param("seckillUserId") Long seckillUserId,
                                 @Param("goodsId") long goodsId);

    /*
    执行插入操作后还需要返回order_info 表中当前的订单号，需要用到@SelectKey注解
    这个返回的订单号将会放在传入参数orderInfo对应的位置中
    */
    @Insert("insert into order_info" +
            "(user_id, goods_id, goods_name, goods_count, " +
                    "goods_price, order_channel, status, create_date) " +
            "values (#{userId}, #{goodsId}, #{goodsName}, #{goodsCount}, " +
                    "#{goodsPrice}, #{orderChannel}, #{status}, #{createDate} )")
    @SelectKey(keyColumn = "id", keyProperty = "id", resultType = long.class,
                    before = false, statement = "select last_insert_id()")
    public long insertOrderInfo(OrderInfo orderInfo);

    //向seckill_order 表中插入一条数据
    @Insert("insert into seckill_order (user_id, goods_id, order_id) " +
                    "values (#{userId}, #{goodsId}, #{orderId})")
    public int insertSeckillOrder(SeckillOrder seckillOrder);

    //根据orderId查找order_info表中的数据
    @Select("select * from order_info where id = #{orderId}")
    public OrderInfo getOrderInfoById(@Param("orderId")long orderId);
}

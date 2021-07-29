package com.fuchentao.seckill.dao;


import com.fuchentao.seckill.domain.Goods;
import com.fuchentao.seckill.domain.SeckillGoods;
import com.fuchentao.seckill.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GoodsDao {

    //并表查询所有的商品
    @Select("select g.*, sg.seckill_price, sg.stock_count, sg.start_date, sg.end_date " +
            "from seckill_goods sg left join goods g " +
            "on sg.goods_id = g.id")
    public List<GoodsVo> listGoodsVo();

    //根据商品的id 查询正常商品和秒杀商品 封装成GoodsVo 类对象
    @Select("select g.*, sg.seckill_price, sg.stock_count, sg.start_date, sg.end_date " +
            "from seckill_goods sg left join goods g " +
            "on sg.goods_id = g.id " +
            "where g.id = #{goodsId}")
    public GoodsVo getGoodsVoByGoodsId(@Param("goodsId")long goodsId);

    //根据商品id 减少秒杀商品的库存 返回更新后的库存
    @Update("update seckill_goods set stock_count = stock_count - 1 " +
            "where goods_id = #{goodsId} and stock_count > 0")
    public int reduceSeckillGoodsStock(SeckillGoods seckillGoodsCache);
    //seckillGoodsCache.goodsId 直接写成了#{goodsId}
}

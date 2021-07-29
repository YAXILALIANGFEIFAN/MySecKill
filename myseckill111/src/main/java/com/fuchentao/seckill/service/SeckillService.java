package com.fuchentao.seckill.service;

//import com.fuchentao.seckill.dao.GoodsDao;
import com.fuchentao.seckill.domain.OrderInfo;
import com.fuchentao.seckill.domain.SeckillOrder;
import com.fuchentao.seckill.domain.SeckillUser;
import com.fuchentao.seckill.redis.RedisService;
import com.fuchentao.seckill.redis.SeckillKey;
import com.fuchentao.seckill.util.MD5Util;
import com.fuchentao.seckill.util.UUIDUtil;
import com.fuchentao.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class SeckillService {

    /*
    service可以调用别的service，别的service可能有需要的缓存，比如session
    但是service只能调用自己的dao
    */
//    @Autowired(required = false)
//    GoodsDao goodsDao;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    //秒杀的核心功能，需要用@Transactional实现事务
    @Transactional
    public OrderInfo seckill(SeckillUser seckillUser, GoodsVo goodsVo) {
        //为秒杀商品减库存
        boolean isSuccess = goodsService.reduceSeckillGoodsStock(goodsVo);
        if (isSuccess) {
            //创建订单 在 order_info 和 seckill_order 中各创建一条记录
            return orderService.createOrderInfoSeckillOrder(seckillUser, goodsVo);
        }
        setSeckillGoodsOver(goodsVo.getId());
        return null;
    }

    //获取秒杀结果，三种可能，秒杀成功，秒杀失败，排队中
    public long getSeckillResult(Long seckillUserId, long goodsId) {
        SeckillOrder seckillOrder = orderService.
                getSeckillOrderBySeckillUserGoodsId(seckillUserId, goodsId);
        if (seckillOrder != null) {
            //秒杀成功，返回订单号
            return seckillOrder.getOrderId();
        }
        else {
            boolean isSeckillOver = getSeckillGoodsOver(goodsId);
            if (isSeckillOver) {
                //秒杀商品都被抢完了，还没有得到订单，说明秒杀失败
                return -1;
            }
            else {
                //秒杀商品没被抢完，还没有得到订单，说明在排队
                return 0;
            }
        }

    }

    private boolean getSeckillGoodsOver(long goodsVoId) {
        return redisService.exists
                (SeckillKey.isSeckillGoodsOver, "" + goodsVoId);
    }

    private void setSeckillGoodsOver(Long goodsVoId) {
        redisService.set
                (SeckillKey.isSeckillGoodsOver, "" + goodsVoId, true);
    }

    public boolean checkPath
                    (String path, SeckillUser seckillUser, long goodsId) {
        if (seckillUser == null || path == null) {
            return false;
        }
        String str = redisService.get(SeckillKey.getSeckillPath,
                "" + seckillUser.getId() + "_" + goodsId, String.class);
        return path.equals(str);
    }

    public String createPath(SeckillUser seckillUser, long goodsId) {
        String path = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisService.set(SeckillKey.getSeckillPath,
                "" + seckillUser.getId() + "_" + goodsId, path);
        return path;
    }
}

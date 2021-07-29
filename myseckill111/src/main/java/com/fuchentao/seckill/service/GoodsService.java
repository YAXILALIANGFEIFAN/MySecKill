package com.fuchentao.seckill.service;

import com.fuchentao.seckill.dao.GoodsDao;
import com.fuchentao.seckill.domain.Goods;
import com.fuchentao.seckill.domain.SeckillGoods;
import com.fuchentao.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    @Autowired(required = false)
    GoodsDao goodsDao;

    public List<GoodsVo> listGoodsVo() {
        return goodsDao.listGoodsVo();
    }

    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }

    public boolean reduceSeckillGoodsStock(GoodsVo goodsVo) {
        SeckillGoods seckillGoodsCache = new SeckillGoods();
        seckillGoodsCache.setGoodsId(goodsVo.getId());
        seckillGoodsCache.setStockCount(goodsVo.getGoodsStock() - 1);
        //对秒杀表操作应该在函数名体现
        int latestStock = goodsDao.reduceSeckillGoodsStock(seckillGoodsCache);
        return latestStock > 0;
    }
}

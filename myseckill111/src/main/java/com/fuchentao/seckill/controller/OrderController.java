package com.fuchentao.seckill.controller;


import com.fuchentao.seckill.domain.OrderInfo;
import com.fuchentao.seckill.domain.SeckillUser;
import com.fuchentao.seckill.redis.RedisService;
import com.fuchentao.seckill.result.CodeMsg;
import com.fuchentao.seckill.result.Result;
import com.fuchentao.seckill.service.GoodsService;
import com.fuchentao.seckill.service.OrderService;
import com.fuchentao.seckill.service.SeckillUserService;
import com.fuchentao.seckill.vo.GoodsVo;
import com.fuchentao.seckill.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
@RequestMapping("/order")
public class OrderController {

//	@Autowired
//	SeckillUserService seckillUserService;
//
//	@Autowired
//	RedisService redisService;
	
	@Autowired
    OrderService orderService;
	
	@Autowired
	GoodsService goodsService;

	/*
	要从页面传过来的请求中获取orderId

	 */
    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, SeckillUser seckillUser,
                                      @RequestParam("orderId") long orderId) {

        //可以直接定义一个拦截器@NeedLogin避免重复代码
        if (seckillUser == null) {
            return Result.error(CodeMsg.sessionError);
        }

        //orderId --> orderInfo --> goodsId --> goodsVo
        OrderInfo orderInfo = orderService.getOrderInfoById(orderId);
        if (orderInfo == null) {
            return Result.error(CodeMsg.orderNotExist);
        }
        Long goodsId = orderInfo.getGoodsId();
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);

        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setGoodsVo(goodsVo);
        orderDetailVo.setOrderInfo(orderInfo);

        return Result.success(orderDetailVo);
    }
    
}

package com.fuchentao.seckill.controller;


import com.fuchentao.seckill.access.AccessLimit;
import com.fuchentao.seckill.domain.OrderInfo;
import com.fuchentao.seckill.domain.SeckillOrder;
import com.fuchentao.seckill.domain.SeckillUser;
import com.fuchentao.seckill.rabbitmq.MQSender;
import com.fuchentao.seckill.rabbitmq.SeckillMessage;
import com.fuchentao.seckill.redis.AccessKey;
import com.fuchentao.seckill.redis.GoodsKey;
import com.fuchentao.seckill.redis.RedisService;
import com.fuchentao.seckill.redis.SeckillKey;
import com.fuchentao.seckill.result.CodeMsg;
import com.fuchentao.seckill.result.Result;
import com.fuchentao.seckill.service.GoodsService;
import com.fuchentao.seckill.service.OrderService;
import com.fuchentao.seckill.service.SeckillService;
import com.fuchentao.seckill.service.SeckillUserService;
import com.fuchentao.seckill.util.MD5Util;
import com.fuchentao.seckill.util.UUIDUtil;
import com.fuchentao.seckill.vo.GoodsVo;
import com.sun.org.apache.bcel.internal.classfile.Code;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean{

    @Autowired
    SeckillUserService seckillUserService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SeckillService seckillService;

    @Autowired
    MQSender mqSender;

    /*
    localHashMap相当于内存标记，用于减少redis的访问
    Long表示秒杀商品的ID Boolean表示是否被秒杀完了，默认为false

    实现内存标记，bitMap是最优解，数组的索引就是秒杀商品的id  不用因为存储key消耗空间
    bitMap也不会有扩容机制而产生额外的空间浪费
    int[] arr = new int[1000];
    int index = 30000;
    int intIndex = index / 32;
    int bitIndex = index % 32;
    arr[intIndex] = (arr[intIndex] | (1 << bitIndex));

    在多线程的情况下，读数组是线程安全的，写不是(其实这里也不需要写操作)
    如果非要在多线程的情况下写，用J.U.C包下的AtomicIntegerArray类
    */
    private HashMap<Long, Boolean> localHashMap = new HashMap<>();

    //系统初始化，把秒杀商品的库存加载进redis
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVoList = goodsService.listGoodsVo();
        if (goodsVoList == null) {
            return;
        }
        for (GoodsVo goodsVo : goodsVoList) {
            redisService.set
                    (GoodsKey.getSeckillGoodsStockCount,
                            "" + goodsVo.getId(), goodsVo.getStockCount());
            localHashMap.put(goodsVo.getId(), false);
        }
    }

    /*
    goodsId这里是goodsVoId的意思，只是页面goods_detail传过来的参数叫这个名字
    这里的提交方式必须指定为POST，因为对数据库产生了影响
    如果用GET 搜索引擎在爬取网页的时候可能就不知不觉中执行了数据库操作
    */
    @RequestMapping(value = "/{path}/do_seckill", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> seckill(Model model, SeckillUser seckillUser,
                       @RequestParam("goodsId")long goodsId,
                       @PathVariable("path") String path){

        model.addAttribute("seckillUser", seckillUser);
        //判断用户是否已经登录，判断随机path是否正确，并且用新的随机path覆盖原来的path
        if (seckillUser == null) {
            return Result.error(CodeMsg.sessionError);
        }
        boolean isValidPath =  seckillService.checkPath(path, seckillUser, goodsId);
        if (!isValidPath) {
            return Result.error(CodeMsg.requestIllegal);
        }
        String latestPath = seckillService.createPath(seckillUser, goodsId);


        //判断hashmap内存标记，判断redis中秒杀商品是否还有库存
        if (localHashMap.get(goodsId)) {
            return Result.error(CodeMsg.seckillOver);
        }

        Long afterDecr = redisService.decr
                            (GoodsKey.getSeckillGoodsStockCount, "" + goodsId);
        if (afterDecr < 0) {
            localHashMap.put(goodsId, true);
            return Result.error(CodeMsg.seckillOver);
        }

        //判断是否秒杀到商品，防止一个人秒杀多个商品
        SeckillOrder seckillOrder = orderService.
                getSeckillOrderBySeckillUserGoodsId(seckillUser.getId(), goodsId);
        if (seckillOrder != null) {
            return Result.error(CodeMsg.seckillRepeat);
        }

        //秒杀请求进入消息队列 返回给用户"排队中"
        SeckillMessage seckillMessage = new SeckillMessage();
        seckillMessage.setSeckillUser(seckillUser);
        seckillMessage.setGoodsId(goodsId);
        mqSender.sendSeckillMessage(seckillMessage);

        return Result.success(0);
    }



    /*
    @RequestMapping(value = "/do_seckill", method = RequestMethod.POST)
    @ResponseBody
    public Result<OrderInfo> seckill(Model model, SeckillUser seckillUser,
                                  @RequestParam("goodsId")long goodsId){

        model.addAttribute("seckillUser", seckillUser);
        //判断用户是否已经登录，判断秒杀是否还有库存
        if (seckillUser == null) {
            return Result.error(CodeMsg.sessionError);
        }
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        if (goodsVo.getStockCount() <= 0) {
            return Result.error(CodeMsg.seckillOver);
        }

        //判断是否秒杀到商品，防止一个人秒杀多个商品
        SeckillOrder seckillOrder = orderService.
                getSeckillOrderBySeckillUserGoodsId(seckillUser.getId(), goodsId);
        if (seckillOrder != null) {
            return Result.error(CodeMsg.seckillRepeat);
        }

        //执行秒杀操作 减库存 下订单 生成秒杀订单 用事务实现 返回订单详情页
        OrderInfo orderInfo = seckillService.seckill(seckillUser, goodsVo);
        return Result.success(orderInfo);
    }
    */



    /*
    //goodsId这里是goodsVoId的意思，只是页面goods_detail传过来的参数叫这个名字
    //这里的提交方式必须指定为POST
    @RequestMapping("/do_seckill")
    public String list(Model model, SeckillUser seckillUser,
                        @RequestParam("goodsId")long goodsId){

        model.addAttribute("seckillUser", seckillUser);
        //判断用户是否已经登录，判断秒杀是否还有库存
        if (seckillUser == null) {
            return "login";
        }
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        if (goodsVo.getStockCount() <= 0) {
            model.addAttribute("errmsg", CodeMsg.seckillOver.getMsg());
            return "seckill_fail";
        }

        //判断是否秒杀到商品，防止一个人秒杀多个商品
        SeckillOrder seckillOrder = orderService.getSeckillOrderBySeckillUserGoodsId
                                                    (seckillUser.getId(), goodsId);
        if (seckillOrder != null) {
            model.addAttribute("errmsg", CodeMsg.seckillRepeat.getMsg());
            return "seckill_fail";
        }

        //执行秒杀操作 减库存 下订单 生成秒杀订单 用事务实现 返回订单详情页
        OrderInfo orderInfo = seckillService.seckill(seckillUser, goodsVo);
        model.addAttribute("orderInfo", orderInfo);
        model.addAttribute("goodsVo", goodsVo);

        return "order_detail";
    }
    */


    //获取秒杀结果
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> seckillResult(Model model, SeckillUser seckillUser,
                                @RequestParam("goodsId")long goodsId) {

        model.addAttribute("seckillUser", seckillUser);
        //判断用户是否已经登录 判断redis中秒杀商品是否还有库存
        if (seckillUser == null) {
            return Result.error(CodeMsg.sessionError);
        }

        //result如果是返回了orderId 说明秒杀成功， 返回-1  秒杀失败， 返回0 排队中
        long result = seckillService.getSeckillResult
                                            (seckillUser.getId(), goodsId);
        return Result.success(result);
    }


    //秒杀地址被隐藏，需要在这里获取
    //@AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillPath(HttpServletRequest httpServletRequest, SeckillUser seckillUser,
                                         @RequestParam("goodsId")long goodsId) {

        //判断用户是否已经登录 判断redis中秒杀商品是否还有库存
        if (seckillUser == null) {
            return Result.error(CodeMsg.sessionError);
        }

        /*
        //查询秒杀页面的访问次数
        String requestURI = httpServletRequest.getRequestURI();
        String key = requestURI + "_" + seckillUser.getId();
        Integer accessTime = redisService.get
                                    (AccessKey.getAccessKey, key, Integer.class);
        //接口限流，在AccessKey的有效期内，访问次数上限为300
        int limitTime = 5;
        if (accessTime == null) {
            redisService.set(AccessKey.getAccessKey, key, 1);
        }
        else if (accessTime <= limitTime) {
            redisService.incr(AccessKey.getAccessKey, key);
        }
        else {
            return Result.error(CodeMsg.accessLimit);
        }
        */

        String path = seckillService.createPath(seckillUser, goodsId);
        return Result.success(path);
    }

}

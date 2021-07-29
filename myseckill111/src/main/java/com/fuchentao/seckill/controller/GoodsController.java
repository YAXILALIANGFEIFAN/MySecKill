package com.fuchentao.seckill.controller;


import com.fuchentao.seckill.domain.SeckillUser;
import com.fuchentao.seckill.redis.GoodsKey;
import com.fuchentao.seckill.redis.RedisService;
import com.fuchentao.seckill.result.Result;
import com.fuchentao.seckill.service.GoodsService;
import com.fuchentao.seckill.service.SeckillUserService;
import com.fuchentao.seckill.vo.GoodsDetailVo;
import com.fuchentao.seckill.vo.GoodsVo;
import com.fuchentao.seckill.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    SeckillUserService seckillUserService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    RedisService redisService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;

    /*
    to_list.html是从to_login.html跳转过来的，这里的请求是带有cookie和token的，
    因此可以把他们获取出来 cookie和token有可能在手机端，也有可能在网页端
    手机端通常放在请求参数里面(@RequestParam)
    电脑端通常放在cookie里面(@CookieValue)
    但是每次都从cookie中获取token 再从redis中获取对象总是过于麻烦
    因此直接在方法中传递seckillUser参数，这样需要重写config包下的ArgumentResolvers方法

    页面静态化，把goods_list页面缓存在redis中
    */
    @RequestMapping(value = "/to_list", produces = "text/html")
    @ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response,
                        Model model, SeckillUser seckillUser){

        model.addAttribute("seckillUser", seckillUser);

        //从redis中获取goods_list 的页面缓存
        //如果没有缓存，执行手动渲染，保存进redis，返回给客户端
        String goodsListHtml = redisService.get
                                    (GoodsKey.getGoodsList, "", String.class);
        if (!StringUtils.isEmpty(goodsListHtml)) {
            return goodsListHtml;
        }

        //查询商品列表 Goods和SeckillGoods都查出来
        List<GoodsVo> goodsVoList = goodsService.listGoodsVo();
        model.addAttribute("goodsVoList", goodsVoList);

        SpringWebContext springWebContext = new SpringWebContext
                (request, response, request.getServletContext(), request.getLocale(),
                        model.asMap(), applicationContext);
        goodsListHtml = thymeleafViewResolver.getTemplateEngine().process
                ("goods_list", springWebContext);
        if (!StringUtils.isEmpty(goodsListHtml)) {
            redisService.set(GoodsKey.getGoodsList, "", goodsListHtml);
        }
        return goodsListHtml;
    }


    /*
    @RequestMapping("/to_list")
    public String list(Model model, SeckillUser seckillUser){

        model.addAttribute("seckillUser", seckillUser);
        //查询商品列表 Goods和SeckillGoods都查出来
        List<GoodsVo> goodsVoList = goodsService.listGoodsVo();
        model.addAttribute("goodsVoList", goodsVoList);
        return "goods_list";
    }
    */


    /*
    @RequestMapping("/to_list")
    public String list
        (HttpServletResponse response, Model model,
         @CookieValue(value = SeckillUserService.cookieNameToken, required = false) String cookieToken,
         @RequestParam(value = SeckillUserService.cookieNameToken, required = false) String paramToken){

        //如果没有cookie，返回登录页面
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return "login";
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        SeckillUser seckillUser = seckillUserService.getByToken(response, token);
        model.addAttribute("seckillUser", seckillUser);
        return "goods_list";
    }
     */


    //这里的@RequestMapping注解不再带有参数 produces = "text/html"
    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response,
                                        Model model, SeckillUser seckillUser,
                                        @PathVariable("goodsId")long goodsId){

        //snowflake算法防止数据库被遍历
        //查询商品详细信息
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);

        //判断秒杀是否正在进行
        long startTime = goodsVo.getStartDate().getTime();
        long endTime = goodsVo.getEndDate().getTime();
        long nowTime = System.currentTimeMillis();
        //定义秒杀状态，秒杀开始倒计时，倒计时应该在客户端执行，(现在各大电商都是这种方案)
        //否则所有的客户端都要不断访问服务端，服务端压力很大
        int seckillStatus = 0;
        int remainSeconds = 0;

        if (nowTime < startTime) {
            //秒杀还没有开始，倒计时
            seckillStatus = 0;
            remainSeconds = (int) (startTime - nowTime) / 1000;
        }
        else if (nowTime > endTime) {
            //秒杀已结束
            seckillStatus = 2;
            remainSeconds = -1;
        }
        else {
            //秒杀正在进行
            seckillStatus = 1;
            remainSeconds = 0;
        }

        GoodsDetailVo goodsDetailVo = new GoodsDetailVo();
        goodsDetailVo.setGoodsVo(goodsVo);
        goodsDetailVo.setSeckillUser(seckillUser);
        goodsDetailVo.setSeckillStatus(seckillStatus);
        goodsDetailVo.setRemainSeconds(remainSeconds);
        return Result.success(goodsDetailVo);
    }


    /*
    @RequestMapping(value = "/to_detail/{goodsId}", produces = "text/html")
    @ResponseBody
    public String detail(HttpServletRequest request, HttpServletResponse response,
                         Model model, SeckillUser seckillUser,
                         @PathVariable("goodsId")long goodsId){

        //snowflake算法防止数据库被遍历
        model.addAttribute("seckillUser", seckillUser);

        //从redis中获取goods_detail 的页面缓存
        //如果没有缓存，执行手动渲染，保存进redis，返回给客户端
        String goodsDetailHtml = redisService.get
                (GoodsKey.getGoodsDetail, "" + goodsId, String.class);
        if (!StringUtils.isEmpty(goodsDetailHtml)) {
            return goodsDetailHtml;
        }

        //手动渲染
        //查询商品详细信息
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goodsVo", goodsVo);

        //判断秒杀是否正在进行
        long startTime = goodsVo.getStartDate().getTime();
        long endTime = goodsVo.getEndDate().getTime();
        long nowTime = System.currentTimeMillis();
        //定义秒杀状态，秒杀开始倒计时，倒计时应该在客户端执行，(现在各大电商都是这种方案)
        //否则所有的客户端都要不断访问服务端，服务端压力很大
        int seckillStatus = 0;
        int remainSeconds = 0;

        if (nowTime < startTime) {
            //秒杀还没有开始，倒计时
            seckillStatus = 0;
            remainSeconds = (int) (startTime - nowTime) / 1000;
        }
        else if (nowTime > endTime) {
            //秒杀已结束
            seckillStatus = 2;
            remainSeconds = -1;
        }
        else {
            //秒杀正在进行
            seckillStatus = 1;
            remainSeconds = 0;
        }

        model.addAttribute("seckillStatus", seckillStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        SpringWebContext springWebContext = new SpringWebContext
                (request, response, request.getServletContext(), request.getLocale(),
                        model.asMap(), applicationContext);
        goodsDetailHtml = thymeleafViewResolver.getTemplateEngine().process
                ("goods_detail", springWebContext);
        if (!StringUtils.isEmpty(goodsDetailHtml)) {
            redisService.set(GoodsKey.getGoodsDetail, "" + goodsId, goodsDetailHtml);
        }
        return goodsDetailHtml;
    }
    */


   /*
    @RequestMapping("/to_detail/{goodsId}")
    public String detail(Model model, SeckillUser seckillUser,
                         @PathVariable("goodsId")long goodsId){

        //snowflake算法防止数据库被遍历
        model.addAttribute("seckillUser", seckillUser);
        //查询商品详细信息
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goodsVo", goodsVo);

        //判断秒杀是否正在进行
        long startTime = goodsVo.getStartDate().getTime();
        long endTime = goodsVo.getEndDate().getTime();
        long nowTime = System.currentTimeMillis();
        //定义秒杀状态，秒杀开始倒计时，倒计时应该在客户端执行，(现在各大电商都是这种方案)
        //否则所有的客户端都要不断访问服务端，服务端压力很大
        int seckillStatus = 0;
        int remainSeconds = 0;

        if (nowTime < startTime) {
            //秒杀还没有开始，倒计时
            seckillStatus = 0;
            remainSeconds = (int) (startTime - nowTime) / 1000;
        }
        else if (nowTime > endTime) {
            //秒杀已结束
            seckillStatus = 2;
            remainSeconds = -1;
        }
        else {
            //秒杀正在进行
            seckillStatus = 1;
            remainSeconds = 0;
        }

        model.addAttribute("seckillStatus", seckillStatus);
        model.addAttribute("remainSeconds", remainSeconds);
        return "goods_detail";
    }
    */

}

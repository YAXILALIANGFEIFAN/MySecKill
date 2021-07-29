package com.fuchentao.seckill.controller;


import com.fuchentao.seckill.redis.RedisService;
import com.fuchentao.seckill.result.CodeMsg;
import com.fuchentao.seckill.result.Result;
import com.fuchentao.seckill.service.SeckillUserService;
import com.fuchentao.seckill.util.ValidatorUtil;
import com.fuchentao.seckill.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {

    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    SeckillUserService seckillUserService;

//    @Autowired
//    RedisService redisService;

    //不能加@ResponseBody注解，否则只会输出login文字，不能跳转到login.html页面
    //把所有的页面跳转功能都交给 controller 实现
    @RequestMapping("/to_login")
    public String toLogin(){
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<Boolean> doLogin
            (HttpServletResponse response, @Valid LoginVo loginVo){

        //日志文件将会在控制台输出，如果需要写成文本，需要配置properties文件
        log.info(loginVo.toString());
        //参数校验
        //StringUtils.isEmpty()方法可以避免参数为null或者结果为null而引发的异常
//        String formPassword = loginVo.getPassword();
//        String mobile = loginVo.getMobile();
//        if (StringUtils.isEmpty(formPassword)) {
//            return Result.error(CodeMsg.passwordEmpty);
//        }
//        if (StringUtils.isEmpty(mobile)) {
//            return Result.error(CodeMsg.mobileEmpty);
//        }
//        if (!(ValidatorUtil.isMobile(mobile))) {
//            return Result.error(CodeMsg.mobileError);
//        }
        //参数校验没问题那么就执行登陆
//        CodeMsg codeMsg = seckillUserService.login(loginVo);
//        if (codeMsg.getCode() == 0) {
//            return Result.success(true);
//        }
//        else {
//            return Result.error(codeMsg);
//        }

        //因为所有可能的异常都被抛出并且捕获，所以直接返回true
        seckillUserService.login(response, loginVo);
        return Result.success(true);

        //用户登录成功，跳转到商品列表页
    }

}

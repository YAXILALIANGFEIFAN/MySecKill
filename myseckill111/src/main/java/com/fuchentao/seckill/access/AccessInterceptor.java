package com.fuchentao.seckill.access;

import com.alibaba.fastjson.JSON;
import com.fuchentao.seckill.domain.SeckillUser;
import com.fuchentao.seckill.redis.AccessKey;
import com.fuchentao.seckill.redis.RedisService;
import com.fuchentao.seckill.result.CodeMsg;
import com.fuchentao.seckill.result.Result;
import com.fuchentao.seckill.service.SeckillUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    SeckillUserService seckillUserService;

    @Autowired
    RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                              HttpServletResponse response,
                              Object handler) throws Exception {

        if (handler instanceof HandlerMethod) {
            //先取出seckillUser并且放进ThreadLocal
            SeckillUser seckillUser = getSeckillUser(request, response);
            SeckillUserContext.setSeckillUser(seckillUser);

            HandlerMethod handlerMethod = (HandlerMethod)handler;
            AccessLimit accessLimit = handlerMethod.
                                            getMethodAnnotation(AccessLimit.class);
            //如果没有注解，那么直接返回true，如果有注解，取出注解信息
            if (accessLimit == null) {
                return true;
            }

            int expireSeconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            String requestURI = request.getRequestURI();
            String key = null;
            //在需要登录的页面用户没有登录，返回false并提示错误信息
            if (needLogin) {
                if (seckillUser == null) {
                    render(response, CodeMsg.sessionError);
                    return false;
                }
                key = requestURI + "_" + seckillUser.getId();
            }
            else {
                //如果页面不需要登录，暂时什么也不做
            }

            //查询页面的访问次数
            AccessKey accessKey = AccessKey.getCustomAccessKey(expireSeconds);
            Integer accessTime = redisService.get(accessKey, key, Integer.class);
            //接口限流，在AccessKey的有效期内，访问次数上限为maxCount
            if (accessTime == null) {
                redisService.set(accessKey, key, 1);
            }
            else if (accessTime <= maxCount) {
                redisService.incr(accessKey, key);
            }
            else {
                render(response, CodeMsg.accessLimit);
                return false;
            }

        }

        return true;
    }

    private void render
            (HttpServletResponse response, CodeMsg codeMsg) throws Exception{

        response.setContentType("application/json;charset=UTF-8");
        OutputStream outputStream = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(codeMsg));
        outputStream.write(str.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();
    }

    private SeckillUser getSeckillUser(HttpServletRequest request,
                                        HttpServletResponse response) {
        String paramToken =
                request.getParameter(SeckillUserService.cookieNameToken);
        String cookieToken =
                getCookieValue(request, SeckillUserService.cookieNameToken);

        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        return seckillUserService.getByToken(response, token);
    }

    private String getCookieValue
            (HttpServletRequest request, String cookieToken) {
        Cookie[] cookies = request.getCookies();
        //可能会出现空指针异常
        if (cookies == null || cookies.length <= 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieToken)) {
                return cookie.getValue();
            }
        }
        return null;
    }


}

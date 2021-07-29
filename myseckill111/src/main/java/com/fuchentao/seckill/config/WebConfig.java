package com.fuchentao.seckill.config;

import com.fuchentao.seckill.access.AccessInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/*
注册一些自己写的配置类，比如
    从网页请求中获取SeckillUser
    网页计数器注解

 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter{

    @Autowired
    SeckillUserArgumentResolver seckillUserArgumentResolver;

    @Autowired
    AccessInterceptor accessInterceptor;

    /*
    controller里面可以带很多参数HttpServletResponse Model等等
    这些参数都是由addArgumentResolvers方法带来的
     */
    @Override
    public void addArgumentResolvers
                    (List<HandlerMethodArgumentResolver> argumentResolvers) {

        argumentResolvers.add(seckillUserArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessInterceptor);
    }

}

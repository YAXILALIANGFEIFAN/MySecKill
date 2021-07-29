package com.fuchentao.seckill.config;

import com.fuchentao.seckill.access.SeckillUserContext;
import com.fuchentao.seckill.domain.SeckillUser;
import com.fuchentao.seckill.service.SeckillUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class SeckillUserArgumentResolver
                            implements HandlerMethodArgumentResolver {

    @Autowired
    SeckillUserService seckillUserService;

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        Class<?> clazz = methodParameter.getParameterType();
        return clazz == SeckillUser.class;
    }

    //自动根据token获取SeckillUser
    @Override
    public Object resolveArgument(MethodParameter methodParameter,
                                   ModelAndViewContainer modelAndViewContainer,
                                   NativeWebRequest nativeWebRequest,
                                   WebDataBinderFactory webDataBinderFactory)
            throws Exception {

        /*
        HttpServletRequest request =
                        nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response =
                        nativeWebRequest.getNativeResponse(HttpServletResponse.class);

        String paramToken =
                        request.getParameter(SeckillUserService.cookieNameToken);
        String cookieToken =
                        getCookieValue(request, SeckillUserService.cookieNameToken);

        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        return seckillUserService.getByToken(response, token);
        */

        return SeckillUserContext.getSeckillUser();
    }

    /*
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
    */
}

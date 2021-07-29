package com.fuchentao.seckill.service;

import com.fuchentao.seckill.dao.SeckillUserDao;
import com.fuchentao.seckill.domain.SeckillUser;
import com.fuchentao.seckill.exception.GlobalException;
import com.fuchentao.seckill.redis.RedisService;
import com.fuchentao.seckill.redis.SeckillUserKey;
import com.fuchentao.seckill.result.CodeMsg;
import com.fuchentao.seckill.util.MD5Util;
import com.fuchentao.seckill.util.UUIDUtil;
import com.fuchentao.seckill.vo.LoginVo;
import com.sun.deploy.net.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

@Service
public class SeckillUserService {

    public static final String cookieNameToken = "token";

    @Autowired(required = false)
    SeckillUserDao seckillUserDao;

    @Autowired
    RedisService redisService;

    /*
    为了防止缓存穿透攻击，在这里手动实现一个布隆过滤器，
    把数据库中的所有用户根据 手机号+formPassword  映射成bitMap  实测能抵御99.9%恶意攻击
    int[] arr = new int[1000];
    int index = 30000;
    int intIndex = index / 32;
    int bitIndex = index % 32;
    arr[intIndex] = (arr[intIndex] | (1 << bitIndex));

     */
    public SeckillUser getById(Long id) {
        //从缓存中取出SeckillUser
        SeckillUser seckillUser = redisService.get
                    (SeckillUserKey.getSeckillUser, "" + id, SeckillUser.class);
        if (seckillUser != null) {
            return seckillUser;
        }

        //(Cache Aside Mode)如果redis中没有SeckillUser  先从mysql中取出数据 再存进redis
        seckillUser = seckillUserDao.getById(id);
        if (seckillUser != null) {
            redisService.set
                    (SeckillUserKey.getSeckillUser, "" + id, seckillUser);
        }
        return seckillUser;
    }

    /*
    注意，用户传过来的是FormPassword，是原始密码经过了一次MD5的
    对象级缓存在更新的时候不能随意删除，比如session，删除了的话用户无法正常登陆
    */
    public boolean updatePassword(Long id, String myNewFormPassword, String token) {
        SeckillUser seckillUser = getById(id);
        if (seckillUser == null) {
            throw new GlobalException(CodeMsg.mobileNotExit);
        }
        SeckillUser updatedSeckillUser = seckillUser;
        updatedSeckillUser.setId(id);
        updatedSeckillUser.setPassword(MD5Util.formPasswordToDBPassword
                                           (myNewFormPassword, seckillUser.getSalt()));

        //更新数据库，并且修改redis缓存
        seckillUserDao.updatePassword(updatedSeckillUser);
        redisService.deleteKey(SeckillUserKey.getSeckillUser, "" + id);
        redisService.set(SeckillUserKey.token, token, updatedSeckillUser);
        return true;
    }

    public SeckillUser getByToken(HttpServletResponse response, String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        //用户重新进入一次页面，调用下面的addCookie方法
        //延长token的有效期，相当于在redis中把原来的token重新设置生命倒计时
        SeckillUser seckillUser =
                redisService.get(SeckillUserKey.token, token, SeckillUser.class);
        if (seckillUser != null) {
            addCookie(response, token, seckillUser);
        }
        return seckillUser;
    }

    //返回值类型从CodeMsg改为boolean
    public boolean login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
//            return CodeMsg.serverError;
            throw new GlobalException(CodeMsg.serverError);
            //原先是返回CodeMsg.serverError，展示给页面，
            //现在直接返回一个全局异常，交给GlobalExceptionHandler处理
        }

        String mobile = loginVo.getMobile();
        String formPassword = loginVo.getPassword();
        //判断手机号是否存在，这里需要查询数据库
        SeckillUser seckillUser = getById(Long.parseLong(mobile));
        if (seckillUser == null) {
//            return CodeMsg.mobileNotExit;
            throw new GlobalException(CodeMsg.mobileNotExit);
        }
        //验证密码
        String dbPassword = seckillUser.getPassword();
        String saltDB = seckillUser.getSalt();
        String calcPassword = MD5Util.formPasswordToDBPassword(formPassword, saltDB);
        if (!(dbPassword.equals(calcPassword))) {
//            return CodeMsg.passwordError;
            throw new GlobalException(CodeMsg.passwordError);
        }
        //登陆成功，在redis中生成用户的token  token其实就是数据库里面的用户数据
        //生成用户的cookie，cookie的有效期与session保持一致,cookie设在网站的根目录下
        //把响应写在response里面
        String token = UUIDUtil.uuid();
        addCookie(response, token, seckillUser);

        return true;
    }

    private void addCookie
                (HttpServletResponse response, String token, SeckillUser seckillUser) {

        redisService.set(SeckillUserKey.token, token, seckillUser);
        Cookie cookie = new Cookie(cookieNameToken, token);
        cookie.setMaxAge(SeckillUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

}

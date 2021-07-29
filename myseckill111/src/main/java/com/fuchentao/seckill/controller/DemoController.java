package com.fuchentao.seckill.controller;

/*
DAO层  DAO层叫数据访问层 data access object，
属于一种比较底层，比较基础的操作，具体到对于某个表的增删改查，
也就是说某个DAO一定是和数据库的某一张表一一对应的，其中封装了增删改查基本操作，
建议DAO只做原子操作，增删改查。

Service层 Service层叫服务层，被称为服务，
粗略的理解就是对一个或多个DAO进行的再次封装，封装成一个服务，
所以这里也就不会是一个原子操作了，需要事物控制。

Controler层 Controler负责请求转发，接受页面过来的参数，
传给Service处理，接到返回值，再传给页面。

总结  DAO面向表，Service面向业务。
后端开发时先数据库设计出所有表，然后对每一张表设计出DAO层，
然后根据具体的业务逻辑进一步封装DAO层成一个Service层，对外提供成一个服务

 */

import com.fuchentao.seckill.domain.User;
import com.fuchentao.seckill.rabbitmq.MQSender;
import com.fuchentao.seckill.redis.RedisService;
import com.fuchentao.seckill.redis.UserKey;
import com.fuchentao.seckill.result.CodeMsg;
import com.fuchentao.seckill.result.Result;
import com.fuchentao.seckill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
//@RequestMapping注解用来映射请求，指定控制器可以处理哪些请求，相当于xml配置文件
public class DemoController {

    @RequestMapping("/")
    @ResponseBody
    public String home(){
        return "Hello World";
    }

    @RequestMapping("/hello")
    @ResponseBody
    public Result<String> hello(){
        return Result.success("Hello, KMP");
    }

    @RequestMapping("/helloError")
    @ResponseBody
    public Result<String> helloError(){
        return Result.error(CodeMsg.serverError);
    }

    @RequestMapping("/thymeleaf")
    public String myThymeleaf(Model model){
        model.addAttribute("name", "BFPRT");
        return "hello";
    }

    @Autowired
    UserService userService;

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> dbGet(){
        User user = userService.getById(1);
        return Result.success(user);
    }

//    @RequestMapping("/db/transaction")
//    @ResponseBody
//    public Result<Boolean> dbTransaction() {
//        userService.transaction();
//        return Result.success(true);
//    }

    @Autowired
    RedisService redisService;

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet(){
        //最后在redis中得到的key为UserKey:id1
        User user = redisService.get(UserKey.getById, "" + 1, User.class);
        return Result.success(user);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet(){
        User user = new User();
        user.setId(1);
        user.setName("zhangsan");
        boolean flag = redisService.set(UserKey.getById, "" + 1, user);
        return Result.success(flag);
    }


    @Autowired
    MQSender mqSender;

    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> myMQ() {
        mqSender.send("this is rabbitMQ");
        return Result.success("rabbitMQ successed");
    }

    @RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> myTopic() {
        mqSender.sendTopic("we are testing rabbitMQ topicQueue");
        return Result.success("rabbitMQ successed");
    }

    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> myFanout() {
        mqSender.sendFanout("we are testing rabbitMQ fanout");
        return Result.success("rabbitMQ successed");
    }

    @RequestMapping("/mq/headers")
    @ResponseBody
    public Result<String> myHeaders() {
        mqSender.sendHeaders("we are testing rabbitMQ headers");
        return Result.success("rabbitMQ successed");
    }

}

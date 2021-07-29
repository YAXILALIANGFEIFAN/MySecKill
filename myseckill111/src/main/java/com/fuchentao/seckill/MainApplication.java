package com.fuchentao.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.fuchentao.seckill.dao")
//在Springboot框架，Mybatis的dao层中只有接口，没有实现类，为了避免报错
//(1)在Springboot的入口，加上@MapperScan注解
//(2)@Autowired注解改为@Autowired(required = false)
public class MainApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MainApplication.class, args);
    }

}

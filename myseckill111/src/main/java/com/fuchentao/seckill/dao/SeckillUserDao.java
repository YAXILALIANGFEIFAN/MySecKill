package com.fuchentao.seckill.dao;

import com.fuchentao.seckill.domain.SeckillUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


/*
@Mapper注解是由Mybatis框架中定义的一个描述数据层接口的注解，
    用于告诉sprigng框架此接口的实现类由Mybatis负责创建，
    并将其实现类对象存储到spring容器中。

需要引入jar包（依赖）,分别是JDBC、MYSQL、MYBATIS。
 */
@Mapper
public interface SeckillUserDao {

    @Select("select * from seckill_user where id = #{id}")
    public SeckillUser getById(@Param("id") long id);


    @Update("update seckill_user set password = #{password} where id = #{id}")
    public void updatePassword(SeckillUser updatedSeckillUser);

}

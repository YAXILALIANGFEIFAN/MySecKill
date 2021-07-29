package com.fuchentao.seckill.service;


import com.fuchentao.seckill.dao.UserDao;
import com.fuchentao.seckill.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired(required = false)
    private UserDao userDao;

    public User getById(int id) {
        return userDao.getById(id);
    }

//    @Transactional
//    //表示这是一个事务
//    public boolean transaction() {
//        User userOne = new User();
//        userOne.setId(2);
//        userOne.setName("2222");
//        userDao.insert(userOne);
//
//        User userTwo = new User();
//        userTwo.setId(1);
//        userTwo.setName("6666");
//        userDao.insert(userTwo);
//
//        return true;
//    }

}

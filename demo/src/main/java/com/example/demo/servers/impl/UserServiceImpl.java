package com.example.demo.servers.impl;

import com.example.demo.dao.UserMapper;
import com.example.demo.pojo.User;
import com.example.demo.servers.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * 业务服务层
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper mapper;

    /**
     * 增
     * @param user
     * @return
     */
    @Override
    public int insertUser(User user) {
        return mapper.insertSelective(user);
    }

    /**
     * 条件查询并列表
     * @param user
     * @return
     */
    @Override
    public List<User> selectUser(User user) {
        Example exp=new Example(User.class);
        if(user!=null){
            Example.Criteria cr=exp.createCriteria();
            if(user.getUsername()!=null&&!user.getUsername().equals("")){
                cr.andLike("username","%"+user.getUsername()+"%");
            }
        }
        return mapper.selectByExample(exp);
    }

    /**
     * 根据用户名查询
     * @param username
     * @return
     */
    @Override
    public User selectUserById(String username) {
        return mapper.selectByPrimaryKey(username);
    }

    /**
     * 更新信息
     * @param user
     * @return
     */
    @Override
    public int updateUser(User user) {
        return mapper.updateByPrimaryKey(user);
    }

    /**
     * 删除
     * @param username
     * @return
     */
    @Override
    public int deleteUserById(String username) {
        Example exp=new Example(User.class);
        if(username!=null){
            Example.Criteria cr=exp.createCriteria();
            if(username!=null&&username.equals("")){
                cr.andEqualTo("username",username);
            }
        }
        return mapper.deleteByPrimaryKey(username);
    }
}

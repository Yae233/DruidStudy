package com.example.demo.servers.interfaces;

import com.example.demo.pojo.User;

import java.util.List;

/**
 * 业务服务层
 */
public interface UserService {
    /**
     * 增加用户
     * @param user
     * @return
     */
    int insertUser(User user);

    /**
     * 查询用户
     * @param user
     * @return
     */
    List<User> selectUser(User user);

    /**
     * 通过主键查询用户
     * @param username
     * @return
     */
    User selectUserById(String username);

    /**
     * 修改用户
     * @param user
     * @return
     */
    int updateUser(User user);

    /**
     * 删除用户
     * @param username
     * @return
     */
    int deleteUserById(String username);
}

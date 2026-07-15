package com.zhangxin.mapper;

import com.zhangxin.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper接口
 */
public interface UserMapper {

    /**
     * 根据ID查询用户
     */
    User findById(@Param("id") Integer id);

    /**
     * 根据用户名查询用户
     */
    User findByUsername(@Param("username") String username);

    /**
     * 查询所有用户
     */
    List<User> findAll();

    /**
     * 添加用户
     */
    int insert(User user);

    /**
     * 更新用户
     */
    int update(User user);

    /**
     * 删除用户
     */
    int delete(@Param("id") Integer id);

    /**
     * 检查用户名是否存在
     */
    int checkUsernameExists(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     */
    int checkEmailExists(@Param("email") String email);
}
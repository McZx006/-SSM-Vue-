package com.zhangxin.service;

import com.zhangxin.entity.User;
import com.zhangxin.mapper.UserMapper;
import com.zhangxin.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务类
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录
     */
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user != null && PasswordUtil.verify(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    /**
     * 用户注册
     */
    @Transactional
    public User register(String username, String password, String email, String phone) {
        // 检查用户名是否存在
        if (userMapper.checkUsernameExists(username) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否存在
        if (email != null && !email.isEmpty() && userMapper.checkEmailExists(email) > 0) {
            throw new RuntimeException("邮箱已被使用");
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.md5(password));
        user.setEmail(email);
        user.setPhone(phone);

        userMapper.insert(user);
        return user;
    }

    /**
     * 根据ID查询用户
     */
    public User findById(Integer id) {
        return userMapper.findById(id);
    }

    /**
     * 查询所有用户
     */
    public List<User> findAll() {
        return userMapper.findAll();
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public void update(User user) {
        userMapper.update(user);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void delete(Integer id) {
        userMapper.delete(id);
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!PasswordUtil.verify(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        user.setPassword(PasswordUtil.md5(newPassword));
        userMapper.update(user);
    }
}
package com.zhangxin.controller;

import com.zhangxin.dto.Result;
import com.zhangxin.entity.User;
import com.zhangxin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 兼容课程要求中的注册接口
 */
@RestController
@RequestMapping("/api")
public class RegistryController {

    @Autowired
    private UserService userService;

    @PostMapping("/registry")
    public Result<User> register(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            String email = params.get("email");
            String phone = params.get("phone");

            if (username == null || password == null) {
                return Result.error("用户名和密码不能为空");
            }

            if (username.length() < 3 || username.length() > 20) {
                return Result.error("用户名长度必须在 3 到 20 个字符之间");
            }

            if (password.length() < 6) {
                return Result.error("密码长度不能少于 6 个字符");
            }

            User user = userService.register(username.trim(), password.trim(), email, phone);
            user.setPassword(null);
            return Result.success("注册成功", user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

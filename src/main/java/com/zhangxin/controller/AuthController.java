package com.zhangxin.controller;

import com.zhangxin.dto.Result;
import com.zhangxin.entity.User;
import com.zhangxin.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器：处理登录、注册、退出和登录状态检查。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录。
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params, HttpSession session) {
        try {
            String username = params.get("username");
            String password = params.get("password");

            if (username == null || username.trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                return Result.error("密码不能为空");
            }

            User user = userService.login(username.trim(), password.trim());
            if (user == null) {
                return Result.error("用户名或密码错误");
            }

            user.setPassword(null);
            session.setAttribute("user", user);

            Map<String, Object> data = new HashMap<>();
            data.put("user", user);
            data.put("token", session.getId());

            return Result.success("登录成功", data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户注册。
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody Map<String, String> params) {
        return doRegister(params);
    }

    /**
     * 用户退出。
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.success("退出成功", null);
    }

    /**
     * 检查登录状态。
     */
    @GetMapping("/check")
    public Result<Map<String, Object>> check(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("loggedIn", true);
        data.put("user", user);
        return Result.success(data);
    }

    private Result<User> doRegister(Map<String, String> params) {
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

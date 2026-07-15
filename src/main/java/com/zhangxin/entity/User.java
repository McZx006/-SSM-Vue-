package com.zhangxin.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private BigDecimal budget;
    private String avatar;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
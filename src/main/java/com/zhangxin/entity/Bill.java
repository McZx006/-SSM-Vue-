package com.zhangxin.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账单实体类
 */
@Data
public class Bill {
    private Integer id;
    private BigDecimal amount;
    private String type; // income or expense
    private Integer categoryId;
    private Integer userId;
    private LocalDate date;
    private String remark;
    private String images;
    private String location;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联对象（不映射到数据库）
    private Category category;
    private User user;
}
package com.zhangxin.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分类实体类
 */
@Data
public class Category {
    private Integer id;
    private String name;
    private String type; // income or expense
    private String icon;
    private String color;
    private Integer userId;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
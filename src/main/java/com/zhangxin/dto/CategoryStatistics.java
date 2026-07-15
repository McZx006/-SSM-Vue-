package com.zhangxin.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 分类统计数据
 */
@Data
public class CategoryStatistics {
    private Integer categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private BigDecimal amount;
    private BigDecimal percentage;
}
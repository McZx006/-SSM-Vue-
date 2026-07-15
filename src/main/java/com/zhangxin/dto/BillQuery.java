package com.zhangxin.dto;

import lombok.Data;

/**
 * 账单查询参数
 */
@Data
public class BillQuery {
    private Integer userId;
    private String type; // income or expense
    private Integer categoryId;
    private String startDate;
    private String endDate;
    private String keyword;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Integer offset; // 分页偏移量
}
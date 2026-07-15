package com.zhangxin.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 月度统计数据
 */
@Data
public class MonthlyStatistics {
    private String month;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal balance;
}
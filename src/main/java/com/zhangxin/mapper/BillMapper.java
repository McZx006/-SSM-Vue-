package com.zhangxin.mapper;

import com.zhangxin.dto.BillQuery;
import com.zhangxin.dto.CategoryStatistics;
import com.zhangxin.dto.MonthlyStatistics;
import com.zhangxin.entity.Bill;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账单Mapper接口
 */
public interface BillMapper {

    /**
     * 根据ID查询账单
     */
    Bill findById(@Param("id") Integer id);

    /**
     * 查询账单列表
     */
    List<Bill> findByQuery(BillQuery query);

    /**
     * 统计账单数量
     */
    int countByQuery(BillQuery query);

    /**
     * 添加账单
     */
    int insert(Bill bill);

    /**
     * 更新账单
     */
    int update(Bill bill);

    /**
     * 删除账单
     */
    int delete(@Param("id") Integer id);

    /**
     * 按月份统计总额
     */
    BigDecimal getTotalByMonth(@Param("userId") Integer userId,
                               @Param("month") String month,
                               @Param("type") String type);

    /**
     * 按分类统计支出占比
     */
    List<CategoryStatistics> getCategoryStatistics(@Param("userId") Integer userId,
                                                   @Param("month") String month,
                                                   @Param("type") String type);

    /**
     * 按月统计收支对比
     */
    List<MonthlyStatistics> getMonthlyStatistics(@Param("userId") Integer userId,
                                                  @Param("year") String year);

    /**
     * 获取最近的账单
     */
    List<Bill> findRecentBills(@Param("userId") Integer userId, @Param("limit") int limit);

    /**
     * 获取月度总支出（用于预算提醒）
     */
    BigDecimal getMonthlyExpense(@Param("userId") Integer userId, @Param("month") String month);
}
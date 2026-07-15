package com.zhangxin.service;

import com.zhangxin.dto.BillQuery;
import com.zhangxin.dto.CategoryStatistics;
import com.zhangxin.dto.MonthlyStatistics;
import com.zhangxin.entity.Bill;
import com.zhangxin.entity.User;
import com.zhangxin.mapper.BillMapper;
import com.zhangxin.mapper.UserMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class BillService {

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private UserMapper userMapper;

    public Bill findById(Integer id) {
        return billMapper.findById(id);
    }

    public List<Bill> findByQuery(BillQuery query) {
        if (query.getPageNum() != null && query.getPageSize() != null) {
            query.setOffset((query.getPageNum() - 1) * query.getPageSize());
        }
        return billMapper.findByQuery(query);
    }

    public int countByQuery(BillQuery query) {
        return billMapper.countByQuery(query);
    }

    @Transactional
    public Bill add(Bill bill) {
        if (bill.getAmount() == null || bill.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("金额必须大于 0");
        }

        if (bill.getDate() == null) {
            bill.setDate(LocalDate.now());
        }

        billMapper.insert(bill);
        return bill;
    }

    @Transactional
    public void update(Bill bill) {
        Bill existing = billMapper.findById(bill.getId());
        if (existing == null) {
            throw new RuntimeException("账单不存在");
        }

        if (bill.getAmount() != null && bill.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("金额必须大于 0");
        }

        billMapper.update(bill);
    }

    @Transactional
    public void delete(Integer id) {
        Bill existing = billMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("账单不存在");
        }

        billMapper.delete(id);
    }

    public BigDecimal getTotalByMonth(Integer userId, String month, String type) {
        return billMapper.getTotalByMonth(userId, month, type);
    }

    public List<CategoryStatistics> getCategoryStatistics(Integer userId, String month, String type) {
        List<CategoryStatistics> statistics = billMapper.getCategoryStatistics(userId, month, type);
        BigDecimal total = statistics.stream()
                .map(CategoryStatistics::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(BigDecimal.ZERO) > 0) {
            for (CategoryStatistics statistic : statistics) {
                BigDecimal percentage = statistic.getAmount()
                        .divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP);
                statistic.setPercentage(percentage);
            }
        }

        return statistics;
    }

    public List<MonthlyStatistics> getMonthlyStatistics(Integer userId, String year) {
        return billMapper.getMonthlyStatistics(userId, year);
    }

    public List<Bill> getRecentBills(Integer userId, int limit) {
        return billMapper.findRecentBills(userId, limit);
    }

    public boolean checkBudgetExceeded(Integer userId, String month) {
        User user = userMapper.findById(userId);
        if (user == null || user.getBudget() == null || user.getBudget().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        BigDecimal monthlyExpense = billMapper.getMonthlyExpense(userId, month);
        return monthlyExpense.compareTo(user.getBudget()) > 0;
    }

    public BigDecimal getBudgetUsage(Integer userId, String month) {
        User user = userMapper.findById(userId);
        if (user == null || user.getBudget() == null || user.getBudget().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal monthlyExpense = billMapper.getMonthlyExpense(userId, month);
        return monthlyExpense.divide(user.getBudget(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public List<Bill> exportBills(Integer userId, String startDate, String endDate) {
        BillQuery query = new BillQuery();
        query.setUserId(userId);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setPageSize(null);
        return billMapper.findByQuery(query);
    }

    public byte[] exportBillsExcel(Integer userId, String startDate, String endDate) {
        List<Bill> bills = exportBills(userId, startDate, endDate);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Bills");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("日期");
            headerRow.createCell(1).setCellValue("类型");
            headerRow.createCell(2).setCellValue("分类");
            headerRow.createCell(3).setCellValue("金额");
            headerRow.createCell(4).setCellValue("备注");

            for (int i = 0; i < bills.size(); i++) {
                Bill bill = bills.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(bill.getDate() == null ? "" : bill.getDate().toString());
                row.createCell(1).setCellValue("income".equals(bill.getType()) ? "收入" : "支出");
                row.createCell(2).setCellValue(bill.getCategory() == null ? "" : bill.getCategory().getName());
                row.createCell(3).setCellValue(bill.getAmount() == null ? 0D : bill.getAmount().doubleValue());
                row.createCell(4).setCellValue(bill.getRemark() == null ? "" : bill.getRemark());
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("导出 Excel 失败", e);
        }
    }
}

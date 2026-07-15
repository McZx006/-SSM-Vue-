package com.zhangxin.controller;

import com.zhangxin.dto.CategoryStatistics;
import com.zhangxin.dto.MonthlyStatistics;
import com.zhangxin.dto.Result;
import com.zhangxin.entity.User;
import com.zhangxin.service.BillService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private BillService billService;

    @GetMapping("/total")
    public Result<Map<String, BigDecimal>> getTotalByMonth(
            @RequestParam(name = "month") String month,
            @RequestParam(name = "type", required = false) String type,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录");
        }

        BigDecimal income = billService.getTotalByMonth(user.getId(), month, "income");
        BigDecimal expense = billService.getTotalByMonth(user.getId(), month, "expense");

        Map<String, BigDecimal> data = new HashMap<>();
        if ("income".equals(type)) {
            data.put("total", income);
        } else if ("expense".equals(type)) {
            data.put("total", expense);
        } else {
            data.put("income", income);
            data.put("expense", expense);
            data.put("balance", income.subtract(expense));
        }

        return Result.success(data);
    }

    @GetMapping("/category")
    public Result<List<CategoryStatistics>> getCategoryStatistics(
            @RequestParam(name = "month") String month,
            @RequestParam(name = "type", required = false, defaultValue = "expense") String type,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录");
        }

        return Result.success(billService.getCategoryStatistics(user.getId(), month, type));
    }

    @GetMapping("/monthly")
    public Result<List<MonthlyStatistics>> getMonthlyStatistics(
            @RequestParam(name = "year") String year,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录");
        }

        return Result.success(billService.getMonthlyStatistics(user.getId(), year));
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard(
            @RequestParam(name = "month", required = false) String month,
            @RequestParam(name = "year", required = false) String year,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录");
        }

        String currentMonth = month != null && !month.isBlank()
                ? month
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String currentYear = year != null && !year.isBlank()
                ? year
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));

        BigDecimal monthIncome = billService.getTotalByMonth(user.getId(), currentMonth, "income");
        BigDecimal monthExpense = billService.getTotalByMonth(user.getId(), currentMonth, "expense");
        List<MonthlyStatistics> yearStatistics = billService.getMonthlyStatistics(user.getId(), currentYear);
        List<CategoryStatistics> categoryStatistics = billService.getCategoryStatistics(user.getId(), currentMonth, "expense");

        BigDecimal yearIncome = yearStatistics.stream()
                .map(MonthlyStatistics::getIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal yearExpense = yearStatistics.stream()
                .map(MonthlyStatistics::getExpense)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> data = new HashMap<>();
        data.put("monthIncome", monthIncome);
        data.put("monthExpense", monthExpense);
        data.put("monthBalance", monthIncome.subtract(monthExpense));
        data.put("yearIncome", yearIncome);
        data.put("yearExpense", yearExpense);
        data.put("yearBalance", yearIncome.subtract(yearExpense));
        data.put("categoryStatistics", categoryStatistics);
        data.put("monthlyStatistics", yearStatistics);
        data.put("budgetUsage", billService.getBudgetUsage(user.getId(), currentMonth));
        data.put("budgetExceeded", billService.checkBudgetExceeded(user.getId(), currentMonth));

        return Result.success(data);
    }

    @GetMapping("/export")
    public void exportBills(
            @RequestParam(name = "startDate") String startDate,
            @RequestParam(name = "endDate") String endDate,
            HttpSession session,
            HttpServletResponse response
    ) throws IOException {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            return;
        }

        byte[] bytes = billService.exportBillsExcel(user.getId(), startDate, endDate);
        String fileName = URLEncoder.encode(
                "账单数据_" + startDate + "_" + endDate + ".xlsx",
                StandardCharsets.UTF_8
        ).replace("+", "%20");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }
}

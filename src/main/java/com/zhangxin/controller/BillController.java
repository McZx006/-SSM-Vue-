package com.zhangxin.controller;

import com.zhangxin.dto.BillQuery;
import com.zhangxin.dto.Result;
import com.zhangxin.entity.Bill;
import com.zhangxin.entity.User;
import com.zhangxin.service.BillService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 账单控制器：提供账单查询、新增、修改、删除和最近账单接口。
 */
@RestController
@RequestMapping("/api/bills")
public class BillController {

    @Autowired
    private BillService billService;

    @GetMapping
    public Result<Map<String, Object>> findByQuery(BillQuery query, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录");
        }

        query.setUserId(user.getId());
        List<Bill> bills = billService.findByQuery(query);
        int total = billService.countByQuery(query);

        Map<String, Object> data = new HashMap<>();
        data.put("list", bills);
        data.put("total", total);
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<Bill> findById(@PathVariable("id") Integer id) {
        Bill bill = billService.findById(id);
        if (bill == null) {
            return Result.error("账单不存在");
        }
        return Result.success(bill);
    }

    @PostMapping
    public Result<Bill> add(@RequestBody Bill bill, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return Result.error(401, "未登录");
            }

            bill.setUserId(user.getId());
            Bill saved = billService.add(bill);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Integer id, @RequestBody Bill bill) {
        try {
            bill.setId(id);
            billService.update(bill);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Integer id) {
        try {
            billService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/recent")
    public Result<List<Bill>> getRecentBills(
            HttpSession session,
            @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录");
        }

        List<Bill> bills = billService.getRecentBills(user.getId(), limit);
        return Result.success(bills);
    }
}

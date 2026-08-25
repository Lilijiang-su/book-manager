package com.bookmanager.controller;

import com.bookmanager.common.JwtUtil;
import com.bookmanager.common.PageResult;
import com.bookmanager.common.Result;
import com.bookmanager.entity.BorrowRecord;
import com.bookmanager.service.BorrowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/add")
    public Result<?> borrow(@RequestParam Integer bookId, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Integer userId = jwtUtil.getUserId(token);
        borrowService.borrow(userId, bookId);
        return Result.success("借书成功");
    }

    @PostMapping("/return/{id}")
    public Result<?> returnBook(@PathVariable Integer id, HttpServletRequest request) {
        borrowService.returnBook(id, getCurrentRole(request), getCurrentUserId(request));
        return Result.success("还书成功");
    }

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletRequest request) {
        // 普通用户只能看自己的记录，管理员可看全部
        if (isAdmin(request)) {
            if (status == null && startDate == null && endDate == null) {
                return Result.success(borrowService.findAll());
            }
            return Result.success(borrowService.findWithFilters(status, startDate, endDate));
        }
        Integer userId = getCurrentUserId(request);
        List<BorrowRecord> records = borrowService.findByUserIdAndFilters(userId, status, startDate, endDate);
        return Result.success(records);
    }

    @GetMapping("/page")
    public Result<?> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletRequest request) {
        if (isAdmin(request)) {
            var records = borrowService.findPage(page, pageSize, status, startDate, endDate);
            long total = borrowService.countWithFilters(status, startDate, endDate);
            return Result.success(new PageResult<>(records, total, page, pageSize));
        }
        Integer userId = getCurrentUserId(request);
        var records = borrowService.findPageByUserId(userId, page, pageSize, status, startDate, endDate);
        long total = borrowService.countByUserIdWithFilters(userId, status, startDate, endDate);
        return Result.success(new PageResult<>(records, total, page, pageSize));
    }

    @GetMapping("/user")
    public Result<?> userRecords(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Integer userId = jwtUtil.getUserId(token);
        return Result.success(borrowService.findByUserId(userId));
    }

    @GetMapping("/status/{status}")
    public Result<?> byStatus(@PathVariable String status, HttpServletRequest request) {
        if (isAdmin(request)) {
            return Result.success(borrowService.findByStatus(status));
        }
        Integer userId = getCurrentUserId(request);
        return Result.success(borrowService.findByUserIdAndStatus(userId, status));
    }

    private boolean isAdmin(HttpServletRequest request) {
        return "admin".equals(request.getAttribute("role"));
    }

    private Integer getCurrentUserId(HttpServletRequest request) {
        return (Integer) request.getAttribute("userId");
    }

    private String getCurrentRole(HttpServletRequest request) {
        return (String) request.getAttribute("role");
    }
}

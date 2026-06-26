package com.bookmanager.controller;

import com.bookmanager.common.JwtUtil;
import com.bookmanager.common.PageResult;
import com.bookmanager.common.Result;
import com.bookmanager.service.BorrowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Result<?> returnBook(@PathVariable Integer id) {
        borrowService.returnBook(id);
        return Result.success("还书成功");
    }

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        if (status == null && startDate == null && endDate == null) {
            return Result.success(borrowService.findAll());
        }
        return Result.success(borrowService.findWithFilters(status, startDate, endDate));
    }

    @GetMapping("/page")
    public Result<?> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        var records = borrowService.findPage(page, pageSize, status, startDate, endDate);
        long total = borrowService.countWithFilters(status, startDate, endDate);
        return Result.success(new PageResult<>(records, total, page, pageSize));
    }

    @GetMapping("/user")
    public Result<?> userRecords(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Integer userId = jwtUtil.getUserId(token);
        return Result.success(borrowService.findByUserId(userId));
    }

    @GetMapping("/status/{status}")
    public Result<?> byStatus(@PathVariable String status) {
        return Result.success(borrowService.findByStatus(status));
    }
}

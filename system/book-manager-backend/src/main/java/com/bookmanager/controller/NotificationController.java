package com.bookmanager.controller;

import com.bookmanager.common.JwtUtil;
import com.bookmanager.common.Result;
import com.bookmanager.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notice")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request) {
        Integer userId = getUserId(request);
        return Result.success(notificationService.findByUserId(userId));
    }

    @GetMapping("/unread-count")
    public Result<?> unreadCount(HttpServletRequest request) {
        Integer userId = getUserId(request);
        return Result.success(notificationService.countUnread(userId));
    }

    @PutMapping("/read/{id}")
    public Result<?> markRead(@PathVariable Integer id) {
        notificationService.markRead(id);
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<?> markAllRead(HttpServletRequest request) {
        Integer userId = getUserId(request);
        notificationService.markAllRead(userId);
        return Result.success();
    }

    private Integer getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.getUserId(token);
    }
}

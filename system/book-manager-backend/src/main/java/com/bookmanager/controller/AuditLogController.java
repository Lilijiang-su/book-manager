package com.bookmanager.controller;

import com.bookmanager.common.PageResult;
import com.bookmanager.common.Result;
import com.bookmanager.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String operatorName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(auditLogService.findWithFilters(action, targetType, operatorName, startDate, endDate));
    }

    @GetMapping("/page")
    public Result<?> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String operatorName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        var records = auditLogService.findPage(page, pageSize, action, targetType, operatorName, startDate, endDate);
        long total = auditLogService.countWithFilters(action, targetType, operatorName, startDate, endDate);
        return Result.success(new PageResult<>(records, total, page, pageSize));
    }
}

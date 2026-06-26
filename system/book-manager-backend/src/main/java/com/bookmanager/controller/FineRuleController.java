package com.bookmanager.controller;

import com.bookmanager.common.Result;
import com.bookmanager.entity.FineRule;
import com.bookmanager.service.FineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fine-rule")
public class FineRuleController {

    @Autowired
    private FineService fineService;

    @GetMapping
    public Result<?> getActive() {
        return Result.success(fineService.getActiveRule());
    }

    @PutMapping
    public Result<?> update(@RequestBody FineRule rule) {
        fineService.updateRule(rule);
        return Result.success("更新成功");
    }
}

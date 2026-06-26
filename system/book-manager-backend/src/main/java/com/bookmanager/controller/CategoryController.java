package com.bookmanager.controller;

import com.bookmanager.common.Result;
import com.bookmanager.entity.Category;
import com.bookmanager.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /** 返回扁平列表（向后兼容） */
    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(categoryService.findAll());
    }

    /** 返回树形结构 */
    @GetMapping("/tree")
    public Result<?> tree() {
        return Result.success(categoryService.findTree());
    }

    /** 只返回启用的分类 */
    @GetMapping("/enabled")
    public Result<?> enabled() {
        return Result.success(categoryService.findEnabled());
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Category category) {
        categoryService.insert(category);
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody Category category) {
        categoryService.update(category);
        return Result.success("更新成功");
    }

    @PutMapping("/status")
    public Result<?> updateStatus(@RequestParam Integer id, @RequestParam Integer status) {
        categoryService.updateStatus(id, status);
        return Result.success();
    }

    @PutMapping("/sort")
    public Result<?> updateSort(@RequestParam Integer id, @RequestParam Integer sortOrder) {
        categoryService.updateSortOrder(id, sortOrder);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        categoryService.deleteById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/batch-delete")
    public Result<?> batchDelete(@RequestBody Map<String, List<Integer>> body) {
        categoryService.batchDelete(body.get("ids"));
        return Result.success("批量删除成功");
    }
}

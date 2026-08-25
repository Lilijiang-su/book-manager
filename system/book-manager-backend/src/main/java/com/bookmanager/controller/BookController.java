package com.bookmanager.controller;

import com.bookmanager.common.PageResult;
import com.bookmanager.common.Result;
import com.bookmanager.entity.Book;
import com.bookmanager.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/book")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(bookService.findAll());
    }

    @GetMapping("/page")
    public Result<?> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Book> records = bookService.findPage(page, pageSize, keyword, categoryId, stockStatus, startDate, endDate);
        long total = bookService.countWithFilters(keyword, categoryId, stockStatus, startDate, endDate);
        return Result.success(new PageResult<>(records, total, page, pageSize));
    }

    @GetMapping("/search")
    public Result<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(bookService.findWithFilters(keyword, categoryId, stockStatus, startDate, endDate));
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Integer id) {
        return Result.success(bookService.findById(id));
    }

    @GetMapping("/category/{categoryId}")
    public Result<?> byCategory(@PathVariable Integer categoryId) {
        return Result.success(bookService.findByCategoryId(categoryId));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Book book) {
        bookService.insert(book);
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody Book book) {
        bookService.update(book);
        return Result.success("更新成功");
    }

    @PutMapping("/status")
    public Result<?> updateStatus(@RequestParam Integer id, @RequestParam Integer status) {
        bookService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        bookService.deleteById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/batch-delete")
    public Result<?> batchDelete(@RequestBody Map<String, List<Integer>> body) {
        bookService.batchDelete(body.get("ids"));
        return Result.success("批量删除成功");
    }

    @PutMapping("/batch-category")
    public Result<?> batchUpdateCategory(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("ids");
        Integer categoryId = (Integer) body.get("categoryId");
        bookService.batchUpdateCategory(ids, categoryId);
        return Result.success("批量修改分类成功");
    }
}

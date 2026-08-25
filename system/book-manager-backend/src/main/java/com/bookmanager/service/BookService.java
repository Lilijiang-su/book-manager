package com.bookmanager.service;

import com.bookmanager.entity.Book;
import com.bookmanager.mapper.BookMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private AuditLogService auditLogService;

    public Book findById(Integer id) {
        return bookMapper.findById(id);
    }

    public List<Book> findAll() {
        return bookMapper.findAll();
    }

    public List<Book> search(String keyword) {
        return bookMapper.search(keyword);
    }

    public List<Book> findByCategoryId(Integer categoryId) {
        return bookMapper.findByCategoryId(categoryId);
    }

    public List<Book> findWithFilters(String keyword, Integer categoryId,
                                       String stockStatus, String startDate, String endDate) {
        return bookMapper.findWithFilters(keyword, categoryId, stockStatus, startDate, endDate);
    }

    public List<Book> findPage(int page, int pageSize, String keyword, Integer categoryId,
                                String stockStatus, String startDate, String endDate) {
        int offset = (page - 1) * pageSize;
        return bookMapper.findPage(offset, pageSize, keyword, categoryId, stockStatus, startDate, endDate);
    }

    public long countWithFilters(String keyword, Integer categoryId,
                                  String stockStatus, String startDate, String endDate) {
        return bookMapper.countWithFilters(keyword, categoryId, stockStatus, startDate, endDate);
    }

    public void insert(Book book) {
        book.setTotal(book.getStock());
        bookMapper.insert(book);
        auditLog(book.getId(), "CREATE", "BOOK", "添加图书《" + book.getName() + "》");
    }

    public void update(Book book) {
        bookMapper.update(book);
        auditLog(book.getId(), "UPDATE", "BOOK", "更新图书《" + book.getName() + "》");
    }

    public void updateStatus(Integer id, Integer status) {
        bookMapper.updateStatus(id, status);
        Book book = bookMapper.findById(id);
        auditLog(id, "UPDATE", "BOOK", (status == 1 ? "上架" : "下架") + "《" + (book != null ? book.getName() : "") + "》");
    }

    public void deleteById(Integer id) {
        Book book = bookMapper.findById(id);
        bookMapper.deleteById(id);
        auditLog(id, "DELETE", "BOOK", "删除图书《" + (book != null ? book.getName() : "") + "》");
    }

    public void batchDelete(List<Integer> ids) {
        bookMapper.batchDelete(ids);
        auditLog(null, "DELETE", "BOOK", "批量删除 " + ids.size() + " 本图书");
    }

    public void batchUpdateCategory(List<Integer> ids, Integer categoryId) {
        bookMapper.batchUpdateCategory(ids, categoryId);
        auditLog(null, "UPDATE", "BOOK", "批量修改 " + ids.size() + " 本图书分类为ID=" + categoryId);
    }

    private void auditLog(Integer targetId, String action, String targetType, String detail) {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                Object userId = attrs.getRequest().getAttribute("userId");
                Object username = attrs.getRequest().getAttribute("username");
                String ip = attrs.getRequest().getRemoteAddr();
                if (username != null) {
                    auditLogService.save(
                        userId != null ? (Integer) userId : 0,
                        username.toString(), action, targetType, targetId, detail, ip
                    );
                }
            }
        } catch (Exception ignored) {}
    }
}

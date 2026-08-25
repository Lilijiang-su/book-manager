package com.bookmanager.service;

import com.bookmanager.entity.BorrowRecord;
import com.bookmanager.mapper.BookMapper;
import com.bookmanager.mapper.BorrowRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BorrowService {

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private FineService fineService;

    @Transactional
    public void borrow(Integer userId, Integer bookId) {
        long count = borrowRecordMapper.countByUserIdAndStatus(userId, "borrowing");
        if (count >= 5) {
            throw new RuntimeException("每人最多同时借阅5本书");
        }
        var book = bookMapper.findById(bookId);
        if (book == null || book.getStock() <= 0) {
            throw new RuntimeException("该书库存不足");
        }
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        borrowRecordMapper.insert(record);
        bookMapper.updateStock(bookId, -1);

        // 审计日志
        String username = getCurrentUsername();
        auditLogService.save(userId, username, "CREATE", "BORROW", record.getId(),
            "借阅《" + book.getName() + "》", getClientIp());

        // 通知
        notificationService.notify(userId, "borrow", "借书成功",
            "您已成功借阅《" + book.getName() + "》，请在30天内归还", record.getId());
    }

    @Transactional
    public void returnBook(Integer recordId) {
        returnBook(recordId, null, null);
    }

    /**
     * 还书，带权限校验：普通用户只能归还自己的借阅记录。
     */
    @Transactional
    public void returnBook(Integer recordId, String role, Integer currentUserId) {
        BorrowRecord record = borrowRecordMapper.findById(recordId);
        if (record == null) {
            throw new RuntimeException("记录不存在");
        }
        // 非管理员只能归还自己的记录
        if (!"admin".equals(role) && currentUserId != null && !record.getUserId().equals(currentUserId)) {
            throw new RuntimeException("无权操作他人的借阅记录");
        }
        LocalDateTime now = LocalDateTime.now();
        boolean isOverdue = now.isAfter(record.getDueTime());

        // 计算罚款（逾期按实际超期天数计算）
        BigDecimal fineAmount = BigDecimal.ZERO;
        if (isOverdue) {
            fineAmount = fineService.calculateFine(record.getDueTime(), now);
        }

        borrowRecordMapper.returnBook(recordId, now, "returned");
        bookMapper.updateStock(record.getBookId(), 1);

        // 审计日志
        String username = getCurrentUsername();
        auditLogService.save(record.getUserId(), username, "UPDATE", "BORROW", recordId,
            "归还《" + record.getBookName() + "》" + (fineAmount.compareTo(BigDecimal.ZERO) > 0 ? "，罚款￥" + fineAmount : ""),
            getClientIp());

        // 通知
        String notifyContent = "您已归还《" + record.getBookName() + "》";
        if (fineAmount.compareTo(BigDecimal.ZERO) > 0) {
            notifyContent += "，逾期罚款 ￥" + fineAmount;
        }
        notificationService.notify(record.getUserId(), "return", "还书成功", notifyContent, recordId);
    }

    public List<BorrowRecord> findByUserId(Integer userId) {
        return borrowRecordMapper.findByUserId(userId);
    }

    public List<BorrowRecord> findByUserIdAndStatus(Integer userId, String status) {
        return borrowRecordMapper.findByUserIdAndStatus(userId, status);
    }

    public List<BorrowRecord> findByUserIdAndFilters(Integer userId, String status, String startDate, String endDate) {
        return borrowRecordMapper.findByUserIdAndFilters(userId, status, startDate, endDate);
    }

    public List<BorrowRecord> findPageByUserId(Integer userId, int page, int pageSize, String status, String startDate, String endDate) {
        return borrowRecordMapper.findPageByUserId((page - 1) * pageSize, pageSize, userId, status, startDate, endDate);
    }

    public long countByUserIdWithFilters(Integer userId, String status, String startDate, String endDate) {
        return borrowRecordMapper.countByUserIdWithFilters(userId, status, startDate, endDate);
    }

    public List<BorrowRecord> findAll() {
        return borrowRecordMapper.findAll();
    }

    public List<BorrowRecord> findByStatus(String status) {
        return borrowRecordMapper.findByStatus(status);
    }

    public List<BorrowRecord> findWithFilters(String status, String startDate, String endDate) {
        return borrowRecordMapper.findWithFilters(status, startDate, endDate);
    }

    public List<BorrowRecord> findPage(int page, int pageSize, String status, String startDate, String endDate) {
        return borrowRecordMapper.findPage((page - 1) * pageSize, pageSize, status, startDate, endDate);
    }

    public long countWithFilters(String status, String startDate, String endDate) {
        return borrowRecordMapper.countWithFilters(status, startDate, endDate);
    }

    private String getCurrentUsername() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                Object u = attrs.getRequest().getAttribute("username");
                if (u != null) return u.toString();
            }
        } catch (Exception ignored) {}
        return "系统";
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest().getRemoteAddr();
            }
        } catch (Exception ignored) {}
        return "127.0.0.1";
    }
}

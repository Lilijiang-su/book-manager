package com.bookmanager.scheduler;

import com.bookmanager.entity.BorrowRecord;
import com.bookmanager.entity.User;
import com.bookmanager.mapper.BorrowRecordMapper;
import com.bookmanager.mapper.UserMapper;
import com.bookmanager.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class BorrowReminderScheduler {

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserMapper userMapper;

    /** 每天早上 8 点检查即将到期的图书 */
    @Scheduled(cron = "0 0 8 * * ?")
    public void checkDueSoon() {
        // 找出所有"借阅中"的记录
        List<BorrowRecord> records = borrowRecordMapper.findByStatus("borrowing");
        LocalDateTime now = LocalDateTime.now();

        for (BorrowRecord r : records) {
            long daysUntilDue = ChronoUnit.DAYS.between(now, r.getDueTime());
            if (daysUntilDue >= 1 && daysUntilDue <= 3) {
                notificationService.notify(
                    r.getUserId(),
                    "overdue_warn",
                    "借阅即将到期",
                    "您借阅的《" + r.getBookName() + "》将在 " + daysUntilDue + " 天后到期，请及时归还",
                    r.getId()
                );
            }
        }
    }
}

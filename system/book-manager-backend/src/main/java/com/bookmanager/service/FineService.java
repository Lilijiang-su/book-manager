package com.bookmanager.service;

import com.bookmanager.entity.FineRule;
import com.bookmanager.mapper.FineRuleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class FineService {

    @Autowired
    private FineRuleMapper fineRuleMapper;

    public FineRule getActiveRule() {
        return fineRuleMapper.findActive();
    }

    public void updateRule(FineRule rule) {
        fineRuleMapper.update(rule);
    }

    /** 计算逾期罚款 */
    public BigDecimal calculateFine(LocalDateTime dueTime, LocalDateTime returnTime) {
        long overdueDays = ChronoUnit.DAYS.between(dueTime, returnTime);
        if (overdueDays <= 0) return BigDecimal.ZERO;
        FineRule rule = fineRuleMapper.findActive();
        if (rule == null) return BigDecimal.ZERO;
        BigDecimal fine = rule.getFinePerDay().multiply(BigDecimal.valueOf(overdueDays));
        return fine.compareTo(rule.getMaxFine()) > 0 ? rule.getMaxFine() : fine;
    }
}

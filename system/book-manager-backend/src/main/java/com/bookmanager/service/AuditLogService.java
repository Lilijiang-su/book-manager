package com.bookmanager.service;

import com.bookmanager.entity.AuditLog;
import com.bookmanager.mapper.AuditLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogMapper auditLogMapper;

    public void save(Integer operatorId, String operatorName, String action,
                     String targetType, Integer targetId, String detail, String ip) {
        AuditLog log = new AuditLog();
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setIp(ip);
        auditLogMapper.insert(log);
    }

    public List<AuditLog> findAll() {
        return auditLogMapper.findAll();
    }

    public List<AuditLog> findWithFilters(String action, String targetType,
                                           String operatorName, String startDate, String endDate) {
        return auditLogMapper.findWithFilters(action, targetType, operatorName, startDate, endDate);
    }

    public List<AuditLog> findPage(int page, int pageSize, String action, String targetType,
                                    String operatorName, String startDate, String endDate) {
        return auditLogMapper.findPage((page - 1) * pageSize, pageSize, action, targetType, operatorName, startDate, endDate);
    }

    public long countWithFilters(String action, String targetType,
                                  String operatorName, String startDate, String endDate) {
        return auditLogMapper.countWithFilters(action, targetType, operatorName, startDate, endDate);
    }
}

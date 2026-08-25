package com.bookmanager.mapper;

import com.bookmanager.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuditLogMapper {
    int insert(AuditLog log);
    List<AuditLog> findAll();
    List<AuditLog> findWithFilters(@Param("action") String action,
                                    @Param("targetType") String targetType,
                                    @Param("operatorName") String operatorName,
                                    @Param("startDate") String startDate,
                                    @Param("endDate") String endDate);

    // 分页
    List<AuditLog> findPage(@Param("offset") int offset, @Param("limit") int limit,
                             @Param("action") String action,
                             @Param("targetType") String targetType,
                             @Param("operatorName") String operatorName,
                             @Param("startDate") String startDate,
                             @Param("endDate") String endDate);
    long countWithFilters(@Param("action") String action,
                         @Param("targetType") String targetType,
                         @Param("operatorName") String operatorName,
                         @Param("startDate") String startDate,
                         @Param("endDate") String endDate);
}

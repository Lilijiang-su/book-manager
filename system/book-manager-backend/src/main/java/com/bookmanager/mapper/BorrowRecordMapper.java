package com.bookmanager.mapper;

import com.bookmanager.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BorrowRecordMapper {
    BorrowRecord findById(@Param("id") Integer id);
    List<BorrowRecord> findByUserId(@Param("userId") Integer userId);
    List<BorrowRecord> findAll();
    List<BorrowRecord> findByStatus(@Param("status") String status);
    List<BorrowRecord> findWithFilters(@Param("status") String status,
                                        @Param("startDate") String startDate,
                                        @Param("endDate") String endDate);

    // 分页
    List<BorrowRecord> findPage(@Param("offset") int offset, @Param("limit") int limit,
                                 @Param("status") String status,
                                 @Param("startDate") String startDate,
                                 @Param("endDate") String endDate);
    long countWithFilters(@Param("status") String status,
                         @Param("startDate") String startDate,
                         @Param("endDate") String endDate);

    int insert(BorrowRecord record);
    int update(BorrowRecord record);
    int returnBook(@Param("id") Integer id, @Param("returnTime") LocalDateTime returnTime, @Param("status") String status);
    long countByUserIdAndStatus(@Param("userId") Integer userId, @Param("status") String status);
}

package com.bookmanager.mapper;

import com.bookmanager.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {
    int insert(Notification notification);
    List<Notification> findByUserId(@Param("userId") Integer userId);
    long countUnread(@Param("userId") Integer userId);
    int markRead(@Param("id") Integer id);
    int markAllRead(@Param("userId") Integer userId);
}

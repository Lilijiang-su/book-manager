package com.bookmanager.service;

import com.bookmanager.entity.Notification;
import com.bookmanager.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    public void notify(Integer userId, String type, String title, String content, Integer relatedId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRelatedId(relatedId);
        notificationMapper.insert(n);
    }

    /** 通知所有管理员 */
    public void notifyAdmins(List<Integer> adminIds, String type, String title, String content, Integer relatedId) {
        for (Integer adminId : adminIds) {
            notify(adminId, type, title, content, relatedId);
        }
    }

    public List<Notification> findByUserId(Integer userId) {
        return notificationMapper.findByUserId(userId);
    }

    public long countUnread(Integer userId) {
        return notificationMapper.countUnread(userId);
    }

    public void markRead(Integer id) {
        notificationMapper.markRead(id);
    }

    public void markAllRead(Integer userId) {
        notificationMapper.markAllRead(userId);
    }
}

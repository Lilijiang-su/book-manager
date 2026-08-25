package com.bookmanager.mapper;

import com.bookmanager.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    User findByUsername(@Param("username") String username);
    User findById(@Param("id") Integer id);
    List<User> findAll();

    // 分页
    List<User> findPage(@Param("offset") int offset, @Param("limit") int limit);
    long count();

    int insert(User user);
    int update(User user);
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);
    int updateAvatar(@Param("id") Integer id, @Param("avatar") String avatar);
    int updatePassword(@Param("id") Integer id, @Param("password") String password);
    int deleteById(@Param("id") Integer id);
    int batchDelete(@Param("ids") List<Integer> ids);
}

package com.bookmanager.mapper;

import com.bookmanager.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {
    Category findById(@Param("id") Integer id);
    List<Category> findAll();
    List<Category> findByParentId(@Param("parentId") Integer parentId);
    int insert(Category category);
    int update(Category category);
    int updateSortOrder(@Param("id") Integer id, @Param("sortOrder") Integer sortOrder);
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);
    int deleteById(@Param("id") Integer id);
    int batchDelete(@Param("ids") List<Integer> ids);
}

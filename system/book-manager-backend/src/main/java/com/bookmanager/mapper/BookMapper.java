package com.bookmanager.mapper;

import com.bookmanager.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookMapper {
    Book findById(@Param("id") Integer id);
    List<Book> findAll();
    List<Book> search(@Param("keyword") String keyword);
    List<Book> findByCategoryId(@Param("categoryId") Integer categoryId);
    long countByCategoryId(@Param("categoryId") Integer categoryId);
    List<Book> findWithFilters(@Param("keyword") String keyword,
                               @Param("categoryId") Integer categoryId,
                               @Param("stockStatus") String stockStatus,
                               @Param("startDate") String startDate,
                               @Param("endDate") String endDate);

    // 分页查询
    List<Book> findPage(@Param("offset") int offset, @Param("limit") int limit,
                        @Param("keyword") String keyword,
                        @Param("categoryId") Integer categoryId,
                        @Param("stockStatus") String stockStatus,
                        @Param("startDate") String startDate,
                        @Param("endDate") String endDate);
    long countWithFilters(@Param("keyword") String keyword,
                         @Param("categoryId") Integer categoryId,
                         @Param("stockStatus") String stockStatus,
                         @Param("startDate") String startDate,
                         @Param("endDate") String endDate);

    int insert(Book book);
    int update(Book book);
    int updateStock(@Param("id") Integer id, @Param("delta") int delta);
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);
    int deleteById(@Param("id") Integer id);
    int batchDelete(@Param("ids") List<Integer> ids);
    int batchUpdateCategory(@Param("ids") List<Integer> ids, @Param("categoryId") Integer categoryId);
}

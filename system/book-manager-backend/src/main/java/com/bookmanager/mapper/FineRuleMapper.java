package com.bookmanager.mapper;

import com.bookmanager.entity.FineRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FineRuleMapper {
    FineRule findActive();
    int update(FineRule rule);
}

package com.campus.activity.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.activity.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

    @Select("SELECT a.*, v.name AS venue_name, v.location AS venue_location " +
            "FROM activity a LEFT JOIN venue v ON a.venue_id = v.venue_id " +
            "${ew.customSqlSegment}")
    List<Activity> selectActivityWithVenue(@Param(Constants.WRAPPER) QueryWrapper<Activity> wrapper);

    /** 分页版 */
    @Select("SELECT a.*, v.name AS venue_name, v.location AS venue_location " +
            "FROM activity a LEFT JOIN venue v ON a.venue_id = v.venue_id " +
            "${ew.customSqlSegment}")
    IPage<Activity> selectActivityPage(Page<Activity> page,
                                       @Param(Constants.WRAPPER) QueryWrapper<Activity> wrapper);
}

package com.campus.activity.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.activity.entity.Registration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {

    @Select("SELECT r.*, a.name AS activity_name, a.activity_date, a.type AS activity_type, " +
            "p.name AS person_name " +
            "FROM registration r " +
            "LEFT JOIN activity a ON r.activity_id = a.activity_id " +
            "LEFT JOIN person p ON r.person_id = p.person_id " +
            "${ew.customSqlSegment}")
    List<Registration> selectRegistrationWithDetails(@Param(Constants.WRAPPER) QueryWrapper<Registration> wrapper);

    /** 分页版 */
    @Select("SELECT r.*, a.name AS activity_name, a.activity_date, a.type AS activity_type, " +
            "p.name AS person_name " +
            "FROM registration r " +
            "LEFT JOIN activity a ON r.activity_id = a.activity_id " +
            "LEFT JOIN person p ON r.person_id = p.person_id " +
            "${ew.customSqlSegment}")
    IPage<Registration> selectRegistrationPage(Page<Registration> page,
                                               @Param(Constants.WRAPPER) QueryWrapper<Registration> wrapper);
}

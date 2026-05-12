package com.campus.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.activity.common.PageResult;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.Registration;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.mapper.RegistrationMapper;
import com.campus.activity.service.RegistrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationMapper registrationMapper;
    private final ActivityMapper activityMapper;

    public RegistrationServiceImpl(RegistrationMapper registrationMapper,
                                   ActivityMapper activityMapper) {
        this.registrationMapper = registrationMapper;
        this.activityMapper = activityMapper;
    }

    @Override
    @Transactional
    public boolean register(Integer activityId, Integer personId) {
        if (isRegistered(activityId, personId)) return false;

        // 检查活动是否存在且名额未满
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) return false;
        if (!"已通过".equals(activity.getStatus())) return false;
        if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) return false;

        Registration reg = new Registration();
        reg.setActivityId(activityId);
        reg.setPersonId(personId);
        reg.setStatus("已报名");
        int rows = registrationMapper.insert(reg);
        if (rows > 0) {
            // 原子更新活动报名人数 +1
            UpdateWrapper<Activity> uw = new UpdateWrapper<>();
            uw.setSql("current_participants = current_participants + 1")
              .eq("activity_id", activityId);
            activityMapper.update(null, uw);
        }
        return rows > 0;
    }

    @Override
    @Transactional
    public boolean cancel(Integer regId, Integer personId) {
        QueryWrapper<Registration> qw = new QueryWrapper<>();
        qw.eq("reg_id", regId).eq("person_id", personId);
        Registration reg = registrationMapper.selectOne(qw);
        if (reg == null || "已取消".equals(reg.getStatus())) return false;
        reg.setStatus("已取消");
        int rows = registrationMapper.updateById(reg);
        if (rows > 0) {
            // 原子更新活动报名人数 -1
            UpdateWrapper<Activity> uw = new UpdateWrapper<>();
            uw.setSql("current_participants = current_participants - 1")
              .eq("activity_id", reg.getActivityId());
            activityMapper.update(null, uw);
        }
        return rows > 0;
    }

    @Override
    public List<Registration> listByPerson(Integer personId) {
        QueryWrapper<Registration> qw = new QueryWrapper<>();
        qw.eq("r.person_id", personId).orderByDesc("r.reg_time");
        return registrationMapper.selectRegistrationWithDetails(qw);
    }

    @Override
    public PageResult<Registration> listByPersonPage(int page, int pageSize, Integer personId) {
        QueryWrapper<Registration> qw = new QueryWrapper<>();
        qw.eq("r.person_id", personId).orderByDesc("r.reg_time");
        Page<Registration> mpPage = new Page<>(page, pageSize);
        var result = registrationMapper.selectRegistrationPage(mpPage, qw);
        return new PageResult<>(result.getTotal(), page, pageSize, result.getRecords());
    }

    @Override
    public List<Registration> listByActivity(Integer activityId) {
        QueryWrapper<Registration> qw = new QueryWrapper<>();
        qw.eq("r.activity_id", activityId).orderByDesc("r.reg_time");
        return registrationMapper.selectRegistrationWithDetails(qw);
    }

    @Override
    public long countByActivity(Integer activityId) {
        QueryWrapper<Registration> qw = new QueryWrapper<>();
        qw.eq("activity_id", activityId).eq("status", "已报名");
        return registrationMapper.selectCount(qw);
    }

    @Override
    public boolean isRegistered(Integer activityId, Integer personId) {
        QueryWrapper<Registration> qw = new QueryWrapper<>();
        qw.eq("activity_id", activityId).eq("person_id", personId).eq("status", "已报名");
        return registrationMapper.selectCount(qw) > 0;
    }
}

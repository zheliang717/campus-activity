package com.campus.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.activity.common.PageResult;
import com.campus.activity.entity.Activity;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.mapper.VenueMapper;
import com.campus.activity.service.ActivityService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;
    private final VenueMapper venueMapper;

    public ActivityServiceImpl(ActivityMapper activityMapper, VenueMapper venueMapper) {
        this.activityMapper = activityMapper;
        this.venueMapper = venueMapper;
    }

    @Override
    public List<Activity> listAll(String name, String type, String date, String status) {
        return queryActivities(name, type, date, status, false);
    }

    @Override
    public PageResult<Activity> listPage(int page, int pageSize, String name, String type, String date, String status) {
        QueryWrapper<Activity> qw = buildQuery(name, type, date, status);
        Page<Activity> mpPage = new Page<>(page, pageSize);
        var result = activityMapper.selectActivityPage(mpPage, qw);
        return new PageResult<>(result.getTotal(), page, pageSize, result.getRecords());
    }

    private List<Activity> queryActivities(String name, String type, String date, String status, boolean paginated) {
        QueryWrapper<Activity> qw = buildQuery(name, type, date, status);
        return activityMapper.selectActivityWithVenue(qw);
    }

    private QueryWrapper<Activity> buildQuery(String name, String type, String date, String status) {
        QueryWrapper<Activity> qw = new QueryWrapper<>();
        if (StringUtils.hasText(name)) qw.like("a.name", name);
        if (StringUtils.hasText(type)) qw.eq("a.type", type);
        if (StringUtils.hasText(date)) qw.eq("a.activity_date", date);
        if (StringUtils.hasText(status)) qw.eq("a.status", status);
        qw.orderByDesc("a.create_time");
        return qw;
    }

    @Override
    public Activity getById(Integer id) {
        QueryWrapper<Activity> qw = new QueryWrapper<>();
        qw.eq("a.activity_id", id);
        List<Activity> list = activityMapper.selectActivityWithVenue(qw);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public boolean save(Activity activity) {
        return activityMapper.insert(activity) > 0;
    }

    @Override
    public boolean update(Activity activity) {
        return activityMapper.updateById(activity) > 0;
    }

    @Override
    public boolean review(Integer id, String status) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) return false;
        activity.setStatus(status);
        return activityMapper.updateById(activity) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        return activityMapper.deleteById(id) > 0;
    }

    @Override
    public List<Map<String, Object>> getSchedule(String date) {
        QueryWrapper<Activity> qw = new QueryWrapper<>();
        if (StringUtils.hasText(date)) qw.eq("a.activity_date", date);
        qw.eq("a.status", "已通过");
        qw.orderByAsc("v.name", "a.start_time");
        List<Activity> activities = activityMapper.selectActivityWithVenue(qw);

        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        for (Activity a : activities) {
            String venueName = a.getVenueName() != null ? a.getVenueName() : "未分配场地";
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("activityId", a.getActivityId());
            item.put("name", a.getName());
            item.put("type", a.getType());
            item.put("organizer", a.getOrganizer());
            item.put("activityDate", a.getActivityDate() != null ? a.getActivityDate().toString() : "");
            item.put("startTime", a.getStartTime());
            item.put("endTime", a.getEndTime());
            item.put("venueLocation", a.getVenueLocation());
            item.put("maxParticipants", a.getMaxParticipants());
            item.put("currentParticipants", a.getCurrentParticipants());
            grouped.computeIfAbsent(venueName, k -> new ArrayList<>()).add(item);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
            Map<String, Object> group = new LinkedHashMap<>();
            group.put("venueName", entry.getKey());
            group.put("activities", entry.getValue());
            result.add(group);
        }
        return result;
    }
}

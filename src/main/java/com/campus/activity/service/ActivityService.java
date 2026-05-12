package com.campus.activity.service;

import com.campus.activity.common.PageResult;
import com.campus.activity.entity.Activity;
import java.util.List;
import java.util.Map;

public interface ActivityService {
    List<Activity> listAll(String name, String type, String date, String status);
    PageResult<Activity> listPage(int page, int pageSize, String name, String type, String date, String status);
    Activity getById(Integer id);
    boolean save(Activity activity);
    boolean update(Activity activity);
    boolean review(Integer id, String status);
    boolean delete(Integer id);
    List<Map<String, Object>> getSchedule(String date);
}

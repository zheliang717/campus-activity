package com.campus.activity.service;

import com.campus.activity.common.PageResult;
import com.campus.activity.entity.Registration;
import java.util.List;

public interface RegistrationService {
    boolean register(Integer activityId, Integer personId);
    boolean cancel(Integer regId, Integer personId);
    List<Registration> listByPerson(Integer personId);
    PageResult<Registration> listByPersonPage(int page, int pageSize, Integer personId);
    List<Registration> listByActivity(Integer activityId);
    long countByActivity(Integer activityId);
    boolean isRegistered(Integer activityId, Integer personId);
}

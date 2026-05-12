package com.campus.activity.service;

import com.campus.activity.common.PageResult;
import com.campus.activity.entity.Venue;
import java.util.List;

public interface VenueService {
    List<Venue> listAll();
    PageResult<Venue> listPage(int page, int pageSize, String name);
    List<Venue> listAvailable();
    Venue getById(Integer id);
    boolean save(Venue venue);
    boolean update(Venue venue);
    boolean delete(Integer id);
}

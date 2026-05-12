package com.campus.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.activity.common.PageResult;
import com.campus.activity.entity.Venue;
import com.campus.activity.mapper.VenueMapper;
import com.campus.activity.service.VenueService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class VenueServiceImpl implements VenueService {

    private final VenueMapper venueMapper;

    public VenueServiceImpl(VenueMapper venueMapper) {
        this.venueMapper = venueMapper;
    }

    @Override
    public List<Venue> listAll() {
        return venueMapper.selectList(null);
    }

    @Override
    public PageResult<Venue> listPage(int page, int pageSize, String name) {
        QueryWrapper<Venue> qw = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            qw.like("name", name);
        }
        qw.orderByAsc("status").orderByDesc("create_time");
        Page<Venue> mpPage = venueMapper.selectPage(new Page<>(page, pageSize), qw);
        return new PageResult<>(mpPage.getTotal(), page, pageSize, mpPage.getRecords());
    }

    @Override
    public List<Venue> listAvailable() {
        QueryWrapper<Venue> qw = new QueryWrapper<>();
        qw.eq("status", 1);
        return venueMapper.selectList(qw);
    }

    @Override
    public Venue getById(Integer id) {
        return venueMapper.selectById(id);
    }

    @Override
    public boolean save(Venue venue) {
        return venueMapper.insert(venue) > 0;
    }

    @Override
    public boolean update(Venue venue) {
        return venueMapper.updateById(venue) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        return venueMapper.deleteById(id) > 0;
    }
}

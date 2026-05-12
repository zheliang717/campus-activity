package com.campus.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.activity.entity.Person;
import com.campus.activity.mapper.PersonMapper;
import com.campus.activity.service.PersonService;
import org.springframework.stereotype.Service;

@Service
public class PersonServiceImpl implements PersonService {

    private final PersonMapper personMapper;

    public PersonServiceImpl(PersonMapper personMapper) {
        this.personMapper = personMapper;
    }

    @Override
    public Person login(String username, String password) {
        QueryWrapper<Person> qw = new QueryWrapper<>();
        qw.eq("username", username);
        qw.eq("password", password);
        return personMapper.selectOne(qw);
    }

    @Override
    public Person getById(Integer id) {
        return personMapper.selectById(id);
    }

    @Override
    public boolean register(Person person) {
        return personMapper.insert(person) > 0;
    }

    @Override
    public boolean update(Person person) {
        return personMapper.updateById(person) > 0;
    }
}

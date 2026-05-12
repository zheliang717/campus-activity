package com.campus.activity.service;

import com.campus.activity.entity.Person;

public interface PersonService {
    Person login(String username, String password);
    Person getById(Integer id);
    boolean register(Person person);
    boolean update(Person person);
}

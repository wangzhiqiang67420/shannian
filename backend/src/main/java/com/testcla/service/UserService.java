package com.testcla.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final Map<String, Long> users = new ConcurrentHashMap<>();

    public boolean exists(String phone) {
        return users.containsKey(phone);
    }

    public void register(String phone) {
        users.putIfAbsent(phone, System.currentTimeMillis());
    }
}

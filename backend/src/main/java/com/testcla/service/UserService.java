package com.testcla.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean exists(String phone) {
        return false;
    }

    public void register(String phone) {
        // Legacy SMS login is kept only for local compatibility.
    }

    public void registerWechat(String openid, String unionid, String sessionKey) {
        jdbcTemplate.update(
                "INSERT INTO users (openid, unionid, session_key, created_at, updated_at) " +
                        "VALUES (?, ?, ?, NOW(), NOW()) " +
                        "ON DUPLICATE KEY UPDATE unionid = VALUES(unionid), session_key = VALUES(session_key), updated_at = NOW()",
                openid, unionid, sessionKey
        );
    }
}

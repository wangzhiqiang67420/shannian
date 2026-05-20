package com.testcla.controller;

import com.testcla.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "未提供认证令牌");
            return ResponseEntity.status(401).body(error);
        }
        String openid = jwtUtil.getPhoneFromToken(authHeader.substring(7));

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("openid", openid);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", userInfo);
        return ResponseEntity.ok(response);
    }
}

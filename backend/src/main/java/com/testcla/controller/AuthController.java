package com.testcla.controller;

import com.testcla.service.UserService;
import com.testcla.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String DEFAULT_CODE = "000000";

    /**
     * 发送验证码（模拟）
     * POST /api/auth/send-code
     */
    @PostMapping("/send-code")
    public ResponseEntity<Map<String, Object>> sendCode(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "手机号格式不正确");
            return ResponseEntity.badRequest().body(error);
        }

        System.out.println("[SMS] 验证码已发送到 " + phone + ": " + DEFAULT_CODE);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "验证码已发送");
        return ResponseEntity.ok(response);
    }

    /**
     * 登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String code = request.get("code");

        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "手机号格式不正确");
            return ResponseEntity.badRequest().body(error);
        }

        if (code == null || !code.equals(DEFAULT_CODE)) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "验证码错误");
            return ResponseEntity.badRequest().body(error);
        }

        // 自动注册（首次登录）
        userService.register(phone);

        // 生成 Token
        String token = jwtUtil.generateToken(phone);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("phone", phone);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", token);
        response.put("user", userInfo);
        return ResponseEntity.ok(response);
    }
}

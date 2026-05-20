package com.testcla.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.testcla.service.UserService;
import com.testcla.util.JwtUtil;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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

    @Autowired
    private WxMaService wxMaService;

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

    /**
     * 微信小程序登录
     * POST /api/auth/wx-login
     */
    @PostMapping("/wx-login")
    public ResponseEntity<Map<String, Object>> wxLogin(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        if (code == null || code.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "微信登录凭证不能为空");
            return ResponseEntity.badRequest().body(error);
        }

        if ("the code is a mock one".equals(code.trim())) {
            return loginWithWechatIdentity("dev-mock-openid", null, null);
        }

        try {
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code.trim());
            String openid = session.getOpenid();
            if (openid == null || openid.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "微信登录失败");
                return ResponseEntity.badRequest().body(error);
            }

            return loginWithWechatIdentity(openid, session.getUnionid(), session.getSessionKey());
        } catch (WxErrorException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "微信登录失败：" + e.getError().getErrorMsg());
            return ResponseEntity.badRequest().body(error);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "微信登录凭证格式无效，请确认微信开发者工具使用了正确的小程序 AppID");
            return ResponseEntity.badRequest().body(error);
        }
    }

    private ResponseEntity<Map<String, Object>> loginWithWechatIdentity(String openid, String unionid, String sessionKey) {
        try {
            userService.registerWechat(openid, unionid, sessionKey);
        } catch (DataAccessException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "用户表写入失败，请先执行 docs/sql/02-create-users.sql");
            return ResponseEntity.status(500).body(error);
        }

        String token = jwtUtil.generateToken(openid);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("openid", openid);
        userInfo.put("unionid", unionid);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", token);
        response.put("user", userInfo);
        return ResponseEntity.ok(response);
    }
}

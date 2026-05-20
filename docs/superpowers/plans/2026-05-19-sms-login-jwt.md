# 手机号 + 短信验证码登录 (JWT) 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在现有全栈项目中增加手机号+短信验证码登录功能，验证码默认 000000，登录后使用 JWT 令牌鉴权。

**Architecture:**
- 后端新增 JWT 工具类和认证过滤器，保护需要登录的 API；用户数据暂存内存（ConcurrentHashMap），无需数据库
- 前端新增登录页面，登录后将 JWT 存入 storage，后续请求自动携带 Authorization header

**Tech Stack:** Spring Boot 2.7.18 / JDK 8 / jjwt 0.12.6 / uni-app Vue 3

---

### Task 1: 后端 — 添加 jjwt 依赖

**Files:**
- Modify: `backend/pom.xml`

- [ ] **Step 1: 在 pom.xml 中添加 jjwt 依赖**（使用 0.12.6 版本，分为 api/impl/jackson 三模块）

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

- [ ] **Step 2: 验证 Maven 依赖可用**

Run: `cd backend && mvn dependency:resolve -q`
Expected: BUILD SUCCESS，无错误

- [ ] **Step 3: Commit**

```bash
git add backend/pom.xml
git commit -m "feat: add jjwt dependency for JWT auth"
```

---

### Task 2: 后端 — 创建 JwtUtil 工具类

**Files:**
- Create: `backend/src/main/java/com/testcla/util/JwtUtil.java`

- [ ] **Step 1: 创建 JwtUtil.java**

```java
package com.testcla.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final String SECRET = "TestClaSecretKey2026ForJWT!";
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7天

    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    /**
     * 生成 JWT Token
     */
    public String generateToken(String phone) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("phone", phone);
        return Jwts.builder()
                .claims(claims)
                .subject(phone)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从 Token 中提取手机号
     */
    public String getPhoneFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

- [ ] **Step 2: 检查编译**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/testcla/util/JwtUtil.java
git commit -m "feat: add JwtUtil for token generation and validation"
```

---

### Task 3: 后端 — 创建 UserService（内存用户存储）

**Files:**
- Create: `backend/src/main/java/com/testcla/service/UserService.java`

- [ ] **Step 1: 创建 UserService.java**

```java
package com.testcla.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存用户存储，生产环境应替换为数据库
 * key: 手机号, value: 注册时间等用户信息
 */
@Service
public class UserService {

    private final Map<String, Long> users = new ConcurrentHashMap<>();

    /**
     * 用户是否存在
     */
    public boolean exists(String phone) {
        return users.containsKey(phone);
    }

    /**
     * 注册新用户（首次登录自动注册）
     */
    public void register(String phone) {
        users.putIfAbsent(phone, System.currentTimeMillis());
    }
}
```

- [ ] **Step 2: 编译检查**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/testcla/service/UserService.java
git commit -m "feat: add UserService with in-memory user store"
```

---

### Task 4: 后端 — 创建 AuthController（登录 API）

**Files:**
- Create: `backend/src/main/java/com/testcla/controller/AuthController.java`

- [ ] **Step 1: 创建 AuthController.java**

```java
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

    // 硬编码验证码，后续对接短信服务商后替换
    private static final String DEFAULT_CODE = "000000";

    /**
     * 发送验证码（目前只是模拟）
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

        // 模拟发送验证码（实际对接 SMS 服务商）
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
```

- [ ] **Step 2: 编译检查**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/testcla/controller/AuthController.java
git commit -m "feat: add send-code and login API endpoints"
```

---

### Task 5: 后端 — 创建 JWT 认证过滤器

**Files:**
- Create: `backend/src/main/java/com/testcla/filter/JwtAuthFilter.java`
- Create: `backend/src/main/java/com/testcla/config/WebConfig.java`

- [ ] **Step 1: 创建 JwtAuthFilter.java**

```java
package com.testcla.filter;

import com.testcla.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // 不需要认证的路径
    private static final String[] PUBLIC_PATHS = {
        "/api/auth/", "/api/ping"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"success\":false,\"message\":\"未提供认证令牌\"}");
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"success\":false,\"message\":\"令牌无效或已过期\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}
```

- [ ] **Step 2: 创建 WebConfig.java 注册过滤器**

```java
package com.testcla.config;

import com.testcla.filter.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilter() {
        FilterRegistrationBean<JwtAuthFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(jwtAuthFilter);
        bean.addUrlPatterns("/api/*");
        bean.setOrder(1);
        return bean;
    }
}
```

- [ ] **Step 3: 编译检查**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: 重启后端并测试登录流程**

Run:
```bash
# 先停掉旧进程（按 Ctrl+C），再重新启动
cd backend && mvn spring-boot:run
```

测试 send-code:
```bash
curl -s -X POST http://localhost:8080/api/auth/send-code \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000"}'
```
Expected: `{"success":true,"message":"验证码已发送"}`

测试 login:
```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","code":"000000"}'
```
Expected: `{"success":true,"token":"eyJ...","user":{"phone":"13800138000"}}`

测试无 token 访问 protected 路径（/api/user/info 尚不存在，但可以用其他 /api/ 路径验证过滤器生效）:
```bash
curl -s http://localhost:8080/api/ping
```
Expected: 返回 `{"message":"Hello from Spring Boot!"}`（public 路径，不受过滤器影响）

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/testcla/filter/JwtAuthFilter.java
git add backend/src/main/java/com/testcla/config/WebConfig.java
git commit -m "feat: add JWT auth filter to protect API endpoints"
```

---

### Task 6: 后端 — 创建用户信息接口（受保护）

**Files:**
- Create: `backend/src/main/java/com/testcla/controller/UserController.java`

- [ ] **Step 1: 创建 UserController.java**

```java
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

    /**
     * 获取当前登录用户信息（受 JWT 保护）
     * GET /api/user/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String phone = jwtUtil.getPhoneFromToken(token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("phone", phone);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", userInfo);
        return ResponseEntity.ok(response);
    }
}
```

- [ ] **Step 2: 重启后端并测试受保护接口**

使用 Task 5 拿到的 token 测试:
```bash
TOKEN="<上一步获取的token>"
curl -s http://localhost:8080/api/user/info \
  -H "Authorization: Bearer $TOKEN"
```
Expected: `{"success":true,"user":{"phone":"13800138000"}}`

无 token 访问:
```bash
curl -s http://localhost:8080/api/user/info
```
Expected: `{"success":false,"message":"未提供认证令牌"}`

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/testcla/controller/UserController.java
git commit -m "feat: add protected /api/user/info endpoint"
```

---

### Task 7: 前端 — 创建登录页面

**Files:**
- Create: `frontend/src/pages/login.vue`
- Modify: `frontend/src/pages.json`
- Modify: `frontend/src/pages/index.vue`

- [ ] **Step 1: 创建 login.vue**

```vue
<template>
  <view class="login-container">
    <view class="login-box">
      <text class="title">手机号登录</text>

      <input
        class="input"
        v-model="phone"
        type="text"
        maxlength="11"
        placeholder="请输入手机号"
        @input="onPhoneInput"
      />

      <view class="code-row">
        <input
          class="input code-input"
          v-model="code"
          type="text"
          maxlength="6"
          placeholder="请输入验证码"
        />
        <button
          class="send-code-btn"
          :disabled="countdown > 0"
          @click="sendCode"
        >
          {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
        </button>
      </view>

      <button type="primary" class="login-btn" @click="handleLogin">登录</button>

      <text v-if="errorMsg" class="error">{{ errorMsg }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const phone = ref('')
const code = ref('')
const countdown = ref(0)
const errorMsg = ref('')
let timer = null

function onPhoneInput(e) {
  // 只允许数字
  phone.value = phone.value.replace(/\D/g, '')
}

async function sendCode() {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    errorMsg.value = '请输入正确的手机号'
    return
  }

  errorMsg.value = ''
  try {
    const res = await uni.request({
      url: '/api/auth/send-code',
      method: 'POST',
      data: { phone: phone.value }
    })
    if (res.data.success) {
      uni.showToast({ title: '验证码已发送', icon: 'success' })
      // 开始倒计时
      countdown.value = 60
      timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
          timer = null
        }
      }, 1000)
    }
  } catch (e) {
    errorMsg.value = '发送验证码失败'
  }
}

async function handleLogin() {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    errorMsg.value = '请输入正确的手机号'
    return
  }
  if (!code.value || code.value.length !== 6) {
    errorMsg.value = '请输入6位验证码'
    return
  }

  errorMsg.value = ''
  try {
    const res = await uni.request({
      url: '/api/auth/login',
      method: 'POST',
      data: {
        phone: phone.value,
        code: code.value
      }
    })

    if (res.data.success) {
      // 保存 token 和用户信息
      uni.setStorageSync('token', res.data.token)
      uni.setStorageSync('userInfo', res.data.user)
      uni.showToast({ title: '登录成功', icon: 'success' })
      // 跳转到首页
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/index' })
      }, 500)
    } else {
      errorMsg.value = res.data.message || '登录失败'
    }
  } catch (e) {
    errorMsg.value = '登录失败，请检查网络'
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 30px;
  background-color: #f5f5f5;
}
.login-box {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 40px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
.title {
  display: block;
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}
.input {
  width: 100%;
  height: 44px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 16px;
  margin-bottom: 16px;
  box-sizing: border-box;
}
.code-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.code-input {
  flex: 1;
  margin-bottom: 0;
}
.send-code-btn {
  width: 120px;
  height: 44px;
  line-height: 44px;
  text-align: center;
  background-color: #f0f0f0;
  border-radius: 8px;
  font-size: 14px;
  color: #333;
  border: none;
  padding: 0;
}
.send-code-btn[disabled] {
  color: #999;
}
.login-btn {
  width: 100%;
  height: 44px;
  line-height: 44px;
  border-radius: 8px;
  font-size: 16px;
  margin-top: 8px;
}
.error {
  display: block;
  color: #e74c3c;
  font-size: 14px;
  text-align: center;
  margin-top: 12px;
}
</style>
```

- [ ] **Step 2: 更新 pages.json 添加登录路由**

```json
{
  "pages": [
    {
      "path": "pages/index",
      "type": "home"
    },
    {
      "path": "pages/login"
    }
  ],
  // ... 其余不变
}
```

- [ ] **Step 3: 更新 index.vue — 顶部添加登录/用户信息**

```vue
<!-- 在 <template> 顶部，container 最前面添加 -->
<view class="header">
  <text v-if="userInfo" class="user-info">{{ userInfo.phone }}</text>
  <button v-else class="login-nav" @click="goLogin">登录</button>
</view>
```

```javascript
// 在 <script setup> 中添加
import { ref, onMounted } from 'vue'

const userInfo = ref(null)
const message = ref('')  // 已有

onMounted(() => {
  const token = uni.getStorageSync('token')
  const stored = uni.getStorageSync('userInfo')
  if (token && stored) {
    userInfo.value = stored
  }
})

function goLogin() {
  uni.navigateTo({ url: '/pages/login' })
}
```

```css
/* 在 <style> 中添加 */
.header {
  position: absolute;
  top: 0;
  right: 0;
  padding: 12px 16px;
}
.user-info {
  font-size: 14px;
  color: #999;
}
.login-nav {
  font-size: 14px;
  padding: 4px 12px;
}
```

注意：因为 index.vue 现在用 `navigationStyle: "custom"`（无默认导航栏），需在顶部加操作按钮。

- [ ] **Step 4: 启动前端验证**

Run: `cd frontend && npm run dev`
Expected: 打开 `http://localhost:5173` 能看到登录按钮 → 点击进入登录页 → 输入手机号获取验证码 → 输入 000000 → 登录成功 → 跳转回首页显示手机号

- [ ] **Step 5: Commit**

```bash
git add frontend/src/pages/login.vue
git add frontend/src/pages.json
git add frontend/src/pages/index.vue
git commit -m "feat: add login page with phone + SMS code auth"
```

---

### Task 8: 端到端验证

- [ ] **Step 1: 启动后端**

```bash
cd backend && mvn spring-boot:run
```
确认后端在 8080 端口运行。

- [ ] **Step 2: 启动前端**

```bash
cd frontend && npm run dev
```
确认前端在 5173 端口运行。

- [ ] **Step 3: 浏览器打开前端**

访问 `http://localhost:5173` → 页面右上角显示"登录"按钮 → 点击 → 进入登录页

- [ ] **Step 4: 登录流程验证**
   - 输入手机号 `13800138000` → 点击"获取验证码" → 提示"验证码已发送"
   - 输入验证码 `000000` → 点击"登录" → 提示"登录成功" → 跳回首页 → 右上角显示手机号

- [ ] **Step 5: API 直接验证**

```bash
# 登录获取 token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","code":"000000"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 携带 token 访问受保护接口
curl -s http://localhost:8080/api/user/info -H "Authorization: Bearer $TOKEN"

# 无 token 访问应被拒绝
curl -s http://localhost:8080/api/user/info
# Expected: 401
```

- [ ] **Step 6: Commit（最终集成提交）**

```bash
git add -A
git commit -m "feat: complete phone + SMS code login with JWT auth"
```

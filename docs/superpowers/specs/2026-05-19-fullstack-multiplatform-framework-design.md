# 全栈多端运行框架 — 设计文档

## 概述

构建一个前后端分离的全栈框架雏形，基于 uni-app（Vue 3）实现 Web / 微信小程序 / 移动端 App 三端统一运行，后端基于 Spring Boot 2.7.x + JDK 8 + Maven 提供 API 服务。首个功能为最简点击交互。

## 技术栈

| 层 | 技术 | 备注 |
|---|------|------|
| 前端框架 | uni-app (Vue 3 编译模式) | 一套代码编译到 Web / 小程序 / App |
| 后端框架 | Spring Boot 2.7.x | 兼容 JDK 8 |
| 构建工具 | Maven | |
| JDK | 8 | |
| 通信 | HTTP JSON | uni.request 适配三端 |

## 项目结构（Monorepo）

```
D:/testcla/
├── frontend/                    # uni-app 项目
│   ├── pages/
│   │   └── index/
│   │       └── index.vue        # 首页：按钮 + 结果展示
│   ├── App.vue
│   ├── main.js                  # 挂载 Vue 3 应用
│   ├── manifest.json            # uni-app 多端配置
│   ├── pages.json               # 页面路由
│   └── uni.scss                 # 全局样式变量
├── backend/                     # Spring Boot 项目
│   ├── pom.xml                  # Maven 依赖
│   └── src/main/
│       ├── java/com/testcla/
│       │   ├── TestclaApplication.java   # 启动入口
│       │   └── controller/
│       │       └── PingController.java   # GET /api/ping
│       └── resources/
│           └── application.yml           # 服务端口等配置
```

## 后端 API

### 接口

| 方法 | 路径 | 说明 | 响应示例 |
|------|------|------|---------|
| GET | `/api/ping` | 点击调用 | `{ "message": "Hello from Spring Boot!" }` |

### PingController

```java
@RestController
@RequestMapping("/api")
public class PingController {

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("message", "Hello from Spring Boot!");
    }
}
```

- 无数据库依赖，零额外配置
- 默认端口 8080，通过 `@CrossOrigin` 注解允许开发阶段跨域

## 前端页面

### 首页 (`pages/index/index.vue`)

- **按钮**：用户点击触发请求
- **结果展示**：显示后端返回的 message
- 使用 Vue 3 Composition API (`<script setup>`)
- 使用 `uni.request` 发起 HTTP 请求，三端统一

## 数据流

```
用户点击 → handleClick() → uni.request(GET /api/ping) → Spring Boot → { message }
                                                                      ↓
                                                  <text> 显示 message ← 赋值
```

## CORS 配置

### Controller 层

`@CrossOrigin` 标注在 `PingController` 上，允许开发阶段跨域访问。

### Web 端开发代理

HBuilder 开发 Web 端时，在 `manifest.json` 中配置 `h5.proxy` 将 `/api` 请求转发到 `http://localhost:8080`，避免浏览器跨域限制。

### 小程序 / App 端

使用完整 URL（如 `http://localhost:8080/api/ping`）直连后端，或通过环境变量配置基础路径。

## 后续扩展方向（不在本阶段实现）

- 数据库集成（Spring Data JPA + MySQL）
- 用户认证（JWT）
- 更多前端页面与组件
- 构建与部署脚本

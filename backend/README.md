# 闪念笔记 - 后端

基于 Spring Boot 2.7 + MyBatis-Plus + JWT 的 RESTful API 服务。

## 技术栈

| 组件 | 版本 |
|------|------|
| Java | 1.8 |
| Spring Boot | 2.7.18 |
| MyBatis-Plus | 3.5.3.2 |
| MySQL | 8.0 |
| jjwt (JSON Web Token) | 0.12.6 |

## 项目结构

```
backend/
├── pom.xml
└── src/main/
    ├── java/com/testcla/
    │   ├── TestclaApplication.java       # 启动入口
    │   ├── config/
    │   │   ├── MybatisPlusConfig.java     # MyBatis-Plus 分页插件配置
    │   │   └── WebConfig.java             # JWT 过滤器注册
    │   ├── controller/
    │   │   ├── PingController.java        # 健康检查 /api/ping
    │   │   ├── AuthController.java        # 登录 & 验证码 /api/auth/*
    │   │   ├── UserController.java        # 用户信息 /api/user/*
    │   │   └── NoteController.java        # 笔记 CRUD /api/notes/*
    │   ├── entity/
    │   │   ├── Note.java                  # 笔记实体 (notes 表)
    │   │   └── NoteHistory.java           # 修改历史实体 (note_histories 表)
    │   ├── filter/
    │   │   └── JwtAuthFilter.java         # JWT 认证过滤器
    │   ├── mapper/
    │   │   ├── NoteMapper.java
    │   │   └── NoteHistoryMapper.java
    │   ├── service/
    │   │   ├── UserService.java           # 用户注册（内存存储）
    │   │   ├── NoteService.java           # 笔记业务逻辑
    │   │   └── GeocodeService.java        # 高德地图逆地理编码
    │   └── util/
    │       └── JwtUtil.java               # JWT 生成与校验
    └── resources/
        └── application.yml                # 应用配置
```

## API 接口

### 公开接口（无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/ping` | 健康检查 |
| POST | `/api/auth/send-code` | 发送验证码（模拟，固定 000000） |
| POST | `/api/auth/login` | 手机号 + 验证码登录，返回 JWT Token |

### 认证接口（需 Bearer Token）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/info` | 获取当前用户信息 |
| GET | `/api/notes/list?page=1&size=5` | 分页获取笔记列表 |
| GET | `/api/notes/detail?id=1` | 获取笔记详情 |
| GET | `/api/notes/geocode?lat=xx&lng=xx` | 逆地理编码（高德） |
| POST | `/api/notes/add` | 新增笔记 |
| POST | `/api/notes/update` | 修改笔记（自动保存历史快照） |
| POST | `/api/notes/delete` | 删除笔记及历史 |
| GET | `/api/notes/histories?id=1` | 查看笔记修改历史 |

## 启动步骤

### 1. 准备数据库

在 MySQL 中执行建表脚本：

```bash
mysql -h <host> -u <user> -p < docs/sql/01-create-tables.sql
```

脚本会自动创建 `notes` 和 `note_histories` 两张表。

### 2. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://<你的MySQL地址>:3306/demo?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8
    username: <用户名>
    password: <密码>
```

### 3. 启动服务

```bash
# 使用 Maven Wrapper（推荐）
./mvnw spring-boot:run

# 或直接使用 Maven
mvn spring-boot:run

# 或打包运行
mvn clean package -DskipTests
java -jar target/testcla-backend-1.0.0.jar
```

服务默认启动在 `http://localhost:8080`。

### 4. 验证

```bash
curl http://localhost:8080/api/ping
# {"message":"Hello from Spring Boot!"}
```

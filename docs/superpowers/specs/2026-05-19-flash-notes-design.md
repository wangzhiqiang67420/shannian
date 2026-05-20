# 闪念笔记功能 — 设计文档

## 概述

在现有全栈框架上增加闪念笔记功能：点击入口快速记录想法，自动记录时间，支持修改历史追溯。

## 技术栈

| 层 | 技术 | 备注 |
|---|------|------|
| 前端 | uni-app Vue 3 + tabBar | 底部两个页签：首页 / 我的 |
| 后端 | Spring Boot 2.7.18 + JDK 8 | |
| 数据库 | MySQL | |
| 认证 | JWT | 复用现有登录体系 |

## 页面结构

```
底部 TabBar
  ├── 首页（默认）
  │     └── 点击"开始记录"按钮 → 跳转笔记编辑页
  │
  └── 我的
        ├── 历史笔记列表（时间倒序）
        │     ├── 点击某条 → 笔记详情页
        │     │     ├── 编辑 → 保存
        │     │     └── 查看修改历史 → 时间线列表
        │     └── 左滑/点击删除
        └── （后续可扩展其他个人信息）
```

### 页面列表

| 页面 | 路径 | 说明 |
|------|------|------|
| 首页 | `pages/index` | tabBar，显示"开始"按钮 |
| 我的 | `pages/mine` | tabBar，笔记历史列表 |
| 笔记编辑 | `pages/note/edit` | 新建笔记页面 |
| 笔记详情 | `pages/note/detail` | 查看/编辑已有笔记 |
| 修改历史 | `pages/note/history` | 显示某条笔记的修改时间线 |

## 数据库

### notes 表

```sql
CREATE TABLE notes (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '笔记ID',
    phone       VARCHAR(20)   NOT NULL COMMENT '用户手机号',
    content     TEXT          NOT NULL COMMENT '笔记内容',
    created_at  DATETIME      NOT NULL COMMENT '创建时间',
    updated_at  DATETIME      NOT NULL COMMENT '最后修改时间',
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### note_histories 表

```sql
CREATE TABLE note_histories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '历史ID',
    note_id     BIGINT        NOT NULL COMMENT '关联笔记ID',
    content     TEXT          NOT NULL COMMENT '修改前的内容快照',
    created_at  DATETIME      NOT NULL COMMENT '快照时间（即修改发生时间）',
    INDEX idx_note_id (note_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

每次更新笔记时，将修改前的 content 存入 note_histories 作为快照。

## 后端 API

所有接口均需 `Authorization: Bearer <token>` 认证。

| 方法 | 路径 | 请求参数 | 返回 | 说明 |
|------|------|---------|------|------|
| GET | `/api/notes/list` | `?page=1&size=20` | 笔记列表（分页，按 updated_at 倒序） | 获取当前用户笔记列表 |
| GET | `/api/notes/detail` | `?id=1` | 单条笔记详情 | 查看指定笔记 |
| POST | `/api/notes/add` | `{ "content": "..." }` | 创建结果（含新笔记 ID） | 创建笔记，自动记录当前用户和时间 |
| POST | `/api/notes/update` | `{ "id": 1, "content": "..." }` | 更新结果 | 更新笔记，自动保存旧版本快照 |
| GET | `/api/notes/histories` | `?id=1` | 修改历史列表 | 获取某条笔记的修改时间线 |
| POST | `/api/notes/delete` | `{ "id": 1 }` | 删除结果 | 删除笔记（同时删除其修改历史） |

### 响应格式

```json
// 成功
{ "success": true, "data": { ... }, "message": "操作成功" }

// 失败
{ "success": false, "message": "错误描述" }
```

## 前后端交互流程

### 创建笔记
```
首页 → 点击"开始记录" → note/edit 页面 → 输入内容 → 保存
  → POST /api/notes/add → 后端插入 notes 表 → 返回成功
  → 跳转回首页，提示已保存
```

### 查看历史列表
```
我的 → 加载笔记列表 → GET /api/notes/list → 按时间倒序展示
  → 点击某条 → 跳转 note/detail?id=X
```

### 更新笔记
```
笔记详情 → 编辑内容 → 保存
  → POST /api/notes/update
  → 后端：当前内容存入 note_histories → 更新 notes.content
  → 返回成功
```

### 查看修改历史
```
笔记详情 → 查看修改历史
  → GET /api/notes/histories?id=X → 返回快照列表（按时间倒序）
  → 展示时间线
```

## 后端新增文件

| 文件 | 说明 |
|------|------|
| `config/DataSourceConfig.java` | MySQL 数据源配置 |
| `entity/Note.java` | 笔记实体 |
| `entity/NoteHistory.java` | 笔记历史实体 |
| `repository/NoteRepository.java` | 笔记 DAO |
| `repository/NoteHistoryRepository.java` | 笔记历史 DAO |
| `service/NoteService.java` | 笔记业务逻辑 |
| `controller/NoteController.java` | 笔记 API 接口 |
| `pom.xml` | 新增 MySQL + Spring Data JPA 依赖 |

## 前端新增/修改文件

| 文件 | 说明 |
|------|------|
| `pages/mine.vue` | 我的页签（笔记历史列表） |
| `pages/note/edit.vue` | 新建笔记 |
| `pages/note/detail.vue` | 笔记详情/编辑 |
| `pages/note/history.vue` | 修改历史时间线 |
| `pages.json` | 添加 tabBar 和新页面路由 |
| `pages/index.vue` | 改为"开始记录"入口 |

## 数据流

```
用户点击"开始记录"
  → navigateTo /pages/note/edit
  → 用户输入内容 → 点击保存
  → POST /api/notes/add (JWT)
  → NoteService.add(phone, content)
  → noteRepository.save(...)
  → return { success: true }
  → uni.reLaunch /pages/index

用户查看历史
  → 底部Tab "我的"
  → GET /api/notes/list (JWT)
  → NoteService.list(phone)
  → noteRepository.findByPhoneOrderByUpdatedAtDesc(phone)
  → return 列表
```

## 后续扩展

- 富文本/Markdown 支持
- 笔记标签分类
- 搜索功能
- 数据导出

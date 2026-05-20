# 笔记地理位置记录 — 实施计划

> Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 笔记记录时自动获取 GPS 并通过高德 API 转为文字地址，列表和详情中展示位置。

**Architecture:** Note 实体增加 lat/lng/address 字段，新增 GeocodeService 封装高德 API 调用，前端编辑页保存前获取坐标并转地址。

**Tech Stack:** 高德地图 Web API / uni.getLocation() / MyBatis-Plus / Spring Boot

---

### Task 1: 数据库 — ALTER TABLE 加字段

Run SQL on remote MySQL:
```sql
ALTER TABLE notes ADD COLUMN latitude  DECIMAL(10,7) DEFAULT NULL COMMENT '纬度';
ALTER TABLE notes ADD COLUMN longitude DECIMAL(10,7) DEFAULT NULL COMMENT '经度';
ALTER TABLE notes ADD COLUMN address   VARCHAR(255)  DEFAULT NULL COMMENT '地址描述';
```

### Task 2: 后端 — Note 实体加字段

Add `latitude`, `longitude`, `address` fields with getters/setters to `Note.java`.

### Task 3: 后端 — 创建 GeocodeService

Call Gaode reverse geocode API, return formatted_address.

### Task 4: 后端 — NoteController 增加 geocode 端点 + add 支持坐标

- GET `/api/notes/geocode?lat=&lng=` → returns { address }
- POST `/api/notes/add` body accepts optional `latitude`, `longitude`, `address`

### Task 5: 前端 — 编辑页增加定位

- Save button calls `uni.getLocation()`, then geocode API, then add note with coordinates.

### Task 6: 前端 — 列表和详情页展示地址

- Mine page: show address below time on cards
- Detail page: show address in header

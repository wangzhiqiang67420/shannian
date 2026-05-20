# 笔记地理位置记录 — 设计文档

## 概述

在闪念笔记中增加地理位置记录功能。用户记录想法时自动获取 GPS 坐标，通过高德地图 API 转换为文字地址，笔记列表和详情中展示位置信息。

## 技术栈

| 项 | 说明 |
|---|------|
| GPS 获取 | uni-app `uni.getLocation()` |
| 逆地理编码 | 高德地图 Web API (`restapi.amap.com/v3/geocode/regeo`) |
| API Key | `d6eaf3a2b5ac64cb71684ffc70690a09` (Web服务) |

## 数据库变更

`notes` 表新增三个字段：

```sql
ALTER TABLE notes ADD COLUMN latitude  DECIMAL(10,7) DEFAULT NULL COMMENT '纬度';
ALTER TABLE notes ADD COLUMN longitude DECIMAL(10,7) DEFAULT NULL COMMENT '经度';
ALTER TABLE notes ADD COLUMN address   VARCHAR(255)  DEFAULT NULL COMMENT '地址描述';
```

## 后端 API

| 方法 | 路径 | 参数 | 说明 |
|------|------|------|------|
| GET | `/api/notes/geocode` | `?lat=xx&lng=xx` | 调高德 API 坐标转地址，返回 `{ address: "..." }` |

`POST /api/notes/add` 请求参数增加可选字段 `latitude`, `longitude`, `address`。

所有 notes 接口返回的 Note 对象自动包含 `latitude`, `longitude`, `address` 字段。

## 后端新增文件

| 文件 | 说明 |
|------|------|
| `service/GeocodeService.java` | 调用高德 API，降级处理 |
| `controller/NoteController.java` | 新增 `/geocode` 端点，`/add` 支持坐标参数 |

## 前端页面改动

| 页面 | 改动 |
|------|------|
| `pages/note/edit.vue` | 保存前调用 `uni.getLocation()`，获取坐标后调 geocode 转地址，保存时传入后端 |
| `pages/mine.vue` | 笔记卡片时间右侧显示地址（小字灰色，最多一行截断） |
| `pages/note/detail.vue` | 时间下方显示完整地址 |

## 数据流

```
编辑页 → 点击保存
  → uni.getLocation() 获取 { latitude, longitude }
  → GET /api/notes/geocode?lat=...&lng=... 获取地址
  → POST /api/notes/add { content, latitude, longitude, address }
  → 后端保存到 notes 表
  → 返回成功，跳转首页
```

## 高德 API 调用

```
GET https://restapi.amap.com/v3/geocode/regeo
  ?key=d6eaf3a2b5ac64cb71684ffc70690a09
  &location=经度,纬度
  &output=JSON

响应:
{
  "status": "1",
  "regeocode": {
    "formatted_address": "北京市朝阳区阜通东大街6号"
  }
}
```

GeocodeService 提取 `regeocode.formatted_address`，失败时返回空字符串（降级处理，不阻塞笔记保存）。

## 降级策略

- GPS 获取失败 → 笔记正常保存，坐标和地址为空
- 高德 API 调用失败 → 坐标正常保存，地址为空
- 无网络 → 跳过定位，不影响笔记记录

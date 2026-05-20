# 闪念笔记功能 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 增加闪念笔记功能，支持创建/编辑/删除笔记、自动记录时间、修改历史追溯，数据存储到 MySQL。

**Architecture:**
- 后端新增 Spring Data JPA + MySQL，Note/NoteHistory 实体，按手机号隔离数据
- 前端增加底部 TabBar（首页 + 我的），笔记编辑/详情/历史页面
- 前端目录结构改为 tabBar 模式

**Tech Stack:** Spring Boot 2.7.18 / JDK 8 / MySQL 5.7 / Spring Data JPA / uni-app Vue 3

**MySQL 连接信息:**
- Host: `172.168.8.101:3306`
- Database: `demo`
- User: `gsxdloan`
- Password: `P@ssw0rd`

---

### Task 1: 后端 — 添加 MySQL + JPA 依赖

**Files:**
- Modify: `backend/pom.xml`

- [ ] **Step 1: 在 pom.xml 中添加依赖**

```xml
<!-- 在 <dependencies> 末尾添加 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
```

- [ ] **Step 2: 验证编译**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 2: 后端 — 配置 MySQL 数据源

**Files:**
- Modify: `backend/src/main/resources/application.yml`

- [ ] **Step 1: 更新 application.yml**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://172.168.8.101:3306/demo?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
    username: gsxdloan
    password: P@ssw0rd
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    database-platform: org.hibernate.dialect.MySQL5InnoDBDialect
```

- [ ] **Step 2: 验证连接**

Run: `cd backend && mvn spring-boot:run -q`
等待启动后：
```bash
curl -s http://localhost:8080/api/ping
```
Expected: `{"message":"Hello from Spring Boot!"}`（启动成功说明 MySQL 连接正常）

---

### Task 3: 后端 — 创建 Note 实体

**Files:**
- Create: `backend/src/main/java/com/testcla/entity/Note.java`

- [ ] **Step 1: 创建 Note.java**

```java
package com.testcla.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 4: 后端 — 创建 NoteHistory 实体

**Files:**
- Create: `backend/src/main/java/com/testcla/entity/NoteHistory.java`

- [ ] **Step 1: 创建 NoteHistory.java**

```java
package com.testcla.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "note_histories")
public class NoteHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long noteId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getNoteId() { return noteId; }
    public void setNoteId(Long noteId) { this.noteId = noteId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 5: 后端 — 创建 Repository 接口

**Files:**
- Create: `backend/src/main/java/com/testcla/repository/NoteRepository.java`
- Create: `backend/src/main/java/com/testcla/repository/NoteHistoryRepository.java`

- [ ] **Step 1: 创建 NoteRepository.java**

```java
package com.testcla.repository;

import com.testcla.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByPhoneOrderByUpdatedAtDesc(String phone);
}
```

- [ ] **Step 2: 创建 NoteHistoryRepository.java**

```java
package com.testcla.repository;

import com.testcla.entity.NoteHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteHistoryRepository extends JpaRepository<NoteHistory, Long> {
    List<NoteHistory> findByNoteIdOrderByCreatedAtDesc(Long noteId);
    void deleteByNoteId(Long noteId);
}
```

- [ ] **Step 3: 编译验证**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 6: 后端 — 创建 NoteService

**Files:**
- Create: `backend/src/main/java/com/testcla/service/NoteService.java`

- [ ] **Step 1: 创建 NoteService.java**

```java
package com.testcla.service;

import com.testcla.entity.Note;
import com.testcla.entity.NoteHistory;
import com.testcla.repository.NoteHistoryRepository;
import com.testcla.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private NoteHistoryRepository noteHistoryRepository;

    public List<Note> list(String phone) {
        return noteRepository.findByPhoneOrderByUpdatedAtDesc(phone);
    }

    public Note detail(Long id) {
        return noteRepository.findById(id).orElse(null);
    }

    @Transactional
    public Note add(String phone, String content) {
        Note note = new Note();
        note.setPhone(phone);
        note.setContent(content);
        return noteRepository.save(note);
    }

    @Transactional
    public Note update(Long id, String content) {
        Note note = noteRepository.findById(id).orElse(null);
        if (note == null) return null;

        // 保存当前内容到历史表
        NoteHistory history = new NoteHistory();
        history.setNoteId(id);
        history.setContent(note.getContent());
        noteHistoryRepository.save(history);

        // 更新笔记
        note.setContent(content);
        note.setUpdatedAt(LocalDateTime.now());
        return noteRepository.save(note);
    }

    public List<NoteHistory> histories(Long noteId) {
        return noteHistoryRepository.findByNoteIdOrderByCreatedAtDesc(noteId);
    }

    @Transactional
    public boolean delete(Long id) {
        if (!noteRepository.existsById(id)) return false;
        noteHistoryRepository.deleteByNoteId(id);
        noteRepository.deleteById(id);
        return true;
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 7: 后端 — 创建 NoteController

**Files:**
- Create: `backend/src/main/java/com/testcla/controller/NoteController.java`

- [ ] **Step 1: 创建 NoteController.java**

```java
package com.testcla.controller;

import com.testcla.entity.Note;
import com.testcla.entity.NoteHistory;
import com.testcla.service.NoteService;
import com.testcla.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private JwtUtil jwtUtil;

    private String getPhone(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.getPhoneFromToken(token);
    }

    private Map<String, Object> success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        result.put("message", "操作成功");
        return result;
    }

    private Map<String, Object> error(String msg) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", msg);
        return result;
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(HttpServletRequest request) {
        String phone = getPhone(request);
        List<Note> notes = noteService.list(phone);
        return ResponseEntity.ok(success(notes));
    }

    @GetMapping("/detail")
    public ResponseEntity<Map<String, Object>> detail(@RequestParam Long id) {
        Note note = noteService.detail(id);
        if (note == null) {
            return ResponseEntity.ok(error("笔记不存在"));
        }
        return ResponseEntity.ok(success(note));
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> add(HttpServletRequest request,
                                                    @RequestBody Map<String, String> body) {
        String phone = getPhone(request);
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.ok(error("内容不能为空"));
        }
        Note note = noteService.add(phone, content.trim());
        return ResponseEntity.ok(success(note));
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String content = (String) body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.ok(error("内容不能为空"));
        }
        Note note = noteService.update(id, content.trim());
        if (note == null) {
            return ResponseEntity.ok(error("笔记不存在"));
        }
        return ResponseEntity.ok(success(note));
    }

    @GetMapping("/histories")
    public ResponseEntity<Map<String, Object>> histories(@RequestParam Long id) {
        List<NoteHistory> histories = noteService.histories(id);
        return ResponseEntity.ok(success(histories));
    }

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        boolean deleted = noteService.delete(id);
        if (!deleted) {
            return ResponseEntity.ok(error("笔记不存在"));
        }
        return ResponseEntity.ok(success(null));
    }
}
```

- [ ] **Step 2: 停掉旧后端，重新启动并测试**

```bash
# 找到并杀掉旧进程
netstat -ano | grep 8080
taskkill //F //PID <old_pid>
# 重新启动
cd backend && mvn spring-boot:run
```

等待启动后测试：
```bash
# 先登录获取 token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","code":"000000"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 添加笔记
curl -s -X POST http://localhost:8080/api/notes/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"content":"这是一条测试笔记"}'

# 获取列表
curl -s http://localhost:8080/api/notes/list \
  -H "Authorization: Bearer $TOKEN"
```

Expected: API 正常返回 JSON 数据

---

### Task 8: 前端 — 重构 pages.json 添加 TabBar

**Files:**
- Modify: `frontend/src/pages.json`

- [ ] **Step 1: 更新 pages.json**

```json
{
  "pages": [
    {
      "path": "pages/index",
      "type": "home"
    },
    {
      "path": "pages/login"
    },
    {
      "path": "pages/mine"
    },
    {
      "path": "pages/note/edit"
    },
    {
      "path": "pages/note/detail"
    },
    {
      "path": "pages/note/history"
    }
  ],
  "tabBar": {
    "color": "#999",
    "selectedColor": "#409eff",
    "backgroundColor": "#fff",
    "borderStyle": "black",
    "list": [
      {
        "pagePath": "pages/index",
        "text": "首页",
        "iconPath": "static/tab/home.png",
        "selectedIconPath": "static/tab/home-active.png"
      },
      {
        "pagePath": "pages/mine",
        "text": "我的",
        "iconPath": "static/tab/mine.png",
        "selectedIconPath": "static/tab/mine-active.png"
      }
    ]
  },
  "globalStyle": {
    "navigationBarTitleText": "TestCla",
    "navigationBarTextStyle": "black",
    "navigationBarBackgroundColor": "#fff"
  }
}
```

注意：需要创建 tab 图标 `static/tab/home.png`、`static/tab/home-active.png`、`static/tab/mine.png`、`static/tab/mine-active.png`。可以用简单的纯色 PNG 图标，或者暂时用文字替代（tabBar 支持 `iconPath` 可选）。

- [ ] **Step 2: 创建 tab 图标占位**

创建 `frontend/src/static/tab/` 目录，放入 tab 图标。如果没有图标资源，tabBar 也可以只显示文字（移除 iconPath 和 selectedIconPath 字段）。

---

### Task 9: 前端 — 改造首页（开始记录入口）

**Files:**
- Modify: `frontend/src/pages/index.vue`

- [ ] **Step 1: 重写 index.vue 为闪念笔记入口**

```vue
<template>
  <view class="page">
    <view class="header">
      <text class="header-title">闪念笔记</text>
    </view>
    <view class="content">
      <view class="start-btn" @click="goEdit">
        <text class="start-icon">+</text>
        <text class="start-text">开始记录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
function goEdit() {
  uni.navigateTo({ url: '/pages/note/edit' })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: #f5f5f5;
}
.header {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 16px;
  padding-top: calc(20px + env(safe-area-inset-top));
  background-color: #fff;
}
.header-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}
.content {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}
.start-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  border-radius: 60px;
  background-color: #409eff;
  color: #fff;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.3);
}
.start-icon {
  font-size: 48px;
  line-height: 1;
}
.start-text {
  font-size: 16px;
  margin-top: 4px;
}
</style>
```

---

### Task 10: 前端 — 创建笔记编辑页面

**Files:**
- Create: `frontend/src/pages/note/edit.vue`

- [ ] **Step 1: 创建 edit.vue**

```vue
<template>
  <view class="page">
    <view class="header">
      <text class="header-title">记录想法</text>
      <text class="header-time">{{ currentTime }}</text>
    </view>
    <textarea
      class="editor"
      :value="content"
      placeholder="输入你的想法..."
      @input="content = $event.detail.value"
      auto-height
      focus
    />
    <view class="footer">
      <button class="save-btn" @click="handleSave">保存</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'

const content = ref('')
const currentTime = ref('')
let timer = null

function updateTime() {
  const now = new Date()
  const pad = n => String(n).padStart(2, '0')
  currentTime.value = `${now.getFullYear()}-${pad(now.getMonth()+1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}`
}
updateTime()
timer = setInterval(updateTime, 1000)

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

async function handleSave() {
  if (!content.value.trim()) {
    uni.showToast({ title: '内容不能为空', icon: 'none' })
    return
  }

  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }

  try {
    const res = await uni.request({
      url: '/api/notes/add',
      method: 'POST',
      header: { 'Authorization': 'Bearer ' + token },
      data: { content: content.value.trim() }
    })
    if (res.data.success) {
      uni.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 500)
    } else {
      uni.showToast({ title: res.data.message || '保存失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: #fff;
  display: flex;
  flex-direction: column;
}
.header {
  padding: 16px;
  border-bottom: 1px solid #eee;
}
.header-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}
.header-time {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
  display: block;
}
.editor {
  flex: 1;
  padding: 16px;
  font-size: 16px;
  line-height: 1.6;
  min-height: 300px;
  border: none;
}
.footer {
  padding: 16px;
  border-top: 1px solid #eee;
}
.save-btn {
  width: 100%;
  height: 44px;
  line-height: 44px;
  background-color: #409eff;
  color: #fff;
  border-radius: 8px;
  font-size: 16px;
  text-align: center;
  border: none;
}
</style>
```

---

### Task 11: 前端 — 创建"我的"页面（历史列表）

**Files:**
- Create: `frontend/src/pages/mine.vue`

- [ ] **Step 1: 创建 mine.vue**

```vue
<template>
  <view class="page">
    <view class="header">
      <text class="header-title">我的</text>
      <text v-if="phone" class="header-phone">{{ phone }}</text>
    </view>

    <view v-if="notes.length === 0" class="empty">
      <text class="empty-text">暂无笔记记录</text>
    </view>

    <scroll-view v-else class="list" scroll-y>
      <view
        class="note-item"
        v-for="item in notes"
        :key="item.id"
        @click="goDetail(item.id)"
      >
        <text class="note-content">{{ item.content }}</text>
        <text class="note-time">{{ formatTime(item.updatedAt) }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onShow } from 'vue'

const notes = ref([])
const phone = ref('')

onShow(() => {
  const token = uni.getStorageSync('token')
  const userInfo = uni.getStorageSync('userInfo')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  phone.value = userInfo?.phone || ''
  loadNotes(token)
})

async function loadNotes(token) {
  try {
    const res = await uni.request({
      url: '/api/notes/list',
      method: 'GET',
      header: { 'Authorization': 'Bearer ' + token }
    })
    if (res.data.success) {
      notes.value = res.data.data || []
    }
  } catch (e) {
    // ignore
  }
}

function goDetail(id) {
  uni.navigateTo({ url: '/pages/note/detail?id=' + id })
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: #f5f5f5;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  padding-top: calc(16px + env(safe-area-inset-top));
  background-color: #fff;
  border-bottom: 1px solid #eee;
}
.header-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}
.header-phone {
  font-size: 14px;
  color: #999;
}
.empty {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}
.empty-text {
  color: #999;
  font-size: 16px;
}
.list {
  padding: 12px;
}
.note-item {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}
.note-content {
  display: block;
  font-size: 16px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}
.note-time {
  display: block;
  font-size: 12px;
  color: #999;
}
</style>
```

---

### Task 12: 前端 — 创建笔记详情/编辑页面

**Files:**
- Create: `frontend/src/pages/note/detail.vue`

- [ ] **Step 1: 创建 detail.vue**

```vue
<template>
  <view class="page">
    <view class="header">
      <text class="header-title">笔记详情</text>
      <text class="header-time">{{ formatTime(note?.createdAt) }}</text>
    </view>

    <textarea
      class="editor"
      :value="content"
      placeholder="加载中..."
      @input="content = $event.detail.value"
      auto-height
    />

    <view class="actions">
      <button class="save-btn" @click="handleUpdate" v-if="hasChanged">保存修改</button>
      <button class="history-btn" @click="goHistory">查看修改历史</button>
      <button class="delete-btn" @click="handleDelete">删除笔记</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const note = ref(null)
const content = ref('')

const hasChanged = computed(() => {
  return note.value && content.value !== note.value.content
})

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const id = currentPage.$page.options?.id || currentPage.options?.id

  if (id) {
    loadDetail(id)
  }
})

async function loadDetail(id) {
  const token = uni.getStorageSync('token')
  if (!token) return

  try {
    const res = await uni.request({
      url: '/api/notes/detail?id=' + id,
      method: 'GET',
      header: { 'Authorization': 'Bearer ' + token }
    })
    if (res.data.success) {
      note.value = res.data.data
      content.value = res.data.data.content
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

async function handleUpdate() {
  const token = uni.getStorageSync('token')
  try {
    const res = await uni.request({
      url: '/api/notes/update',
      method: 'POST',
      header: { 'Authorization': 'Bearer ' + token },
      data: { id: note.value.id, content: content.value.trim() }
    })
    if (res.data.success) {
      uni.showToast({ title: '修改已保存', icon: 'success' })
      note.value.content = content.value.trim()
    } else {
      uni.showToast({ title: res.data.message || '保存失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}

async function handleDelete() {
  uni.showModal({
    title: '确认删除',
    content: '删除后无法恢复',
    success: async (res) => {
      if (res.confirm) {
        const token = uni.getStorageSync('token')
        try {
          const r = await uni.request({
            url: '/api/notes/delete',
            method: 'POST',
            header: { 'Authorization': 'Bearer ' + token },
            data: { id: note.value.id }
          })
          if (r.data.success) {
            uni.showToast({ title: '已删除', icon: 'success' })
            setTimeout(() => uni.navigateBack(), 500)
          }
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

function goHistory() {
  uni.navigateTo({ url: '/pages/note/history?id=' + note.value.id })
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: #fff;
  display: flex;
  flex-direction: column;
}
.header {
  padding: 16px;
  border-bottom: 1px solid #eee;
}
.header-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}
.header-time {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
  display: block;
}
.editor {
  flex: 1;
  padding: 16px;
  font-size: 16px;
  line-height: 1.6;
  min-height: 200px;
  border: none;
}
.actions {
  padding: 16px;
  border-top: 1px solid #eee;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.save-btn {
  height: 44px;
  line-height: 44px;
  background-color: #409eff;
  color: #fff;
  border-radius: 8px;
  font-size: 16px;
  text-align: center;
  border: none;
}
.history-btn {
  height: 44px;
  line-height: 44px;
  background-color: #f0f0f0;
  color: #333;
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
  border: none;
}
.delete-btn {
  height: 44px;
  line-height: 44px;
  background-color: #fff;
  color: #e74c3c;
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
  border: 1px solid #e74c3c;
}
</style>
```

---

### Task 13: 前端 — 创建修改历史页面

**Files:**
- Create: `frontend/src/pages/note/history.vue`

- [ ] **Step 1: 创建 history.vue**

```vue
<template>
  <view class="page">
    <view class="header">
      <text class="header-title">修改历史</text>
    </view>

    <view v-if="histories.length === 0" class="empty">
      <text class="empty-text">暂无历史记录</text>
    </view>

    <scroll-view v-else class="list" scroll-y>
      <view class="history-item" v-for="item in histories" :key="item.id">
        <view class="time-tag">{{ formatTime(item.createdAt) }}</view>
        <text class="content-text">{{ item.content }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const histories = ref([])

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const id = currentPage.$page.options?.id || currentPage.options?.id

  if (id) {
    loadHistories(id)
  }
})

async function loadHistories(noteId) {
  const token = uni.getStorageSync('token')
  if (!token) return

  try {
    const res = await uni.request({
      url: '/api/notes/histories?id=' + noteId,
      method: 'GET',
      header: { 'Authorization': 'Bearer ' + token }
    })
    if (res.data.success) {
      histories.value = res.data.data || []
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: #f5f5f5;
}
.header {
  padding: 16px;
  padding-top: calc(16px + env(safe-area-inset-top));
  background-color: #fff;
  border-bottom: 1px solid #eee;
}
.header-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}
.empty {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}
.empty-text {
  color: #999;
  font-size: 16px;
}
.list {
  padding: 12px;
}
.history-item {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}
.time-tag {
  font-size: 12px;
  color: #409eff;
  margin-bottom: 8px;
}
.content-text {
  display: block;
  font-size: 15px;
  color: #333;
  line-height: 1.5;
}
</style>
```

---

### Task 14: 端到端集成测试

- [ ] **Step 1: 停掉旧后端，重新启动**

```bash
# 杀掉旧进程
netstat -ano | grep 8080
taskkill //F //PID <pid>
# 重新启动
cd backend && mvn spring-boot:run
```
等待启动成功（MySQL 建表）

- [ ] **Step 2: 停掉旧前端，重新启动**

```bash
netstat -ano | grep 5173
taskkill //F //PID <pid>
cd frontend && npm run dev
```

- [ ] **Step 3: 后端 API 测试**

```bash
# 登录
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","code":"000000"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo "=== 1. 创建笔记 ==="
curl -s -X POST http://localhost:8080/api/notes/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"content":"我的第一条闪念笔记"}'

echo "=== 2. 创建第二条 ==="
curl -s -X POST http://localhost:8080/api/notes/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"content":"第二天笔记，测试修改功能"}'

echo "=== 3. 获取列表 ==="
curl -s http://localhost:8080/api/notes/list \
  -H "Authorization: Bearer $TOKEN"

echo "=== 4. 更新笔记（假设id=2）==="
curl -s -X POST http://localhost:8080/api/notes/update \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"id":2,"content":"更新后的内容"}'

echo "=== 5. 查看修改历史 ==="
curl -s "http://localhost:8080/api/notes/histories?id=2" \
  -H "Authorization: Bearer $TOKEN"

echo "=== 6. 删除笔记 ==="
curl -s -X POST http://localhost:8080/api/notes/delete \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"id":1}'
```

所有接口应正常返回 JSON。

- [ ] **Step 4: 前端验证**

打开 `http://localhost:5173` → 底部 TabBar 显示"首页"和"我的"
首页 → 点击蓝色"+"按钮 → 进入笔记编辑页 → 输入内容 → 保存
底部"我的" → 显示笔记列表 → 点击查看详情 → 可编辑 → 可查看修改历史

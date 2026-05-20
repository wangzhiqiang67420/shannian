# 闪念笔记 - 前端

基于 uni-app (Vue 3) + Vite + uni-helper 构建的跨平台笔记应用。

## 技术栈

| 组件 | 版本 |
|------|------|
| uni-app | 3.0 |
| Vue | 3.4.21 |
| Vite | 5.2.8 |
| Vue Router | 4.5.1 |
| uni-helper (unh) | 0.3.1 |
| Sass | 1.64.2 |

## 项目结构

```
frontend/
├── index.html                  # H5 入口 HTML
├── package.json                # 依赖与脚本
├── vite.config.js              # Vite 构建配置
├── unh.config.js               # uni-helper 平台配置（默认 H5）
└── src/
    ├── main.js                 # 应用入口，创建 Vue SSR App
    ├── App.vue                 # 根组件，全局样式
    ├── uni.scss                # uni-app 全局 SCSS 变量
    ├── pages.json              # 页面路由 & TabBar 配置
    ├── components/
    │   ├── AppFooter.vue       # 底部导航组件
    │   ├── AppLogos.vue        # Logo 组件
    │   └── InputEntry.vue      # 输入组件
    └── pages/
        ├── index.vue           # 首页 - 萤火虫动画 & 记一笔入口
        ├── login.vue           # 登录页 - 手机号 + 验证码
        ├── mine.vue            # 我的 - 笔记列表（分页加载）
        └── note/
            ├── edit.vue        # 新建笔记（含定位）
            ├── detail.vue      # 笔记详情 & 编辑 & 删除
            └── history.vue     # 修改历史记录
```

## 页面说明

| 页面 | 路径 | 说明 |
|------|------|------|
| 首页 | `/pages/index` | 主页，轮播语录 + "记一笔"按钮 + 笔记计数 |
| 登录 | `/pages/login` | 手机号验证码登录（验证码默认 000000） |
| 我的 | `/pages/mine` | 笔记卡片列表，支持下拉分页加载 |
| 新建笔记 | `/pages/note/edit` | 编辑器 + GPS 定位 + 逆地理编码地址 |
| 笔记详情 | `/pages/note/detail` | 查看/编辑/删除笔记 |
| 修改历史 | `/pages/note/history` | 查看笔记的历史版本列表 |

## 启动步骤

### 1. 安装依赖

```bash
npm install
```

> 依赖较多时建议配置国内镜像：
> ```bash
> npm config set registry https://registry.npmmirror.com
> ```

### 2. 配置代理

在 `unh.config.js` 中配置开发服务器代理，将 API 请求转发到后端。编辑 `vite.config.js`：

```js
export default defineConfig({
  // ... 现有配置
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
```

### 3. 启动开发服务器

```bash
# H5 模式（默认）
npm run dev

# 微信小程序模式
npm run dev:mp-weixin
```

> 通过 `unh.config.js` 中的 `platform.default: 'h5'` 可切换默认平台。

H5 开发服务器默认运行在 `http://localhost:5173`（或 Vite 分配的下一个可用端口）。

### 4. 构建

```bash
# H5 构建
npm run build

# 产物输出到 dist/ 目录
```

## 前置依赖

- **后端服务必须先启动**（默认 `localhost:8080`），前端所有 API 请求需要代理到后端
- **数据库** 需要提前创建（参考 `docs/sql/01-create-tables.sql`）
- **高德地图 API Key** 已在后端 `GeocodeService.java` 中配置，用于笔记位置逆地理编码

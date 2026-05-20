<template>
  <view class="page">
    <view class="header">
      <view class="header-left">
        <text class="header-label">记录想法</text>
        <text class="header-time">{{ timeDisplay }}</text>
      </view>
    </view>

    <view class="editor-wrap">
      <textarea
        v-if="editorReady"
        class="editor"
        :value="content"
        placeholder="此刻在想什么..."
        placeholder-style="color:#c4b8a8;font-size:16px"
        maxlength="-1"
        fixed
        cursor-spacing="80"
        @input="handleContentInput"
      ></textarea>
    </view>

    <!-- Location bar -->
    <view class="location-bar">
      <view class="loc-left" @click="toggleLoc" v-if="locStatus === 'ok'">
        <text class="loc-icon">{{ includeLoc ? '📍' : '⊘' }}</text>
        <text class="loc-addr">{{ includeLoc ? address : '不记录位置' }}</text>
      </view>
      <view class="loc-left" v-else-if="locStatus === 'loading'">
        <text class="loc-icon">◎</text>
        <text class="loc-addr loc-loading">获取位置中...</text>
      </view>
      <view class="loc-row" v-else>
        <view class="loc-left">
          <text class="loc-icon">⊘</text>
          <text class="loc-addr loc-fail">{{ locError || '定位失败' }}</text>
        </view>
        <text class="loc-retry" @click="loadLocation">重试</text>
      </view>
    </view>

    <view class="footer">
      <view class="save-btn" @click="handleSave" hover-class="save-btn--press">
        <text class="save-text">保存笔记</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { apiUrl } from '../../utils/request'

const content = ref('')
const editorReady = ref(true)
const timeDisplay = ref('')
const includeLoc = ref(true)
const address = ref('')
const locStatus = ref('loading') // loading | ok | fail
const locError = ref('')
let latitude = null
let longitude = null
let timer = null

function updateTime() {
  const now = new Date()
  const pad = n => String(n).padStart(2, '0')
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  timeDisplay.value = `${now.getFullYear()}.${pad(now.getMonth()+1)}.${pad(now.getDate())} 周${weekdays[now.getDay()]} ${pad(now.getHours())}:${pad(now.getMinutes())}`
}
updateTime()
timer = setInterval(updateTime, 20000)

onMounted(() => { loadLocation() })

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

function handleContentInput(event) {
  content.value = event.detail?.value || ''
}

async function loadLocation() {
  locStatus.value = 'loading'
  const token = uni.getStorageSync('token')
  if (!token) { locStatus.value = 'fail'; return }

  try {
    // Use native geolocation API to avoid uni-app provider config requirement
    const locRes = await new Promise((resolve, reject) => {
      // #ifdef H5
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (pos) => resolve({ latitude: pos.coords.latitude, longitude: pos.coords.longitude }),
          (err) => reject({ errMsg: err.message }),
          { timeout: 8000, enableHighAccuracy: false }
        )
      } else {
        reject({ errMsg: '浏览器不支持定位' })
      }
      // #endif
      // #ifndef H5
      uni.getLocation({ type: 'gcj02', success: resolve, fail: reject })
      // #endif
    })
    latitude = locRes.latitude
    longitude = locRes.longitude

    const geoRes = await uni.request({
      url: apiUrl(`/api/notes/geocode?lat=${latitude}&lng=${longitude}`),
      method: 'GET',
      header: { 'Authorization': 'Bearer ' + token }
    })
    if (geoRes.data.success && geoRes.data.data.address) {
      address.value = geoRes.data.data.address
      locStatus.value = 'ok'
    } else {
      locStatus.value = 'fail'
    }
  } catch (e) {
    locStatus.value = 'fail'
    locError.value = e.errMsg || e.message || '定位失败'
  }
}

function toggleLoc() {
  includeLoc.value = !includeLoc.value
}

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

  const saveData = { content: content.value.trim() }
  if (includeLoc.value && latitude != null) {
    saveData.latitude = String(latitude)
    saveData.longitude = String(longitude)
    saveData.address = address.value
  }

  try {
    const res = await uni.request({
      url: apiUrl('/api/notes/add'),
      method: 'POST',
      header: { 'Authorization': 'Bearer ' + token },
      data: saveData
    })
    if (res.data.success) {
      uni.showToast({ title: '已保存', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 400)
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
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fcf9f3;
}

.header {
  padding: 18px 20px 14px;
  border-bottom: 1px solid #ede5d6;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-label {
  font-size: 15px;
  font-weight: 600;
  color: #2d2318;
  font-family: Georgia, serif;
  letter-spacing: 2px;
}

.header-time {
  font-size: 12px;
  color: #c4b8a8;
  letter-spacing: 1px;
}

.editor-wrap {
  flex: 1;
  min-height: 0;
  padding: 22px 20px;
  box-sizing: border-box;
}

.editor {
  width: 100%;
  height: 100%;
  font-size: 17px;
  line-height: 1.8;
  color: #3d3226;
  background: transparent;
  border: none;
  font-family: Georgia, "Times New Roman", serif;
  box-sizing: border-box;
}

/* ── Location bar ── */
.location-bar {
  padding: 12px 20px;
  border-top: 1px solid #ede5d6;
  background: #f8f3ea;
}

.loc-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.loc-icon {
  font-size: 16px;
}

.loc-addr {
  font-size: 13px;
  color: #6b5640;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.loc-loading {
  color: #c4b8a8;
  animation: pulse 1.5s ease-in-out infinite;
}

.loc-fail {
  color: #b8a58c;
}

.loc-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loc-retry {
  font-size: 12px;
  color: #8c6a47;
  padding: 2px 10px;
  border: 1px solid #d6c4a8;
  border-radius: 10px;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.footer {
  padding: 16px 20px;
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
  border-top: 1px solid #ede5d6;
}

.save-btn {
  height: 48px;
  border-radius: 24px;
  background: linear-gradient(135deg, #2d2318, #40301e);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 3px 12px rgba(45, 35, 24, 0.1);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.save-btn--press {
  transform: scale(0.97);
  box-shadow: 0 1px 6px rgba(45, 35, 24, 0.06);
}

.save-text {
  color: #e8d5b7;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 4px;
}
</style>

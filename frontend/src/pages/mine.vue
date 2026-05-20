<template>
  <view class="page">
    <view class="header">
      <view class="header-left">
        <view class="header-mark" />
        <text class="header-title">闪念</text>
      </view>
      <text v-if="phone" class="header-phone">{{ maskedPhone }}</text>
    </view>

    <view
      v-if="notes.length > 0"
      class="list"
    >
      <view class="list-inner">
        <view
          class="note-card"
          v-for="(item, index) in notes"
          :key="item.id"
          :style="{ animationDelay: ((index % 20) * 0.05) + 's' }"
          @click="goDetail(item.id)"
          hover-class="note-card--hover"
        >
          <view class="card-top">
            <text class="card-time">{{ formatTime(item.updatedAt) }}</text>
            <text class="card-addr" v-if="item.address">{{ item.address }}</text>
          </view>
          <text class="card-content">{{ truncate(item.content) }}</text>
        </view>

        <view class="load-status" v-if="loading">
          <text class="load-text">加载中...</text>
        </view>
        <view class="load-status load-more-btn" v-else-if="hasMore" @click="loadMore">
          <text class="load-text">加载更多</text>
        </view>
        <view class="load-status" v-else>
          <text class="load-text">— 没有更多了 —</text>
        </view>
      </view>
    </view>

    <view v-else class="empty">
      <text class="empty-icon">✎</text>
      <text class="empty-title">还没有笔记</text>
      <text class="empty-sub">去首页开始记录第一个想法吧</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'

const notes = ref([])
const phone = ref('')
const page = ref(1)
const hasMore = ref(true)
const loading = ref(false)

const maskedPhone = computed(() => {
  const p = phone.value
  if (!p || p.length !== 11) return p
  return p.slice(0, 3) + '****' + p.slice(7)
})

onShow(() => {
  page.value = 1
  notes.value = []
  hasMore.value = true
  const token = uni.getStorageSync('token')
  const userInfo = uni.getStorageSync('userInfo')
  if (token) {
    phone.value = userInfo?.phone || ''
    loadNotes(token)
  }
})

async function loadNotes(token) {
  loading.value = true
  try {
    const res = await uni.request({
      url: `/api/notes/list?page=${page.value}&size=5`,
      method: 'GET',
      header: { 'Authorization': 'Bearer ' + token }
    })
    if (res.data.success) {
      notes.value = res.data.data.records || []
      hasMore.value = res.data.data.hasMore
    }
  } catch (e) {
    // ignore
  }
  loading.value = false
}

async function loadMore() {
  if (loading.value || !hasMore.value) return
  page.value++
  loading.value = true
  const token = uni.getStorageSync('token')
  try {
    const res = await uni.request({
      url: `/api/notes/list?page=${page.value}&size=5`,
      method: 'GET',
      header: { 'Authorization': 'Bearer ' + token }
    })
    if (res.data.success) {
      notes.value = [...notes.value, ...(res.data.data.records || [])]
      hasMore.value = res.data.data.hasMore
    }
  } catch (e) {
    page.value--
  }
  loading.value = false
}

function goDetail(id) {
  uni.navigateTo({ url: '/pages/note/detail?id=' + id })
}

function truncate(text) {
  if (!text) return ''
  if (text.length <= 16) return text
  return text.slice(0, 16) + '...'
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const pad = n => String(n).padStart(2, '0')
  const now = new Date()
  const isToday = d.toDateString() === now.toDateString()
  const timeStr = `${pad(d.getHours())}:${pad(d.getMinutes())}`
  const dateStr = `${d.getFullYear()}.${pad(d.getMonth()+1)}.${pad(d.getDate())}`
  return isToday ? `今天 ${timeStr}` : dateStr
}
</script>

<style scoped>
.page {
  height: calc(100vh - 50px);
  display: flex;
  flex-direction: column;
  background: #faf6f0;
  overflow: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 24px 16px;
  background: #faf6f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-mark {
  width: 4px; height: 22px;
  background: #b8863f;
  border-radius: 2px;
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  color: #3d3226;
  font-family: Georgia, "Times New Roman", serif;
  letter-spacing: 4px;
}

.subtitle {
  font-size: 13px;
  color: #9e8a73;
  margin-top: 2px;
  letter-spacing: 3px;
}

.header-phone {
  font-size: 13px;
  color: #c4b8a8;
}

.list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.list-inner {
  padding: 12px 16px 24px;
}

.load-status {
  text-align: center;
  padding: 20px 0;
}

.load-more-btn {
  border: 1px solid #d6c4a8;
  border-radius: 12px;
  padding: 14px 0;
  margin-top: 4px;
}

.load-text {
  font-size: 12px;
  color: #c4b8a8;
}

.note-card {
  box-sizing: border-box;
  width: 100%;
  background: #fffefb;
  border-radius: 16px;
  padding: 18px 20px;
  margin-bottom: 12px;
  box-shadow: 0 2px 12px rgba(45, 35, 24, 0.05);
  animation: fadeInUp 0.35s ease both;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  overflow: hidden;
}

.note-card--hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(45, 35, 24, 0.10);
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.card-addr {
  font-size: 11px;
  color: #b8a58c;
  max-width: 50%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-time {
  font-size: 12px;
  color: #9e8a73;
  letter-spacing: 0.5px;
}

.card-content {
  display: block;
  font-size: 15px;
  color: #3d3226;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.empty-icon {
  font-size: 48px;
  color: #d6cbba;
  font-family: Georgia, serif;
}

.empty-title {
  font-size: 17px;
  color: #8c6a47;
  font-weight: 500;
}

.empty-sub {
  font-size: 13px;
  color: #c4b8a8;
}
</style>

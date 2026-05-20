<template>
  <view class="page">
    <view class="header">
      <text class="header-label">修改历史</text>
      <text class="header-count" v-if="histories.length > 0">{{ histories.length }} 个版本</text>
    </view>

    <view v-if="histories.length === 0" class="empty">
      <text class="empty-text">暂无历史记录</text>
    </view>

    <scroll-view v-else class="list" scroll-y enhanced show-scrollbar="false">
      <view class="timeline" />
      <view
        class="history-item"
        v-for="(item, index) in histories"
        :key="item.id"
        :style="{ animationDelay: (index * 0.06) + 's' }"
      >
        <view class="dot" :class="{ 'dot--first': index === 0 }" />
        <view class="item-body">
          <text class="item-time">{{ formatTime(item.createdAt) }}</text>
          <text class="item-content">{{ item.content }}</text>
        </view>
      </view>

      <view class="list-bottom" />
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { apiUrl } from '../../utils/request'

const histories = ref([])

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const id = currentPage.$page.options?.id || currentPage.options?.id
  if (id) loadHistories(id)
})

async function loadHistories(noteId) {
  const token = uni.getStorageSync('token')
  if (!token) return
  try {
    const res = await uni.request({
      url: apiUrl('/api/notes/histories?id=' + noteId),
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
  return `${d.getFullYear()}.${pad(d.getMonth()+1)}.${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>

<style scoped>
.page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #faf6f0;
}

.header {
  padding: 18px 20px 14px;
  border-bottom: 1px solid #ede5d6;
  background: #fcf9f3;
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.header-label {
  font-size: 15px;
  font-weight: 600;
  color: #2d2318;
  font-family: Georgia, serif;
  letter-spacing: 2px;
}

.header-count {
  font-size: 12px;
  color: #9e8a73;
}

.empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-text {
  color: #c4b8a8;
  font-size: 15px;
}

.list {
  flex: 1;
  padding: 0 24px 12px;
  position: relative;
}

.list-bottom {
  height: 20px;
}

.timeline {
  position: absolute;
  left: 30px;
  top: 8px;
  bottom: 8px;
  width: 1px;
  background: linear-gradient(to bottom, #e8d5b7, #f0e7d8, transparent);
}

.history-item {
  display: flex;
  gap: 16px;
  padding: 18px 0;
  animation: fadeIn 0.4s ease both;
  position: relative;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateX(-8px); }
  to { opacity: 1; transform: translateX(0); }
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 5px;
  background: #d6cbba;
  border: 2px solid #f0e7d8;
  margin-top: 6px;
  flex-shrink: 0;
  z-index: 1;
}

.dot--first {
  background: #bf8b3c;
  border-color: #f0e7d8;
  box-shadow: 0 0 0 3px rgba(191, 139, 60, 0.15);
}

.item-body {
  flex: 1;
}

.item-time {
  display: block;
  font-size: 12px;
  color: #9e8a73;
  margin-bottom: 6px;
  letter-spacing: 0.5px;
}

.item-content {
  display: block;
  font-size: 15px;
  color: #3d3226;
  line-height: 1.7;
  font-family: Georgia, "Times New Roman", serif;
}
</style>

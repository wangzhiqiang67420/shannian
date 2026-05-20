<template>
  <view class="page">
    <view class="header">
      <view class="header-left">
        <text class="header-label">闪念详情</text>
        <text class="header-time">{{ formatTime(note?.createdAt) }}</text>
        <text class="header-addr" v-if="note?.address">📍 {{ note.address }}</text>
      </view>
    </view>

    <textarea
      class="editor"
      :value="content"
      placeholder="加载中..."
      placeholder-style="color:#c4b8a8;font-size:16px"
      @input="content = $event.detail.value"
      auto-height
    />

    <view class="actions">
      <view
        class="save-btn"
        v-if="hasChanged"
        @click="handleUpdate"
        hover-class="save-btn--press"
      >
        <text class="save-text">保存修改</text>
      </view>

      <view class="action-row">
        <view class="action-btn action-btn--outline" @click="goHistory">
          <text class="action-text">修改历史</text>
        </view>
        <view class="action-btn action-btn--danger" @click="handleDelete">
          <text class="action-text action-text--danger">删除笔记</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const note = ref(null)
const content = ref('')

const hasChanged = computed(() => note.value && content.value !== note.value.content)

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const id = currentPage.$page.options?.id || currentPage.options?.id
  if (id) loadDetail(id)
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
      uni.showToast({ title: '已保存', icon: 'success' })
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
            setTimeout(() => uni.navigateBack(), 400)
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
  return `${d.getFullYear()}.${pad(d.getMonth()+1)}.${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
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
  margin-top: 4px;
  display: block;
}

.header-addr {
  font-size: 12px;
  color: #9e8a73;
  margin-top: 4px;
  display: block;
}

.editor {
  flex: 1;
  padding: 22px 20px;
  font-size: 17px;
  line-height: 1.8;
  color: #3d3226;
  background: transparent;
  border: none;
  font-family: Georgia, "Times New Roman", serif;
  min-height: 0;
}

.actions {
  padding: 16px 20px;
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
  border-top: 1px solid #ede5d6;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.save-btn {
  height: 48px;
  border-radius: 24px;
  background: linear-gradient(135deg, #2d2318, #40301e);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 3px 12px rgba(45, 35, 24, 0.1);
  transition: transform 0.2s ease;
}

.save-btn--press {
  transform: scale(0.97);
}

.save-text {
  color: #e8d5b7;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 4px;
}

.action-row {
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  height: 44px;
  border-radius: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease;
}

.action-btn--outline {
  border: 1px solid #d6cbba;
  background: transparent;
}

.action-btn--danger {
  border: 1px solid #d4a998;
  background: transparent;
}

.action-text {
  font-size: 14px;
  color: #8c6a47;
  letter-spacing: 2px;
}

.action-text--danger {
  color: #b85c4a;
}
</style>

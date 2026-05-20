<template>
  <view class="page">
    <view class="login-link" v-if="!isLoggedIn" @click="goLogin">
      <text class="login-link-text">登录</text>
    </view>

    <view class="lights">
      <view class="light" v-for="i in 10" :key="i"
        :style="{
          left: (8 + i * 8.5) + '%',
          top: (15 + i * 7.1) + '%',
          animationDelay: (i * 0.7) + 's',
          animationDuration: (3 + i * 0.5) + 's',
        }"
      />
    </view>

    <scroll-view class="body" scroll-y enhanced show-scrollbar="false">
      <text class="moment-text" :key="quoteKey">{{ quotes[quoteIdx] }}</text>
      <view class="divider" />
      <text class="date-text">{{ todayText }}</text>

      <view class="capture-wrap">
        <view class="ripple ripple--1" />
        <view class="ripple ripple--2" />
        <view class="capture-btn" @click="goEdit" hover-class="capture-btn--down">
          <view class="btn-face">
            <text class="btn-icon">+</text>
          </view>
          <text class="btn-label">{{ isLoggedIn ? '记一笔' : '登录后记录' }}</text>
        </view>
      </view>

      <text class="count-text" v-if="!isLoggedIn">登录后开始记录你的念头</text>
      <text class="count-text" v-else-if="noteCount > 0">已留下 {{ noteCount }} 个念头</text>
      <text class="count-text" v-else>开始记录你的第一个念头</text>

      <view class="spacer" />
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { apiUrl } from '../utils/request'

const quotes = [
  '念头像萤火，一闪而过',
  '不写下来，就消失了',
  '此刻，你在想什么？',
  '最好的想法总是不请自来',
  '哪怕只有一句话',
]
const quoteIdx = ref(0)
const quoteKey = ref(0)
let quoteTimer = null
const todayText = ref('')
const noteCount = ref(0)
const isLoggedIn = ref(false)

onMounted(() => {
  const now = new Date()
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  todayText.value = `${now.getFullYear()}年${now.getMonth()+1}月${now.getDate()}日 · 星期${weekdays[now.getDay()]}`
  quoteTimer = setInterval(() => {
    quoteIdx.value = (quoteIdx.value + 1) % quotes.length
    quoteKey.value++
  }, 6000)
})

onShow(() => {
  isLoggedIn.value = !!uni.getStorageSync('token')
  noteCount.value = 0
  loadCount()
})

onUnmounted(() => { if (quoteTimer) clearInterval(quoteTimer) })

async function loadCount() {
  const token = uni.getStorageSync('token')
  if (!token) return
  try {
    const res = await uni.request({ url: apiUrl('/api/notes/list'), method: 'GET', header: { 'Authorization': 'Bearer ' + token } })
    if (!res.data.success) return
    if (Array.isArray(res.data.data)) {
      noteCount.value = res.data.data.length
    } else {
      noteCount.value = Number(res.data.data?.total || 0)
    }
  } catch (e) { /* silent */ }
}

function goEdit() {
  if (!isLoggedIn.value) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/note/edit' })
}

function goLogin() {
  uni.navigateTo({ url: '/pages/login' })
}
</script>

<style scoped>
.page {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, #fdfaf4 0%, #f8f1e6 55%, #f0e4d2 100%);
  overflow: hidden;
}

.lights { position: absolute; inset: 0; pointer-events: none; z-index: 0; }

.login-link {
  position: absolute;
  top: 18px;
  right: 20px;
  z-index: 5;
  min-width: 58px;
  height: 32px;
  border: 1px solid #d6c4a8;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 254, 251, 0.78);
}

.login-link-text {
  font-size: 13px;
  color: #8c6a47;
  letter-spacing: 2px;
}

.light {
  position: absolute; width: 3px; height: 3px; border-radius: 50%; background: #d4a854;
  box-shadow: 0 0 6px 3px rgba(212,168,84,0.30), 0 0 12px 6px rgba(212,168,84,0.10);
  animation: firefly ease-in-out infinite alternate;
}

@keyframes firefly {
  0%   { transform: translate(-4px,0) scale(1); opacity: 0.25; }
  50%  { transform: translate(5px,-7px) scale(1.5); opacity: 0.75; }
  100% { transform: translate(2px,-3px) scale(1); opacity: 0.35; }
}

.body {
  position: relative; z-index: 2;
  padding: 80px 30px 30px;
  height: 100%; box-sizing: border-box;
}

.moment-text {
  display: block; text-align: center;
  font-size: 17px; color: #5c4a3a; font-weight: 500;
  letter-spacing: 2px; font-family: Georgia,"Times New Roman",serif;
  animation: fadeText 0.6s ease;
}

@keyframes fadeText {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}

.divider { width: 20px; height: 1px; background: #d4c4a8; margin: 18px auto; }

.date-text {
  display: block; text-align: center;
  font-size: 12px; color: #b8a58c; letter-spacing: 1.5px;
}

.capture-wrap {
  margin-top: 56px; position: relative;
  display: flex; align-items: center; justify-content: center;
}

.ripple {
  position: absolute; border-radius: 50%;
  border: 1px solid rgba(196,160,100,0.2);
  animation: rippleOut ease-in-out infinite;
}
.ripple--1 { width: 190px; height: 190px; animation-duration: 3s; }
.ripple--2 { width: 260px; height: 260px; animation-duration: 4s; animation-delay: 0.6s; border-color: rgba(196,160,100,0.1); }

@keyframes rippleOut {
  0%,100% { transform: scale(0.9); opacity: 0.5; }
  50%     { transform: scale(1.06); opacity: 0.12; }
}

.capture-btn { display: flex; flex-direction: column; align-items: center; gap: 16px; z-index: 1; transition: transform 0.25s cubic-bezier(0.34,1.56,0.64,1); }
.capture-btn--down { transform: scale(0.93); }

.btn-face {
  width: 110px; height: 110px; border-radius: 55px;
  background: linear-gradient(145deg,#faf4ea,#efe0cb);
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 6px 22px rgba(140,100,60,0.10), 0 2px 6px rgba(140,100,60,0.05), inset 0 1px 0 rgba(255,255,255,0.5);
  border: 1px solid rgba(196,160,110,0.25);
}

.btn-icon { font-size: 44px; color: #b8863f; line-height: 1; font-weight: 300; }
.btn-label { font-size: 14px; color: #6b5640; font-weight: 600; letter-spacing: 6px; font-family: Georgia,"Times New Roman",serif; }

.count-text {
  display: block; text-align: center; margin-top: 44px;
  font-size: 12px; color: #c4b096; letter-spacing: 1.5px;
}

.spacer { height: 30px; }
</style>

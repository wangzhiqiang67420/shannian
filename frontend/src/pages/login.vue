<template>
  <view class="page">
    <view class="hero">
      <text class="brand-mark">◇</text>
      <text class="brand-name">闪念笔记</text>
      <text class="tagline">登录后开始记录</text>
    </view>

    <view class="form">
      <view
        class="login-btn"
        :class="{ 'login-btn--disabled': loading }"
        @click="handleWechatLogin"
        hover-class="login-btn--press"
      >
        <text class="login-btn-text">{{ loading ? '登录中' : '微信登录' }}</text>
      </view>

      <text v-if="errorMsg" class="error">{{ errorMsg }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { apiUrl } from '../utils/request'

const loading = ref(false)
const errorMsg = ref('')

async function handleWechatLogin() {
  if (loading.value) return
  loading.value = true
  errorMsg.value = ''
  try {
    const loginRes = await new Promise((resolve, reject) => {
      uni.login({ provider: 'weixin', success: resolve, fail: reject })
    })
    if (!loginRes.code) {
      throw new Error('未获取到微信登录凭证')
    }

    const res = await uni.request({
      url: apiUrl('/api/auth/wx-login'),
      method: 'POST',
      data: { code: loginRes.code }
    })
    if (res.statusCode >= 200 && res.statusCode < 300 && res.data.success) {
      uni.setStorageSync('token', res.data.token)
      uni.setStorageSync('userInfo', res.data.user)
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => uni.reLaunch({ url: '/pages/index' }), 400)
    } else {
      errorMsg.value = res.data?.message || `登录失败(${res.statusCode})`
    }
  } catch (e) {
    errorMsg.value = e.errMsg || e.message || '微信登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(170deg, #fcf9f3 0%, #f7f1e8 40%, #f0e7d8 100%);
  padding: 40px 28px;
}

.hero {
  text-align: center;
  margin-bottom: 48px;
}

.brand-mark {
  font-size: 28px;
  color: #bf8b3c;
  font-family: Georgia, serif;
}

.brand-name {
  display: block;
  margin-top: 6px;
  font-size: 28px;
  font-weight: 700;
  color: #2d2318;
  font-family: Georgia, "Times New Roman", serif;
  letter-spacing: 4px;
}

.tagline {
  display: block;
  margin-top: 8px;
  font-size: 13px;
  color: #9e8a73;
  letter-spacing: 2px;
}

.form {
  width: 100%;
  max-width: 360px;
}

.login-btn {
  height: 50px;
  border-radius: 25px;
  background: linear-gradient(135deg, #2d2318, #4a3726);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(45, 35, 24, 0.12);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.login-btn--press {
  transform: scale(0.97);
  box-shadow: 0 2px 8px rgba(45, 35, 24, 0.08);
}

.login-btn--disabled {
  opacity: 0.65;
}

.login-btn-text {
  color: #e8d5b7;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
}

.error {
  display: block;
  color: #c84a3b;
  font-size: 13px;
  text-align: center;
  margin-top: 16px;
}

</style>

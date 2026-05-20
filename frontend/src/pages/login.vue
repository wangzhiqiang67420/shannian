<template>
  <view class="page">
    <view class="hero">
      <text class="brand-mark">◇</text>
      <text class="brand-name">闪念笔记</text>
      <text class="tagline">登录后开始记录</text>
    </view>

    <view class="form">
      <view class="input-wrap">
        <text class="input-label">手机号</text>
        <input
          class="input"
          :value="phone"
          type="number"
          maxlength="11"
          placeholder="请输入手机号"
          @input="phone = $event.detail.value"
        />
      </view>

      <view class="input-wrap">
        <text class="input-label">验证码</text>
        <view class="code-row">
          <input
            class="input code-input"
            :value="code"
            type="number"
            maxlength="6"
            placeholder="6位验证码"
            @input="code = $event.detail.value"
          />
          <view
            class="code-btn"
            :class="{ 'code-btn--counting': countdown > 0 }"
            @click="sendCode"
          >
            {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
          </view>
        </view>
      </view>

      <view class="login-btn" @click="handleLogin" hover-class="login-btn--press">
        <text class="login-btn-text">登 录</text>
      </view>

      <text v-if="errorMsg" class="error">{{ errorMsg }}</text>

      <view class="hint">
        <text class="hint-text">验证码默认 000000</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'

const phone = ref('')
const code = ref('')
const countdown = ref(0)
const errorMsg = ref('')
let timer = null

onUnmounted(() => {
  if (timer) { clearInterval(timer); timer = null }
})

async function sendCode() {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    errorMsg.value = '请输入正确的手机号'
    return
  }
  errorMsg.value = ''
  try {
    const res = await uni.request({
      url: '/api/auth/send-code',
      method: 'POST',
      data: { phone: phone.value }
    })
    if (res.data.success) {
      uni.showToast({ title: '验证码已发送', icon: 'success' })
      countdown.value = 60
      timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) { clearInterval(timer); timer = null }
      }, 1000)
    } else {
      errorMsg.value = res.data.message || '发送失败'
    }
  } catch (e) {
    errorMsg.value = '发送验证码失败'
  }
}

async function handleLogin() {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    errorMsg.value = '请输入正确的手机号'
    return
  }
  if (!code.value || code.value.length !== 6) {
    errorMsg.value = '请输入6位验证码'
    return
  }
  errorMsg.value = ''
  try {
    const res = await uni.request({
      url: '/api/auth/login',
      method: 'POST',
      data: { phone: phone.value, code: code.value }
    })
    if (res.data.success) {
      uni.setStorageSync('token', res.data.token)
      uni.setStorageSync('userInfo', res.data.user)
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => uni.reLaunch({ url: '/pages/index' }), 400)
    } else {
      errorMsg.value = res.data.message || '登录失败'
    }
  } catch (e) {
    errorMsg.value = '登录失败，请检查网络'
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

.input-wrap {
  margin-bottom: 24px;
}

.input-label {
  display: block;
  font-size: 12px;
  color: #8c6a47;
  letter-spacing: 2px;
  margin-bottom: 10px;
  text-transform: uppercase;
}

.input {
  width: 100%;
  height: 48px;
  border: none;
  border-bottom: 1.5px solid #e0d5c5;
  background: transparent;
  font-size: 17px;
  color: #2d2318;
  padding: 0 4px;
  box-sizing: border-box;
  font-family: Georgia, serif;
  transition: border-color 0.2s ease;
}

.input:focus {
  border-bottom-color: #bf8b3c;
}

.code-row {
  display: flex;
  gap: 16px;
  align-items: center;
}

.code-input {
  flex: 1;
  margin-bottom: 0;
}

.code-btn {
  width: 110px;
  height: 36px;
  line-height: 36px;
  text-align: center;
  border-radius: 18px;
  font-size: 13px;
  color: #8c6a47;
  background: #f0e7d8;
  transition: all 0.2s ease;
}

.code-btn--counting {
  color: #c4b8a8;
  background: #f5efe5;
}

.login-btn {
  margin-top: 36px;
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

.login-btn-text {
  color: #e8d5b7;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 8px;
}

.error {
  display: block;
  color: #c84a3b;
  font-size: 13px;
  text-align: center;
  margin-top: 16px;
}

.hint {
  margin-top: 24px;
  text-align: center;
}

.hint-text {
  font-size: 12px;
  color: #d6cbba;
}
</style>

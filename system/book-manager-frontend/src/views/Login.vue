<template>
  <div class="login-page">
    <div class="login-left">
      <div class="brand">
        <div class="brand-icon">
          <svg viewBox="0 0 80 80" fill="none"><rect width="80" height="80" rx="16" fill="rgba(255,255,255,0.2)"/><path d="M20 24h40v6H20zm0 14h32v6H20zm0 14h36v6H20z" fill="white" opacity="0.9"/></svg>
        </div>
        <h1>图书借阅<br>管理系统</h1>
        <p>智能化图书管理与借阅平台<br>让知识触手可及</p>
      </div>
      <div class="features">
        <div class="feature"><span class="dot"></span>海量图书资源</div>
        <div class="feature"><span class="dot"></span>一键借阅归还</div>
        <div class="feature"><span class="dot"></span>智能数据统计</div>
      </div>
    </div>
    <div class="login-right">
      <div class="login-card">
        <div class="card-header">
          <h2>欢迎回来</h2>
          <p>请登录您的账号继续使用</p>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" size="large" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" :prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
              登 录
            </el-button>
          </el-form-item>
        </el-form>
        <div class="card-footer">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { login } from '../api'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ username: '', password: '' })

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function handleLogin() {
  formRef.value?.validate(async valid => {
    if (!valid) return
    loading.value = true
    try {
      const res: any = await login(form)
      const { token, user } = res.data
      userStore.setToken(token)
      userStore.setUserInfo(user)
      ElMessage({ message: `登录成功，欢迎 ${user.name}`, type: 'success' })
      setTimeout(() => router.push(user.role === 'admin' ? '/admin' : '/user'), 300)
    } catch {
      /* handled */
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
}
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #3D6FD9 0%, #2B5AC2 30%, #1E40AF 70%, #172554 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 80px 60px;
  position: relative;
  overflow: hidden;
}
.login-left::before {
  content: '';
  position: absolute;
  top: -100px;
  right: -100px;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: rgba(255,255,255,0.04);
}
.login-left::after {
  content: '';
  position: absolute;
  bottom: -150px;
  left: -80px;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: rgba(255,255,255,0.03);
}
.brand {
  position: relative;
  z-index: 1;
  margin-bottom: 40px;
}
.brand-icon {
  width: 80px;
  height: 80px;
  margin-bottom: 24px;
}
.brand h1 {
  font-size: 36px;
  font-weight: 800;
  color: #fff;
  line-height: 1.2;
  margin-bottom: 16px;
  letter-spacing: -0.5px;
}
.brand p {
  font-size: 16px;
  color: rgba(255,255,255,0.7);
  line-height: 1.7;
}
.features {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 24px;
}
.feature {
  color: rgba(255,255,255,0.8);
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255,255,255,0.5);
}
.login-right {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #F8FAFC;
}
.login-card {
  width: 100%;
  max-width: 400px;
}
.card-header {
  margin-bottom: 36px;
}
.card-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}
.card-header p {
  font-size: 15px;
  color: var(--text-secondary);
}
.login-btn {
  width: 100%;
  height: 46px !important;
  font-size: 16px !important;
  margin-top: 8px;
}
.card-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: var(--text-secondary);
}
.card-footer a {
  font-weight: 600;
}
</style>

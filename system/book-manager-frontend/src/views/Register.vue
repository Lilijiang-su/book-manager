<template>
  <div class="register-page">
    <div class="register-left">
      <div class="brand">
        <div class="brand-icon">
          <svg viewBox="0 0 80 80" fill="none"><rect width="80" height="80" rx="16" fill="rgba(255,255,255,0.2)"/><path d="M28 30h24v6H28zm0 14h24v6H28z" fill="white" opacity="0.8"/><circle cx="52" cy="52" r="10" fill="#22C55E" opacity="0.9"/><path d="M49 52l2 2 4-4" stroke="white" stroke-width="2" fill="none"/></svg>
        </div>
        <h1>加入我们</h1>
        <p>注册图书借阅管理系统<br>开启阅读之旅</p>
        <div class="back-home">
          <router-link to="/login">← 返回登录</router-link>
        </div>
      </div>
    </div>
    <div class="register-right">
      <div class="register-card">
        <div class="card-header">
          <h2>创建账号</h2>
          <p>填写信息完成注册</p>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item prop="username">
                <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="name">
                <el-input v-model="form.name" placeholder="真实姓名" size="large" :prefix-icon="User" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="设置密码" size="large" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" size="large" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item prop="phone">
            <el-input v-model="form.phone" placeholder="手机号" size="large" :prefix-icon="Phone" />
          </el-form-item>
          <el-form-item prop="email">
            <el-input v-model="form.email" placeholder="电子邮箱" size="large" :prefix-icon="Message" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" class="register-btn" :loading="loading" @click="handleRegister">
              注 册
            </el-button>
          </el-form-item>
        </el-form>
        <div class="card-footer">
          已有账号？<router-link to="/login">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Phone, Message } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { register } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ username: '', password: '', confirmPassword: '', name: '', phone: '', email: '' })

const validatePass = (_rule: any, value: string, callback: any) => {
  if (value !== form.password) callback(new Error('两次输入的密码不一致'))
  else callback()
}

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validatePass, trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

async function handleRegister() {
  formRef.value?.validate(async valid => {
    if (!valid) return
    loading.value = true
    try {
      await register({ username: form.username, password: form.password, name: form.name, phone: form.phone, email: form.email })
      ElMessage.success('注册成功，请登录')
      setTimeout(() => router.push('/login'), 500)
    } catch {
      /* handled */
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
}
.register-left {
  width: 440px;
  background: linear-gradient(135deg, #3D6FD9 0%, #2B5AC2 30%, #1E40AF 70%, #172554 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px 50px;
  position: relative;
  overflow: hidden;
}
.register-left::before {
  content: '';
  position: absolute;
  top: -120px;
  right: -120px;
  width: 350px;
  height: 350px;
  border-radius: 50%;
  background: rgba(255,255,255,0.04);
}
.brand { position: relative; z-index: 1; }
.brand-icon { width: 70px; height: 70px; margin-bottom: 20px; }
.brand h1 { font-size: 32px; font-weight: 800; color: #fff; margin-bottom: 12px; }
.brand p { font-size: 15px; color: rgba(255,255,255,0.7); line-height: 1.7; margin-bottom: 32px; }
.back-home a { color: rgba(255,255,255,0.7); font-size: 14px; font-weight: 500; }
.back-home a:hover { color: #fff; }
.register-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #F8FAFC;
}
.register-card {
  width: 100%;
  max-width: 520px;
}
.card-header { margin-bottom: 32px; }
.card-header h2 { font-size: 28px; font-weight: 700; color: var(--text-primary); margin-bottom: 8px; }
.card-header p { font-size: 15px; color: var(--text-secondary); }
.register-btn { width: 100%; height: 46px !important; font-size: 16px !important; margin-top: 8px; }
.card-footer { text-align: center; margin-top: 24px; font-size: 14px; color: var(--text-secondary); }
.card-footer a { font-weight: 600; }
</style>

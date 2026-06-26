<template>
  <div class="profile">
    <div class="page-title">个人信息</div>
    <el-row :gutter="24">
      <el-col :span="8">
        <el-card shadow="never" class="profile-card">
          <div class="avatar-section">
            <el-avatar :size="80" icon="UserFilled" />
            <h3>{{ userStore.userInfo?.name }}</h3>
            <el-tag type="primary" effect="plain" round>普通用户</el-tag>
          </div>
          <div class="info-list">
            <div class="info-item">
              <el-icon color="var(--text-muted)"><User /></el-icon>
              <span class="info-label">用户名</span>
              <span class="info-value">{{ form.username }}</span>
            </div>
            <div class="info-item">
              <el-icon color="var(--text-muted)"><Phone /></el-icon>
              <span class="info-label">手机号</span>
              <span class="info-value">{{ form.phone || '未设置' }}</span>
            </div>
            <div class="info-item">
              <el-icon color="var(--text-muted)"><Message /></el-icon>
              <span class="info-label">邮箱</span>
              <span class="info-value">{{ form.email || '未设置' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">编辑资料</span>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
            <el-form-item prop="name">
              <el-input v-model="form.name" size="large" placeholder="真实姓名">
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="phone">
              <el-input v-model="form.phone" size="large" placeholder="手机号码">
                <template #prefix><el-icon><Phone /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="email">
              <el-input v-model="form.email" size="large" placeholder="电子邮箱">
                <template #prefix><el-icon><Message /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" :loading="saveLoading" @click="handleSave" style="width: 100%">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getUserInfo, updateUser } from '../../api'
import { useUserStore } from '../../store/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Phone, Message } from '@element-plus/icons-vue'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const saveLoading = ref(false)
const form = ref({ username: '', name: '', phone: '', email: '' })

const rules: FormRules = { name: [{ required: true, message: '请输入姓名', trigger: 'blur' }] }

async function fetchInfo() {
  try {
    const res = await getUserInfo()
    const d = res.data
    form.value = { username: d.username, name: d.name, phone: d.phone || '', email: d.email || '' }
  } catch { /* handled */ }
}

async function handleSave() {
  formRef.value?.validate(async valid => {
    if (!valid) return
    saveLoading.value = true
    try {
      await updateUser({ id: userStore.userInfo.id, name: form.value.name, phone: form.value.phone, email: form.value.email })
      ElMessage.success('保存成功')
      userStore.setUserInfo({ ...userStore.userInfo, ...form.value })
    } finally { saveLoading.value = false }
  })
}

onMounted(fetchInfo)
</script>

<style scoped>
.profile { animation: fadeInUp 0.4s ease; }
.card-title { font-size: 16px; font-weight: 600; }
.avatar-section { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 24px 0 16px; }
.avatar-section h3 { font-size: 18px; font-weight: 600; }
.info-list { padding: 16px 0; border-top: 1px solid var(--border-color); }
.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  font-size: 14px;
}
.info-item:not(:last-child) { border-bottom: 1px dashed var(--border-color); }
.info-label { color: var(--text-muted); width: 60px; }
.info-value { color: var(--text-primary); font-weight: 500; margin-left: auto; }
</style>

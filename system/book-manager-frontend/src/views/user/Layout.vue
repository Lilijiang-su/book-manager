<template>
  <div class="user-layout">
    <el-container>
      <el-aside width="240px">
        <div class="logo-area">
          <div class="logo-icon">
            <svg viewBox="0 0 40 40" fill="none"><rect width="40" height="40" rx="8" fill="rgba(255,255,255,0.15)"/><path d="M10 14h20v3H10zm0 6h16v3H10zm0 6h18v3H10z" fill="white" opacity="0.9"/></svg>
          </div>
          <div class="logo-text">
            <span class="logo-title">图书借阅</span>
            <span class="logo-sub">Library</span>
          </div>
        </div>
        <div class="user-card">
          <el-avatar :size="40" icon="UserFilled" />
          <div class="user-info">
            <span class="user-name">{{ userStore.userInfo?.name || '用户' }}</span>
            <span class="user-role">普通用户</span>
          </div>
        </div>
        <el-menu
          :default-active="route.path"
          router
          background-color="transparent"
          text-color="rgba(255,255,255,0.6)"
          active-text-color="#fff"
        >
          <el-menu-item index="/user/books">
            <el-icon><Reading /></el-icon>
            <span>图书浏览</span>
          </el-menu-item>
          <el-menu-item index="/user/my-borrows">
            <el-icon><Document /></el-icon>
            <span>我的借阅</span>
          </el-menu-item>
          <el-menu-item index="/user/profile">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
        </el-menu>
        <div class="sidebar-footer">
          <el-button text class="logout-btn" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </el-button>
        </div>
      </el-aside>
      <el-container>
        <el-header>
          <div class="header-title">{{ currentPageTitle }}</div>
        </el-header>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { SwitchButton } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pageTitles: Record<string, string> = {
  '/user/books': '图书浏览',
  '/user/my-borrows': '我的借阅',
  '/user/profile': '个人信息'
}

const currentPageTitle = computed(() => pageTitles[route.path] || '用户中心')

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.user-layout {
  height: 100vh;
}
.user-layout > .el-container {
  height: 100%;
}
.el-aside {
  background: linear-gradient(180deg, #1E293B 0%, #0F172A 100%);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  box-shadow: 2px 0 12px rgba(0,0,0,0.1);
}
.logo-area {
  padding: 24px 20px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.logo-title { font-size: 17px; font-weight: 700; color: #fff; display: block; line-height: 1.3; }
.logo-sub { font-size: 11px; color: rgba(255,255,255,0.35); display: block; font-weight: 400; letter-spacing: 1px; text-transform: uppercase; }
.user-card {
  margin: 16px;
  padding: 14px;
  background: rgba(255,255,255,0.06);
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-info { display: flex; flex-direction: column; gap: 2px; }
.user-name { color: #fff; font-size: 14px; font-weight: 500; }
.user-role { color: rgba(255,255,255,0.4); font-size: 12px; }
.el-menu { flex: 1; padding: 8px; }
.el-menu-item {
  border-radius: 8px !important;
  margin-bottom: 4px !important;
  height: 44px !important;
  line-height: 44px !important;
  font-size: 14px !important;
  transition: all 0.2s ease;
}
.el-menu-item:hover { background: rgba(255,255,255,0.08) !important; }
.sidebar-footer { padding: 16px; border-top: 1px solid rgba(255,255,255,0.08); }
.logout-btn {
  width: 100%;
  color: rgba(255,255,255,0.5) !important;
  justify-content: center;
  gap: 8px;
  height: 40px;
  border-radius: 8px;
}
.logout-btn:hover { color: #EF4444 !important; background: rgba(239,68,68,0.1) !important; }
.el-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.header-title { font-size: 16px; font-weight: 600; color: var(--text-primary); }
.el-main { background: var(--bg-page); padding: 28px; }
</style>

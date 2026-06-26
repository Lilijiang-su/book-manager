<template>
  <div class="admin-layout">
    <el-container>
      <el-aside width="240px">
        <div class="logo-area">
          <div class="logo-icon">
            <svg viewBox="0 0 40 40" fill="none"><rect width="40" height="40" rx="8" fill="rgba(255,255,255,0.15)"/><path d="M10 14h20v3H10zm0 6h16v3H10zm0 6h18v3H10z" fill="white" opacity="0.9"/></svg>
          </div>
          <div class="logo-text">
            <span class="logo-title">图书管理</span>
            <span class="logo-sub">Book Admin</span>
          </div>
        </div>
        <div class="user-card">
          <el-upload
            :show-file-list="false"
            :http-request="handleUpload"
            :before-upload="beforeUpload"
            class="avatar-uploader"
          >
            <el-avatar :size="40" :src="userStore.userInfo?.avatar ? getAvatarUrl(userStore.userInfo.avatar) : ''" icon="UserFilled" class="avatar-hover" />
            <div class="avatar-overlay"><el-icon><Camera /></el-icon></div>
          </el-upload>
          <div class="user-info">
            <span class="user-name">{{ userStore.userInfo?.name || '管理员' }}</span>
            <el-tag size="small" type="danger" effect="dark">管理员</el-tag>
          </div>
        </div>
        <el-menu
          :default-active="route.path"
          router
          background-color="transparent"
          text-color="rgba(255,255,255,0.6)"
          active-text-color="#fff"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据概览</span>
          </el-menu-item>
          <el-menu-item index="/admin/books">
            <el-icon><Reading /></el-icon>
            <span>图书管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/borrows">
            <el-icon><Document /></el-icon>
            <span>借阅管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/categories">
            <el-icon><Collection /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/audit">
            <el-icon><DocumentChecked /></el-icon>
            <span>审计日志</span>
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
          <div class="header-breadcrumb">
            <el-button text class="search-trigger" @click="globalSearchRef?.open()">
              <el-icon><Search /></el-icon>
              <span>搜索...</span>
              <kbd>Ctrl+K</kbd>
            </el-button>
            <span>{{ currentPageTitle }}</span>
          </div>
          <div class="header-actions">
            <el-popover placement="bottom" :width="360" trigger="click" @show="fetchNotifications">
              <template #reference>
                <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" class="notif-badge">
                  <el-button circle :icon="Bell" />
                </el-badge>
              </template>
              <div class="notif-panel">
                <div class="notif-header">
                  <span class="notif-title">消息通知</span>
                  <el-button text size="small" @click="markAllRead">全部已读</el-button>
                </div>
                <div class="notif-list" v-if="notifications.length > 0">
                  <div v-for="n in notifications" :key="n.id" class="notif-item" :class="{ unread: !n.isRead }" @click="markRead(n.id)">
                    <div class="notif-dot" v-if="!n.isRead"></div>
                    <div class="notif-body">
                      <span class="notif-item-title">{{ n.title }}</span>
                      <span class="notif-content">{{ n.content }}</span>
                      <span class="notif-time">{{ n.createTime }}</span>
                    </div>
                  </div>
                </div>
                <el-empty v-else description="暂无通知" :image-size="60" />
              </div>
            </el-popover>
          </div>
        </el-header>
        <el-main>
          <router-view />
        </el-main>
        <GlobalSearch ref="globalSearchRef" />
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { Bell, SwitchButton, Camera, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getNotifications, getUnreadCount, markNotificationRead, markAllNotificationsRead } from '../../api'
import GlobalSearch from '../../components/GlobalSearch.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const globalSearchRef = ref()
const notifications = ref<any[]>([])
const unreadCount = ref(0)

const pageTitles: Record<string, string> = {
  '/admin/dashboard': '数据概览',
  '/admin/books': '图书管理',
  '/admin/users': '用户管理',
  '/admin/borrows': '借阅管理',
  '/admin/categories': '分类管理',
  '/admin/audit': '审计日志'
}

const currentPageTitle = computed(() => pageTitles[route.path] || '管理后台')

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

// 头像上传
function beforeUpload(file: File) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) { ElMessage.error('只能上传图片文件'); return false }
  if (!isLt2M) { ElMessage.error('图片大小不能超过 2MB'); return false }
  return true
}

async function handleUpload(option: any) {
  const formData = new FormData()
  formData.append('file', option.file)
  try {
    const res = await fetch('/api/user/avatar', {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${userStore.token}` },
      body: formData
    })
    const data = await res.json()
    if (data.code === 200) {
      ElMessage.success('头像更新成功')
      userStore.setUserInfo({ ...userStore.userInfo, avatar: data.data })
      userStore.setUserInfo({ ...userStore.userInfo, avatar: data.data })
    } else {
      ElMessage.error(data.message || '上传失败')
    }
  } catch { ElMessage.error('上传失败') }
}

function getAvatarUrl(path: string) {
  return path.startsWith('http') ? path : `http://localhost:8090${path}`
}

// 通知
async function fetchNotifications() {
  try { const r = await getNotifications(); notifications.value = r.data || [] } catch { /* */ }
}

async function fetchUnreadCount() {
  try { const r = await getUnreadCount(); unreadCount.value = r.data || 0 } catch { /* */ }
}

async function markRead(id: number) {
  await markNotificationRead(id)
  fetchUnreadCount()
  fetchNotifications()
}

async function markAllRead() {
  await markAllNotificationsRead()
  unreadCount.value = 0
  fetchNotifications()
}

onMounted(fetchUnreadCount)
</script>

<style scoped>
.admin-layout { height: 100vh; }
.admin-layout > .el-container { height: 100%; }
.el-aside {
  background: linear-gradient(180deg, #1E293B 0%, #0F172A 100%);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  box-shadow: 2px 0 12px rgba(0,0,0,0.1);
}
.logo-area { padding: 24px 20px 20px; display: flex; align-items: center; gap: 12px; border-bottom: 1px solid rgba(255,255,255,0.08); }
.logo-icon svg { display: block; }
.logo-title { display: block; font-size: 17px; font-weight: 700; color: #fff; line-height: 1.3; }
.logo-sub { display: block; font-size: 11px; color: rgba(255,255,255,0.4); font-weight: 400; letter-spacing: 1px; text-transform: uppercase; }
.user-card { margin: 16px; padding: 14px; background: rgba(255,255,255,0.06); border-radius: 10px; display: flex; align-items: center; gap: 12px; }
.avatar-uploader { position: relative; cursor: pointer; }
.avatar-overlay {
  position: absolute; top: 0; left: 0; width: 40px; height: 40px; border-radius: 50%;
  background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.2s; color: #fff;
}
.user-card:hover .avatar-overlay { opacity: 1; }
.user-info { display: flex; flex-direction: column; gap: 4px; }
.user-name { color: #fff; font-size: 14px; font-weight: 500; }
.el-menu { flex: 1; padding: 8px; }
.el-menu-item { border-radius: 8px !important; margin-bottom: 4px !important; height: 44px !important; line-height: 44px !important; font-size: 14px !important; transition: all 0.2s ease; }
.el-menu-item:hover { background: rgba(255,255,255,0.08) !important; }
.sidebar-footer { padding: 16px; border-top: 1px solid rgba(255,255,255,0.08); }
.logout-btn { width: 100%; color: rgba(255,255,255,0.5) !important; justify-content: center; gap: 8px; height: 40px; border-radius: 8px; transition: all 0.2s ease; }
.logout-btn:hover { color: #EF4444 !important; background: rgba(239,68,68,0.1) !important; }
.el-header { background: #fff; display: flex; align-items: center; justify-content: space-between; padding: 0 28px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); z-index: 10; }
.header-breadcrumb { font-size: 16px; font-weight: 600; color: var(--text-primary); }
.header-actions { display: flex; align-items: center; gap: 8px; }
.el-main { background: var(--bg-page); padding: 28px; }

.notif-panel { max-height: 420px; overflow-y: auto; }
.notif-header { display: flex; align-items: center; justify-content: space-between; padding-bottom: 12px; border-bottom: 1px solid var(--border-color); margin-bottom: 4px; }
.notif-title { font-weight: 600; font-size: 15px; }
.notif-item { display: flex; gap: 10px; padding: 12px 0; border-bottom: 1px solid #f3f4f6; cursor: pointer; }
.notif-item:hover { background: #f9fafb; }
.notif-item.unread { background: #F0F5FF; margin: 0 -12px; padding: 12px; border-radius: 8px; }
.notif-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--primary); flex-shrink: 0; margin-top: 6px; }
.notif-body { display: flex; flex-direction: column; gap: 4px; flex: 1; }
.notif-item-title { font-size: 13px; font-weight: 600; color: var(--text-primary); }
.notif-content { font-size: 12px; color: var(--text-secondary); line-height: 1.4; }
.notif-time { font-size: 11px; color: var(--text-muted); }
</style>

<template>
  <div class="dashboard">
    <div class="page-title">数据概览</div>
    <el-row :gutter="24" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-inner">
            <div class="stat-icon" style="background: linear-gradient(135deg, #5B8DEF 0%, #3D6FD9 100%)">
              <el-icon :size="24" color="#fff"><Reading /></el-icon>
            </div>
            <div class="stat-body">
              <span class="stat-value">{{ stats.bookCount }}</span>
              <span class="stat-label">图书总量</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-inner">
            <div class="stat-icon" style="background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%)">
              <el-icon :size="24" color="#fff"><Document /></el-icon>
            </div>
            <div class="stat-body">
              <span class="stat-value">{{ stats.borrowingCount }}</span>
              <span class="stat-label">借阅中</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-inner">
            <div class="stat-icon" style="background: linear-gradient(135deg, #22C55E 0%, #16A34A 100%)">
              <el-icon :size="24" color="#fff"><User /></el-icon>
            </div>
            <div class="stat-body">
              <span class="stat-value">{{ stats.userCount }}</span>
              <span class="stat-label">用户总数</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-inner">
            <div class="stat-icon" style="background: linear-gradient(135deg, #EF4444 0%, #DC2626 100%)">
              <el-icon :size="24" color="#fff"><WarningFilled /></el-icon>
            </div>
            <div class="stat-body">
              <span class="stat-value">{{ stats.overdueCount }}</span>
              <span class="stat-label">逾期未还</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" style="margin-top: 24px">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-row">
              <span class="card-title">最近借阅记录</span>
              <el-tag size="small" round>实时</el-tag>
            </div>
          </template>
          <el-table :data="recentRecords" stripe v-loading="loading" empty-text="暂无借阅记录">
            <el-table-column prop="userName" label="借阅人" width="120">
              <template #default="{ row }">
                <div class="cell-user">
                  <el-avatar :size="28" icon="UserFilled" />
                  <span>{{ row.userName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="bookName" label="书名" min-width="200" show-overflow-tooltip />
            <el-table-column prop="borrowTime" label="借书时间" width="170" />
            <el-table-column prop="dueTime" label="应还时间" width="170" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'borrowing' ? 'warning' : row.status === 'overdue' ? 'danger' : 'success'" round size="small">
                  {{ row.status === 'borrowing' ? '借阅中' : row.status === 'overdue' ? '逾期' : '已归还' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="quick-actions">
          <template #header>
            <span class="card-title">快捷操作</span>
          </template>
          <div class="action-grid">
            <div class="action-item" @click="$router.push('/admin/books')">
              <el-icon :size="20"><Plus /></el-icon>
              <span>添加图书</span>
            </div>
            <div class="action-item" @click="$router.push('/admin/users')">
              <el-icon :size="20"><UserFilled /></el-icon>
              <span>管理用户</span>
            </div>
            <div class="action-item" @click="$router.push('/admin/borrows')">
              <el-icon :size="20"><Checked /></el-icon>
              <span>处理还书</span>
            </div>
            <div class="action-item" @click="$router.push('/admin/categories')">
              <el-icon :size="20"><FolderOpened /></el-icon>
              <span>分类设置</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getBooks, getBorrowRecords, getUsers } from '../../api'
import { Plus, UserFilled, Checked, FolderOpened, WarningFilled, Reading, Document, User } from '@element-plus/icons-vue'

const stats = ref({ bookCount: 0, borrowingCount: 0, userCount: 0, overdueCount: 0 })
const recentRecords = ref<any[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const [bRes, rRes, uRes] = await Promise.all([getBooks(), getBorrowRecords(), getUsers()])
    const books: any[] = bRes.data || []
    const records: any[] = rRes.data || []
    const users: any[] = uRes.data || []
    stats.value = {
      bookCount: books.length,
      borrowingCount: records.filter(r => r.status === 'borrowing').length,
      userCount: users.length,
      overdueCount: records.filter(r => r.status === 'overdue').length
    }
    recentRecords.value = records.slice(0, 10)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.dashboard { animation: fadeInUp 0.4s ease; }
.stat-card { cursor: default; }
.stat-inner { display: flex; align-items: center; gap: 16px; }
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-body { display: flex; flex-direction: column; }
.stat-value { font-size: 32px; font-weight: 800; color: var(--text-primary); line-height: 1.2; }
.stat-label { font-size: 13px; color: var(--text-secondary); margin-top: 2px; font-weight: 500; }
.card-header-row { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 16px; font-weight: 600; color: var(--text-primary); }
.cell-user { display: flex; align-items: center; gap: 8px; }
.action-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px 12px;
  border-radius: var(--radius-sm);
  background: #F8FAFC;
  cursor: pointer;
  transition: var(--transition);
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
}
.action-item:hover {
  background: var(--primary-light);
  color: var(--primary);
  transform: translateY(-2px);
}
</style>

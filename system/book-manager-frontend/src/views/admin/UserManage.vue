<template>
  <div class="user-manage">
    <div class="page-title">用户管理</div>

    <div class="search-bar">
      <div class="flex-1"></div>
      <el-button type="danger" plain :disabled="selectedIds.length === 0" @click="handleBatchDelete">
        <el-icon><Delete /></el-icon> 批量删除
      </el-button>
      <el-button plain @click="exportData">
        <el-icon><Download /></el-icon> 导出Excel
      </el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="tableData" stripe v-loading="loading" empty-text="暂无用户数据"
        @selection-change="handleSelectionChange" ref="tableRef">
        <el-table-column type="selection" width="50" />
        <el-table-column label="用户" min-width="180">
          <template #default="{ row }">
            <div class="cell-user">
              <el-avatar :size="32" icon="UserFilled" />
              <div>
                <div class="user-name">{{ row.name }}</div>
                <div class="user-username">@{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="200" show-overflow-tooltip />
        <el-table-column label="角色" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'primary'" effect="plain" round size="small">
              {{ row.role === 'admin' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain" round size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-popconfirm title="确定删除该用户？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 16px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchPage(1)"
          @current-change="fetchPage"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getUsersPage, updateUserStatus, deleteUser, batchDeleteUsers } from '../../api'
import { exportToExcel, type ColumnDef } from '../../utils/excel'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Download } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const tableRef = ref()
const selectedIds = ref<number[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

async function fetchPage(page: number) {
  currentPage.value = page
  loading.value = true
  try {
    const res = await getUsersPage({ page, pageSize: pageSize.value })
    tableData.value = (res.data.records || []).filter((u: any) => u.role !== 'admin')
    total.value = res.data.total
  } finally { loading.value = false }
}

function handleSelectionChange(rows: any[]) { selectedIds.value = rows.map(r => r.id) }

async function toggleStatus(row: any) { const ns = row.status === 1 ? 0 : 1; await updateUserStatus(row.id, ns); ElMessage.success(ns === 1 ? '已启用' : '已禁用'); fetchPage(currentPage.value) }
async function handleDelete(id: number) { await deleteUser(id); ElMessage.success('删除成功'); fetchPage(currentPage.value) }

async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm('确定删除选中的用户吗？', '批量删除', { type: 'warning' })
    await batchDeleteUsers(selectedIds.value); ElMessage.success('批量删除成功')
    selectedIds.value = []; fetchPage(currentPage.value)
  } catch { /* cancelled */ }
}

function exportData() {
  const cols: ColumnDef[] = [
    { label: '用户名', key: 'username' }, { label: '姓名', key: 'name' }, { label: '手机号', key: 'phone' },
    { label: '邮箱', key: 'email' }, { label: '角色', key: 'role' }, { label: '状态', key: 'status' }
  ]
  exportToExcel(tableData.value, cols, '用户列表'); ElMessage.success('导出成功')
}

onMounted(() => fetchPage(1))
</script>

<style scoped>
.user-manage { animation: fadeInUp 0.4s ease; }
.cell-user { display: flex; align-items: center; gap: 10px; }
.user-name { font-weight: 500; font-size: 14px; }
.user-username { font-size: 12px; color: var(--text-muted); }
.flex-1 { flex: 1; }
</style>

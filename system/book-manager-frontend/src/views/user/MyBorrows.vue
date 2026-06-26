<template>
  <div class="my-borrows">
    <div class="page-title">我的借阅</div>
    <el-card shadow="never">
      <div class="filter-tabs">
        <el-radio-group v-model="statusFilter">
          <el-radio-button value="all">借阅中</el-radio-button>
          <el-radio-button value="overdue">已逾期</el-radio-button>
          <el-radio-button value="returned">已归还</el-radio-button>
        </el-radio-group>
      </div>
      <el-table :data="filteredData" stripe v-loading="loading" empty-text="暂无借阅记录">
        <el-table-column prop="bookName" label="书名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="borrowTime" label="借书时间" width="175" />
        <el-table-column prop="dueTime" label="应还时间" width="175">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.status === 'borrowing' && isOverdue(row.dueTime) }">{{ row.dueTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="归还时间" width="175">
          <template #default="{ row }">
            <span :class="{ 'text-success': row.status === 'returned' }">{{ row.returnTime || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'returned'" type="success" effect="dark" round size="small">已归还</el-tag>
            <el-tag v-else-if="isOverdue(row.dueTime)" type="danger" effect="dark" round size="small">逾期</el-tag>
            <el-tag v-else type="warning" effect="dark" round size="small">借阅中</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'borrowing'"
              type="success" size="small" plain
              :loading="returningId === row.id"
              @click="handleReturn(row.id)"
            >
              还书
            </el-button>
            <el-tag v-else type="success" size="small" effect="plain" hit>
              <el-icon style="vertical-align: -2px; margin-right: 2px"><CircleCheckFilled /></el-icon>已归还
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getUserBorrowRecords, returnBook } from '../../api'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const returningId = ref<number | null>(null)
const statusFilter = ref('all')

const filteredData = computed(() => {
  if (statusFilter.value === 'all') {
    return tableData.value.filter(r => r.status === 'borrowing' && !isOverdue(r.dueTime))
  }
  if (statusFilter.value === 'overdue') {
    return tableData.value.filter(r => r.status === 'borrowing' && isOverdue(r.dueTime))
  }
  return tableData.value.filter(r => r.status === 'returned')
})

function isOverdue(dueTime: string) { return new Date(dueTime) < new Date() }

async function fetchData() {
  loading.value = true
  try { const res = await getUserBorrowRecords(); tableData.value = res.data || [] } finally { loading.value = false }
}

async function handleReturn(id: number) {
  returningId.value = id
  try { await returnBook(id); ElMessage.success('还书成功'); statusFilter.value = 'returned'; fetchData() } finally { returningId.value = null }
}

onMounted(fetchData)
</script>

<style scoped>
.my-borrows { animation: fadeInUp 0.4s ease; }
.filter-tabs { margin-bottom: 16px; }
.text-danger { color: var(--danger); font-weight: 600; }
.text-success { color: var(--success); font-weight: 500; }
</style>

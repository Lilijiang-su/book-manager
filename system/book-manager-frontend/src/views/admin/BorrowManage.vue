<template>
  <div class="borrow-manage">
    <div class="page-title">借阅管理</div>
    <div class="search-bar">
      <el-radio-group v-model="filterStatus" @change="fetchPage(1)" size="default">
        <el-radio-button value="">全部记录</el-radio-button>
        <el-radio-button value="borrowing">借阅中</el-radio-button>
        <el-radio-button value="overdue">已逾期</el-radio-button>
        <el-radio-button value="returned">已归还</el-radio-button>
      </el-radio-group>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
        start-placeholder="起始时间" end-placeholder="结束时间" value-format="YYYY-MM-DD"
        style="width: 260px" @change="fetchPage(1)" />
    </div>

    <el-card shadow="never">
      <el-table :data="tableData" stripe v-loading="loading" empty-text="暂无借阅记录">
        <el-table-column label="借阅人" width="130">
          <template #default="{ row }">
            <div class="cell-user">
              <el-avatar :size="28" icon="UserFilled" />
              <span>{{ row.userName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="bookName" label="书名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="borrowTime" label="借书时间" width="175" />
        <el-table-column prop="dueTime" label="应还时间" width="175">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.status === 'overdue' }">{{ row.dueTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="归还时间" width="175">
          <template #default="{ row }">
            <span :class="{ 'text-success': row.status === 'returned' }">{{ row.returnTime || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="fineAmount" label="罚款" width="80" align="center">
          <template #default="{ row }"><span class="text-danger" v-if="row.fineAmount > 0">￥{{ row.fineAmount }}</span><span v-else>-</span></template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'borrowing' ? 'warning' : row.status === 'overdue' ? 'danger' : 'success'" effect="plain" round size="small">
              {{ row.status === 'borrowing' ? '借阅中' : row.status === 'overdue' ? '逾期' : '已归还' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 'borrowing' || row.status === 'overdue'" type="success" size="small" plain
              :loading="returningId === row.id" @click="handleReturn(row.id)">确认还书</el-button>
            <span v-else style="color: var(--text-muted); font-size: 13px">已完成</span>
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
import { getBorrowRecordsPage, returnBook } from '../../api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const filterStatus = ref('')
const dateRange = ref<[string, string] | null>(null)
const returningId = ref<number | null>(null)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

async function fetchPage(page: number) {
  currentPage.value = page
  loading.value = true
  try {
    const params: any = { page, pageSize: pageSize.value }
    if (filterStatus.value) params.status = filterStatus.value
    if (dateRange.value) { params.startDate = dateRange.value[0]; params.endDate = dateRange.value[1] }
    const res = await getBorrowRecordsPage(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function handleReturn(id: number) { returningId.value = id; try { await returnBook(id); ElMessage.success('还书成功'); fetchPage(currentPage.value) } finally { returningId.value = null } }

onMounted(() => fetchPage(1))
</script>

<style scoped>
.borrow-manage { animation: fadeInUp 0.4s ease; }
.cell-user { display: flex; align-items: center; gap: 8px; }
.text-danger { color: var(--danger); font-weight: 600; }
.text-success { color: var(--success); font-weight: 500; }
</style>

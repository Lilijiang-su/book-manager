<template>
  <div class="audit-log">
    <div class="page-title">审计日志</div>

    <div class="search-bar">
      <el-select v-model="filters.action" placeholder="操作类型" clearable style="width: 140px" @change="fetchPage(1)">
        <el-option label="创建" value="CREATE" /><el-option label="更新" value="UPDATE" /><el-option label="删除" value="DELETE" />
      </el-select>
      <el-select v-model="filters.targetType" placeholder="目标类型" clearable style="width: 140px" @change="fetchPage(1)">
        <el-option label="图书" value="BOOK" /><el-option label="用户" value="USER" /><el-option label="分类" value="CATEGORY" /><el-option label="借阅" value="BORROW" />
      </el-select>
      <el-input v-model="filters.operatorName" placeholder="操作人" clearable style="width: 180px" @keyup.enter="fetchPage(1)">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-date-picker v-model="filters.dateRange" type="daterange" range-separator="至"
        start-placeholder="起始时间" end-placeholder="结束时间" value-format="YYYY-MM-DD"
        style="width: 260px" @change="fetchPage(1)" />
      <el-button type="primary" @click="fetchPage(1)">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="tableData" stripe v-loading="loading" empty-text="暂无操作记录">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="operatorName" label="操作人" width="120">
          <template #default="{ row }">
            <div class="cell-operator"><el-avatar :size="28" icon="UserFilled" /><span>{{ row.operatorName }}</span></div>
          </template>
        </el-table-column>
        <el-table-column label="操作类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="actionTag(row.action)" effect="plain" round size="small">{{ actionLabel(row.action) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="目标类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag type="info" effect="plain" round size="small">{{ targetLabel(row.targetType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="操作详情" min-width="300" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="createTime" label="操作时间" width="170" />
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
import { ref, reactive, onMounted } from 'vue'
import { getAuditLogsPage } from '../../api'
import { Search } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const filters = reactive({
  action: '', targetType: '', operatorName: '',
  dateRange: null as [string, string] | null
})

function actionTag(a: string) { return a === 'DELETE' ? 'danger' : a === 'CREATE' ? 'success' : 'warning' }
function actionLabel(a: string) { return a === 'CREATE' ? '创建' : a === 'UPDATE' ? '更新' : a === 'DELETE' ? '删除' : a }
function targetLabel(t: string) { return t === 'BOOK' ? '图书' : t === 'USER' ? '用户' : t === 'CATEGORY' ? '分类' : t === 'BORROW' ? '借阅' : t }

async function fetchPage(page: number) {
  currentPage.value = page
  loading.value = true
  try {
    const params: any = { page, pageSize: pageSize.value }
    if (filters.action) params.action = filters.action
    if (filters.targetType) params.targetType = filters.targetType
    if (filters.operatorName) params.operatorName = filters.operatorName
    if (filters.dateRange) { params.startDate = filters.dateRange[0]; params.endDate = filters.dateRange[1] }
    const res = await getAuditLogsPage(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

onMounted(() => fetchPage(1))
</script>

<style scoped>
.audit-log { animation: fadeInUp 0.4s ease; }
.cell-operator { display: flex; align-items: center; gap: 8px; }
</style>

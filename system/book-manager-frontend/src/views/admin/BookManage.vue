<template>
  <div class="book-manage">
    <div class="page-title">图书管理</div>

    <!-- 搜索+批量操作 -->
    <div class="search-bar">
      <el-input v-model="filters.keyword" placeholder="搜索书名 / 作者 / ISBN" clearable class="search-input" @keyup.enter="fetchPage(1)">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="filters.categoryId" placeholder="全部分类" clearable style="width: 140px" @change="fetchPage(1)">
        <el-option v-for="c in flatCategories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select v-model="filters.stockStatus" placeholder="库存状态" clearable style="width: 120px" @change="fetchPage(1)">
        <el-option label="有库存" value="available" />
        <el-option label="低库存" value="low" />
        <el-option label="已借完" value="empty" />
      </el-select>
      <el-date-picker v-model="filters.dateRange" type="daterange" range-separator="至"
        start-placeholder="起始时间" end-placeholder="结束时间" value-format="YYYY-MM-DD"
        style="width: 260px" @change="fetchPage(1)" />
      <el-button type="primary" @click="fetchPage(1)">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <div class="flex-1"></div>
      <el-button v-if="hasFilters" plain @click="resetFilters">
        <el-icon><Back /></el-icon> 返回全部图书
      </el-button>
      <el-button type="danger" plain :disabled="selectedIds.length === 0" @click="handleBatchDelete">
        <el-icon><Delete /></el-icon> 批量删除
      </el-button>
      <el-button type="warning" plain :disabled="selectedIds.length === 0" @click="showBatchCategoryDialog = true">
        <el-icon><Collection /></el-icon> 批量改分类
      </el-button>
      <el-button plain @click="exportData">
        <el-icon><Download /></el-icon> 导出Excel
      </el-button>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> 添加图书
      </el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="tableData" stripe v-loading="loading" empty-text="暂无图书数据"
        @selection-change="handleSelectionChange" ref="tableRef">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="isbn" label="ISBN" width="150" show-overflow-tooltip />
        <el-table-column prop="name" label="书名" min-width="200" show-overflow-tooltip>
          <template #default="{ row }"><span class="book-name">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="140" />
        <el-table-column label="分类" width="110">
          <template #default="{ row }">
            <el-tag size="small" type="info" round>{{ row.categoryName || '未分类' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="70" align="center">
          <template #default="{ row }"><span :class="{ 'text-danger': row.stock <= 1 }">{{ row.stock }}</span></template>
        </el-table-column>
        <el-table-column prop="borrowCount" label="借阅次数" width="90" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain" round size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
              <template #reference><el-button size="small" link type="danger">删除</el-button></template>
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

    <!-- 批量修改分类对话框 -->
    <el-dialog v-model="showBatchCategoryDialog" title="批量修改分类" width="400px">
      <el-form label-width="0">
        <el-form-item>
          <el-tree-select v-model="batchCategoryId" :data="categoryTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择分类" check-strictly style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBatchCategoryDialog = false">取消</el-button>
        <el-button type="primary" @click="handleBatchCategory">确认</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑图书' : '添加图书'" width="680px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="ISBN" prop="isbn">
              <el-input v-model="form.isbn" placeholder="请输入ISBN" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-tree-select v-model="form.categoryId" :data="categoryTree"
                :props="{ label: 'name', value: 'id', children: 'children' }"
                placeholder="请选择分类" check-strictly style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="16">
            <el-form-item label="书名" prop="name">
              <el-input v-model="form.name" placeholder="请输入书名" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="1" :max="1000" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="form.author" placeholder="请输入作者" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版社" prop="publisher">
              <el-input v-model="form.publisher" placeholder="请输入出版社" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="简介" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入图书简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">确认保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getBooksPage, addBook, updateBook, updateBookStatus, deleteBook, batchDeleteBooks, batchUpdateBookCategory, getCategoryTree } from '../../api'
import { exportToExcel, type ColumnDef } from '../../utils/excel'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Delete, Download, Collection, Back } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const tableRef = ref()
const selectedIds = ref<number[]>([])
const flatCategories = ref<any[]>([])
const categoryTree = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saveLoading = ref(false)
const showBatchCategoryDialog = ref(false)
const batchCategoryId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const filters = reactive({
  keyword: '', categoryId: null as number | null, stockStatus: '',
  dateRange: null as [string, string] | null
})

const form = ref<any>({ isbn: '', name: '', author: '', publisher: '', categoryId: null, stock: 1, description: '' })
const rules: FormRules = {
  isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
  name: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

async function fetchPage(page: number) {
  currentPage.value = page
  loading.value = true
  try {
    const params: any = { page, pageSize: pageSize.value }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.categoryId) params.categoryId = filters.categoryId
    if (filters.stockStatus) params.stockStatus = filters.stockStatus
    if (filters.dateRange) { params.startDate = filters.dateRange[0]; params.endDate = filters.dateRange[1] }
    const res = await getBooksPage(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function handleSelectionChange(rows: any[]) { selectedIds.value = rows.map(r => r.id) }

function openDialog(row?: any) {
  form.value = row ? { ...row } : { isbn: '', name: '', author: '', publisher: '', categoryId: null, stock: 1, description: '' }
  isEdit.value = !!row; dialogVisible.value = true
}
async function handleSave() {
  formRef.value?.validate(async v => { if (!v) return; saveLoading.value = true; try { isEdit.value ? await updateBook(form.value) : await addBook(form.value); ElMessage.success(isEdit.value ? '更新成功' : '添加成功'); dialogVisible.value = false; fetchPage(currentPage.value) } finally { saveLoading.value = false } })
}
async function toggleStatus(row: any) { const ns = row.status === 1 ? 0 : 1; await updateBookStatus(row.id, ns); ElMessage.success(ns === 1 ? '已上架' : '已下架'); fetchPage(currentPage.value) }
async function handleDelete(id: number) { await deleteBook(id); ElMessage.success('删除成功'); fetchPage(currentPage.value) }
async function handleBatchDelete() { try { await ElMessageBox.confirm('确定删除？', '批量删除', { type: 'warning' }); await batchDeleteBooks(selectedIds.value); ElMessage.success('批量删除成功'); selectedIds.value = []; fetchPage(currentPage.value) } catch { /* cancelled */ } }
async function handleBatchCategory() { if (!batchCategoryId.value) return; try { await batchUpdateBookCategory(selectedIds.value, batchCategoryId.value); ElMessage.success('批量修改分类成功'); showBatchCategoryDialog.value = false; selectedIds.value = []; fetchPage(currentPage.value) } catch { /* */ } }

const hasFilters = computed(() =>
  !!filters.keyword || !!filters.categoryId || !!filters.stockStatus || !!filters.dateRange
)

function resetFilters() {
  filters.keyword = ''; filters.categoryId = null; filters.stockStatus = ''; filters.dateRange = null
  currentPage.value = 1
  fetchPage(1)
}

function exportData() {
  const cols: ColumnDef[] = [
    { label: 'ISBN', key: 'isbn' }, { label: '书名', key: 'name' }, { label: '作者', key: 'author' },
    { label: '出版社', key: 'publisher' }, { label: '分类', key: 'categoryName' },
    { label: '库存', key: 'stock' }, { label: '借阅次数', key: 'borrowCount' }
  ]
  exportToExcel(tableData.value, cols, '图书列表'); ElMessage.success('导出成功')
}

function buildFlatList(tree: any[]) { for (const c of tree) { flatCategories.value.push(c); if (c.children) buildFlatList(c.children) } }

onMounted(async () => {
  fetchPage(1)
  try { const res = await getCategoryTree(); categoryTree.value = res.data || []; flatCategories.value = []; buildFlatList(categoryTree.value) } catch { /* */ }
})
</script>

<style scoped>
.book-manage { animation: fadeInUp 0.4s ease; }
.search-input { width: 240px; }
.flex-1 { flex: 1; }
.book-name { font-weight: 500; }
.text-danger { color: var(--danger); font-weight: 600; }
.search-bar { flex-wrap: wrap; }
</style>

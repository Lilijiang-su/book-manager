<template>
  <div class="book-list">
    <div class="page-title">图书浏览</div>
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索书名 / 作者 / ISBN" clearable class="search-input" @clear="onSearch" @input="onSearchInput">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="categoryFilter" placeholder="全部分类" clearable @change="filterByCategory" style="width: 160px">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <span class="result-count" v-if="!loading">找到 {{ total }} 本书</span>
    </div>

    <el-row :gutter="20" v-loading="loading" element-loading-background="transparent">
      <el-col :span="6" v-for="book in tableData" :key="book.id" style="margin-bottom: 20px">
        <el-card :body-style="{ padding: '0' }" class="book-card" shadow="hover">
          <div class="book-cover">
            <div class="cover-bg" :style="{ background: coverColors[book.id % coverColors.length] }">
              <el-icon :size="40" color="rgba(255,255,255,0.7)"><Reading /></el-icon>
            </div>
            <div class="cover-badge" v-if="book.stock <= 0">
              <el-tag type="danger" size="small" effect="dark">已借完</el-tag>
            </div>
            <div class="cover-category">
              <el-tag size="small" effect="dark">{{ book.categoryName || '未分类' }}</el-tag>
            </div>
          </div>
          <div class="book-body">
            <h4 class="book-title" :title="book.name">{{ book.name }}</h4>
            <div class="book-meta">
              <span class="meta-author">{{ book.author }}</span>
              <span class="meta-isbn">{{ book.isbn }}</span>
            </div>
            <div class="book-footer">
              <span class="book-stock" :class="{ 'text-danger': book.stock <= 1 }">
                <el-icon><Box /></el-icon>
                {{ book.stock > 0 ? `库存 ${book.stock} 册` : '暂无库存' }}
              </span>
              <span class="book-borrow-count">
                <el-icon><View /></el-icon> {{ book.borrowCount }}
              </span>
            </div>
          </div>
          <div class="book-action">
            <el-button
              type="primary"
              :disabled="book.stock <= 0"
              :loading="borrowingId === book.id"
              @click="handleBorrow(book.id)"
              class="borrow-btn"
              plain
            >
              {{ book.stock <= 0 ? '暂无库存' : '借阅此书' }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="fetchData"
      />
    </div>

    <el-empty v-if="!loading && !tableData.length" description="没有找到匹配的图书" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getBooksPage, getCategories, borrowBook } from '../../api'
import { ElMessage } from 'element-plus'
import { Search, Reading, Box, View } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const keyword = ref('')
const categories = ref<any[]>([])
const categoryFilter = ref<number | null>(null)
const borrowingId = ref<number | null>(null)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

let searchTimer: ReturnType<typeof setTimeout> | null = null

const coverColors = [
  'linear-gradient(135deg, #5B8DEF 0%, #3D6FD9 100%)',
  'linear-gradient(135deg, #22C55E 0%, #16A34A 100%)',
  'linear-gradient(135deg, #F59E0B 0%, #D97706 100%)',
  'linear-gradient(135deg, #EF4444 0%, #DC2626 100%)',
  'linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%)',
  'linear-gradient(135deg, #EC4899 0%, #DB2777 100%)',
  'linear-gradient(135deg, #06B6D4 0%, #0891B2 100%)',
  'linear-gradient(135deg, #84CC16 0%, #65A30D 100%)',
]

function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { currentPage.value = 1; fetchData() }, 300)
}

function onSearch() {
  currentPage.value = 1
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { page: currentPage.value, pageSize: pageSize.value }
    if (keyword.value) params.keyword = keyword.value
    if (categoryFilter.value) params.categoryId = categoryFilter.value
    const res = await getBooksPage(params)
    const pageData = res.data
    tableData.value = (pageData.records || []).filter((b: any) => b.status === 1)
    total.value = pageData.total || 0
  } finally {
    loading.value = false
  }
}

function filterByCategory(val: number | '') {
  categoryFilter.value = val ? (val as number) : null
  currentPage.value = 1
  fetchData()
}

async function handleBorrow(bookId: number) {
  borrowingId.value = bookId
  try {
    await borrowBook(bookId)
    ElMessage.success('借书成功，请在30天内归还')
    fetchData()
  } finally {
    borrowingId.value = null
  }
}

onMounted(async () => {
  fetchData()
  const res = await getCategories()
  categories.value = res.data || []
})
</script>

<style scoped>
.book-list { animation: fadeInUp 0.4s ease; }
.search-input { width: 350px; }
.result-count { font-size: 13px; color: var(--text-muted); margin-left: auto; }
.book-card {
  border-radius: var(--radius) !important;
  overflow: hidden;
  transition: var(--transition);
  height: 100%;
}
.book-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg) !important;
}
.book-cover {
  height: 140px;
  position: relative;
  overflow: hidden;
}
.cover-bg {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cover-badge {
  position: absolute;
  top: 10px;
  right: 10px;
}
.cover-category {
  position: absolute;
  bottom: 10px;
  left: 10px;
}
.book-body { padding: 16px; }
.book-title { font-size: 15px; font-weight: 600; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.book-meta { display: flex; gap: 12px; margin-bottom: 12px; font-size: 13px; color: var(--text-secondary); }
.meta-isbn { color: var(--text-muted); font-family: monospace; font-size: 12px; }
.book-footer { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.book-stock { font-size: 13px; color: var(--text-secondary); display: flex; align-items: center; gap: 4px; }
.book-borrow-count { font-size: 12px; color: var(--text-muted); display: flex; align-items: center; gap: 4px; }
.text-danger { color: var(--danger) !important; font-weight: 600; }
.book-action { padding: 0 16px 16px; }
.borrow-btn { width: 100%; }
.pagination-wrapper { display: flex; justify-content: center; margin-top: 20px; }
</style>

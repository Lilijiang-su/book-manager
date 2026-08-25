<template>
  <div class="global-search-overlay" v-if="visible" @click.self="close">
    <div class="search-dialog">
      <div class="search-input-wrapper">
        <el-icon :size="20" color="var(--text-muted)"><Search /></el-icon>
        <input
          ref="inputRef"
          v-model="query"
          placeholder="搜索菜单、图书、用户... (拼音首字母也行)"
          class="search-input"
          @keydown.esc="close"
          @keydown.enter="selectHighlighted"
          @keydown.up.prevent="moveHighlight(-1)"
          @keydown.down.prevent="moveHighlight(1)"
        />
        <kbd class="esc-hint">ESC</kbd>
      </div>

      <div class="search-results" v-if="query && hasResults">
        <!-- 菜单 -->
        <div class="result-group" v-if="filteredMenus.length">
          <div class="group-label">菜单</div>
          <div
            v-for="(item, idx) in filteredMenus"
            :key="'m'+idx"
            class="result-item"
            :class="{ highlighted: highlightIdx === idx }"
            @click="goTo(item.path)"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
            <span class="result-path">{{ item.path }}</span>
          </div>
        </div>

        <!-- 图书 -->
        <div class="result-group" v-if="filteredBooks.length">
          <div class="group-label">图书 ({{ filteredBooks.length }})</div>
          <div
            v-for="(item, idx) in filteredBooks"
            :key="'b'+idx"
            class="result-item"
            :class="{ highlighted: highlightIdx === filteredMenus.length + idx }"
            @click="goTo('/admin/books')"
          >
            <el-icon><Reading /></el-icon>
            <span>{{ item.name }}</span>
            <span class="result-path">{{ item.author }}</span>
          </div>
        </div>

        <!-- 用户 -->
        <div class="result-group" v-if="filteredUsers.length">
          <div class="group-label">用户 ({{ filteredUsers.length }})</div>
          <div
            v-for="(item, idx) in filteredUsers"
            :key="'u'+idx"
            class="result-item"
            :class="{ highlighted: highlightIdx === filteredMenus.length + filteredBooks.length + idx }"
            @click="goTo('/admin/users')"
          >
            <el-icon><User /></el-icon>
            <span>{{ item.name }}</span>
            <span class="result-path">@{{ item.username }}</span>
          </div>
        </div>
      </div>

      <div class="no-results" v-if="query && !hasResults">
        <el-empty description="未找到结果" :image-size="50" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Reading, User } from '@element-plus/icons-vue'
import { matchPinyinInitial } from '../utils/pinyin'
import { getBooks, getUsers } from '../api'

const router = useRouter()
const visible = ref(false)
const query = ref('')
const highlightIdx = ref(0)
const inputRef = ref<HTMLInputElement | null>(null)
const bookList = ref<any[]>([])
const userList = ref<any[]>([])

// 菜单项
const menuItems = [
  { label: '数据概览', path: '/admin/dashboard', icon: 'DataAnalysis' },
  { label: '图书管理', path: '/admin/books', icon: 'Reading' },
  { label: '用户管理', path: '/admin/users', icon: 'User' },
  { label: '借阅管理', path: '/admin/borrows', icon: 'Document' },
  { label: '分类管理', path: '/admin/categories', icon: 'Collection' },
  { label: '审计日志', path: '/admin/audit', icon: 'DocumentChecked' },
]

const filteredMenus = computed(() =>
  menuItems.filter(m => matchPinyinInitial(query.value, m.label))
)

const filteredBooks = computed(() =>
  query.value
    ? bookList.value.filter(b =>
        matchPinyinInitial(query.value, b.name) ||
        matchPinyinInitial(query.value, b.author) ||
        (b.isbn && b.isbn.includes(query.value))
      ).slice(0, 8)
    : []
)

const filteredUsers = computed(() =>
  query.value
    ? userList.value.filter(u =>
        matchPinyinInitial(query.value, u.name) ||
        matchPinyinInitial(query.value, u.username)
      ).slice(0, 5)
    : []
)

const totalResults = computed(() =>
  filteredMenus.value.length + filteredBooks.value.length + filteredUsers.value.length
)

const hasResults = computed(() => totalResults.value > 0)

function open() {
  visible.value = true
  query.value = ''
  highlightIdx.value = 0
  setTimeout(() => inputRef.value?.focus(), 100)
  // 预加载数据
  if (bookList.value.length === 0) getBooks().then(r => bookList.value = r.data || [])
  if (userList.value.length === 0) getUsers().then(r => userList.value = r.data || [])
}

function close() {
  visible.value = false
  query.value = ''
}

function moveHighlight(delta: number) {
  const max = totalResults.value
  highlightIdx.value = (highlightIdx.value + delta + max) % max
}

function selectHighlighted() {
  let idx = highlightIdx.value
  if (idx < filteredMenus.value.length) {
    goTo(filteredMenus.value[idx].path)
  } else {
    // 图书或用户，跳到对应管理页面
    goTo('/admin/books')
  }
}

function goTo(path: string) {
  close()
  router.push(path)
}

function onKeyDown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    open()
  }
}

onMounted(() => window.addEventListener('keydown', onKeyDown))
onUnmounted(() => window.removeEventListener('keydown', onKeyDown))

defineExpose({ open })
</script>

<style scoped>
.global-search-overlay {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0,0,0,0.45); z-index: 9999;
  display: flex; align-items: flex-start; justify-content: center;
  padding-top: 15vh;
  animation: fadeIn 0.15s ease;
}
.search-dialog {
  width: 560px; max-height: 70vh;
  background: #fff; border-radius: 16px;
  box-shadow: var(--shadow-xl); overflow: hidden;
}
.search-input-wrapper {
  display: flex; align-items: center; gap: 12px;
  padding: 16px 20px; border-bottom: 1px solid var(--border-color);
}
.search-input {
  flex: 1; border: none; outline: none; font-size: 17px;
  background: transparent; color: var(--text-primary);
}
.search-input::placeholder { color: var(--text-muted); }
.esc-hint {
  font-size: 11px; padding: 2px 8px; border-radius: 4px;
  background: #F1F5F9; color: var(--text-muted);
}
.search-results { max-height: 50vh; overflow-y: auto; padding: 8px; }
.result-group { margin-bottom: 8px; }
.group-label {
  font-size: 11px; font-weight: 600; color: var(--text-muted);
  text-transform: uppercase; letter-spacing: 0.5px;
  padding: 8px 12px 4px;
}
.result-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 12px; border-radius: 8px; cursor: pointer;
  transition: background 0.15s; font-size: 14px;
}
.result-item:hover, .result-item.highlighted {
  background: var(--primary-light);
}
.result-path { margin-left: auto; font-size: 12px; color: var(--text-muted); }
.no-results { padding: 24px; }
@keyframes fadeIn { from { opacity:0 } to { opacity:1 } }
</style>

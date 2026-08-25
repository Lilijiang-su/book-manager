<template>
  <div class="category-manage">
    <div class="page-title">分类管理</div>
    <div class="search-bar">
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> 添加分类
      </el-button>
      <el-button type="danger" plain :disabled="selectedIds.length === 0" @click="handleBatchDelete">
        <el-icon><Delete /></el-icon> 批量删除
      </el-button>
      <span class="hint-text">共 {{ flatCategories.length }} 个分类</span>
    </div>

    <el-card shadow="never">
      <el-table :data="categoryTree" stripe v-loading="loading" empty-text="暂无分类"
        row-key="id" :tree-props="{ children: 'children' }"
        @selection-change="handleSelectionChange" ref="tableRef">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="name" label="分类名称" width="240">
          <template #default="{ row }">
            <el-tag :type="row.parentId ? 'info' : 'primary'" effect="plain" size="default" round>{{ row.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="分类描述" min-width="300" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序值" width="90" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain" round size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-popconfirm title="删除后图书将取消分类，确定删除？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '添加分类'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-row :gutter="16">
          <el-col :span="14">
            <el-form-item label="分类名称" prop="name">
              <el-input v-model="form.name" placeholder="分类名称" />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="排序值" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="父级分类">
          <el-tree-select
            v-model="form.parentId"
            :data="categoryTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="留空则为一级分类"
            check-strictly clearable style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="分类描述（可选）" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.statusBool" active-text="启用" inactive-text="禁用" :active-value="true" :inactive-value="false" />
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
import { ref, reactive, onMounted } from 'vue'
import { getCategoryTree, addCategory, updateCategory, updateCategoryStatus, deleteCategory, batchDeleteCategories } from '../../api'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const categoryTree = ref<any[]>([])
const flatCategories = ref<any[]>([])
const tableRef = ref()
const selectedIds = ref<number[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saveLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<any>({ id: null, name: '', description: '', parentId: null, sortOrder: 0, statusBool: true })

const rules: FormRules = { name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] }

function buildFlatList(tree: any[]) {
  for (const c of tree) {
    flatCategories.value.push(c)
    if (c.children) buildFlatList(c.children)
  }
}

function collectSelectedIds(tree: any[]): number[] {
  return tree.flatMap(c => [c.id, ...(c.children ? collectSelectedIds(c.children) : [])])
}

function handleSelectionChange(rows: any[]) {
  selectedIds.value = rows.map(r => r.id)
  // 也收集子节点
  for (const r of rows) {
    if (r.children) {
      selectedIds.value.push(...collectSelectedIds(r.children))
    }
  }
  // 去重
  selectedIds.value = [...new Set(selectedIds.value)]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getCategoryTree()
    categoryTree.value = res.data || []
    flatCategories.value = []
    buildFlatList(categoryTree.value)
  } finally { loading.value = false }
}

function openDialog(row?: any) {
  if (row) {
    form.id = row.id; form.name = row.name; form.description = row.description || ''
    form.parentId = row.parentId; form.sortOrder = row.sortOrder; form.statusBool = row.status === 1
  } else {
    form.id = null; form.name = ''; form.description = ''; form.parentId = null; form.sortOrder = 0; form.statusBool = true
  }
  isEdit.value = !!row
  dialogVisible.value = true
}

async function handleSave() {
  formRef.value?.validate(async valid => {
    if (!valid) return
    saveLoading.value = true
    try {
      const data = { id: form.id, name: form.name, description: form.description, parentId: form.parentId, sortOrder: form.sortOrder, status: form.statusBool ? 1 : 0 }
      isEdit.value ? await updateCategory(data) : await addCategory(data)
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      fetchData()
    } finally { saveLoading.value = false }
  })
}

async function toggleStatus(row: any) { const ns = row.status === 1 ? 0 : 1; await updateCategoryStatus(row.id, ns); ElMessage.success(ns === 1 ? '已启用' : '已禁用'); fetchData() }
async function handleDelete(id: number) { await deleteCategory(id); ElMessage.success('删除成功'); fetchData() }

async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(`确定删除选中的分类吗？`, '批量删除', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
    await batchDeleteCategories(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    fetchData()
  } catch { /* cancelled */ }
}

onMounted(fetchData)
</script>

<style scoped>
.category-manage { animation: fadeInUp 0.4s ease; }
.hint-text { font-size: 13px; color: var(--text-muted); margin-left: auto; }
</style>

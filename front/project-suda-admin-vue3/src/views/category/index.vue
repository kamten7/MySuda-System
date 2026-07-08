<template>
  <div class="category-page">
    <!-- Search Bar -->
    <div class="bg-white rounded-lg p-4 mb-4 shadow-sm">
      <el-form :model="searchForm" :inline="true" size="default">
        <el-form-item label="分类名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入分类名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="分类类型">
          <el-select
            v-model="searchForm.type"
            placeholder="请选择类型"
            clearable
            style="width: 160px"
          >
            <el-option label="菜品分类" :value="1" />
            <el-option label="套餐分类" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            <span>查询</span>
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            <span>重置</span>
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Action Bar -->
    <div class="bg-white rounded-lg p-4 mb-4 shadow-sm">
      <div class="flex gap-2">
        <el-button type="primary" @click="handleAdd(1)">
          <el-icon><Plus /></el-icon>
          <span>新增菜品分类</span>
        </el-button>
        <el-button type="warning" @click="handleAdd(2)">
          <el-icon><Plus /></el-icon>
          <span>新增套餐分类</span>
        </el-button>
      </div>
    </div>

    <!-- Table -->
    <div class="bg-white rounded-lg p-4 shadow-sm">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#303133', fontWeight: 600 }"
      >
        <el-table-column label="分类名称" prop="name" min-width="150" />
        <el-table-column label="分类类型" prop="type" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? undefined : 'warning'" size="small">
              {{ row.type === 1 ? '菜品分类' : '套餐分类' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="sort" min-width="80" align="center" />
        <el-table-column label="状态" min-width="100" align="center">
          <template #default="scope: any">
            <el-switch
              :model-value="scope.row.status === 1"
              active-text="启用"
              inactive-text="禁用"
              @change="(val: any) => handleToggleStatus(scope.row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作时间" prop="updateTime" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="160" fixed="right">
          <template #default="scope: any">
            <div class="flex gap-1">
              <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button
                v-if="canDelete"
                type="danger"
                size="small"
                @click="handleDelete(scope.row)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="flex justify-end mt-4">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.isEdit ? '编辑分类' : '新增分类'"
      width="480px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="dialog.form"
        :rules="dialog.rules"
        label-width="80px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model="dialog.form.name"
            placeholder="请输入分类名称（中文或英文）"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="dialog.form.sort"
            :min="0"
            :max="99"
            placeholder="排序号"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { formatDateTime } from '@/utils/date'
import {
  getCategoryPage,
  addCategory,
  editCategory,
  deleteCategory,
  toggleCategoryStatus,
} from '@/api/modules/category'
import type { CategoryRecord, CategoryQuery, CategoryForm } from '@/api/modules/category'

// Delete permission gate
const canDelete = computed(() => import.meta.env.VITE_DELETE_PERMISSIONS === 'true')

// Search form
const searchForm = reactive({
  name: '',
  type: null as number | null,
})

// Pagination
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// Table
const loading = ref(false)
const tableData = ref<CategoryRecord[]>([])

// Dialog
const formRef = ref<FormInstance>()
const dialog = reactive({
  visible: false,
  isEdit: false,
  submitting: false,
  form: {
    name: '',
    type: 1 as number,
    sort: 0,
  } as CategoryForm & { id?: number },
  rules: {
    name: [
      { required: true, message: '请输入分类名称', trigger: 'blur' },
      { pattern: /^[一-龥a-zA-Z]+$/, message: '名称仅支持中文或英文', trigger: 'blur' },
    ],
    sort: [{ required: true, message: '请输入排序号', trigger: 'blur' }],
  } as FormRules,
})

// Fetch data
async function fetchData() {
  loading.value = true
  try {
    const params: CategoryQuery = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (searchForm.name) params.name = searchForm.name
    if (searchForm.type !== null) params.type = searchForm.type

    const res = await getCategoryPage(params)
    if (res.data.code === 1 && res.data.data) {
      tableData.value = res.data.data.records || []
      pagination.total = res.data.data.total || 0
    }
  } catch {
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// Search
function handleSearch() {
  pagination.page = 1
  fetchData()
}

// Reset
function handleReset() {
  searchForm.name = ''
  searchForm.type = null
  pagination.page = 1
  fetchData()
}

// Pagination
function handlePageChange(page: number) {
  pagination.page = page
  fetchData()
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchData()
}

// Add
function handleAdd(type: number) {
  dialog.visible = true
  dialog.isEdit = false
  dialog.form = { name: '', type, sort: 0 }
}

// Edit
function handleEdit(row: CategoryRecord) {
  dialog.visible = true
  dialog.isEdit = true
  dialog.form = { id: row.id, name: row.name, type: row.type, sort: row.sort }
}

// Delete
async function handleDelete(row: CategoryRecord) {
  try {
    await ElMessageBox.confirm(`确认删除分类「${row.name}」?`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    const res = await deleteCategory(row.id)
    if (res.data.code === 1) {
      ElMessage.success('删除成功')
      fetchData()
    }
  } catch {
    // User cancelled or error
  }
}

// Toggle status
async function handleToggleStatus(row: CategoryRecord, value: boolean) {
  const newStatus = value ? 1 : 0
  try {
    const res = await toggleCategoryStatus(newStatus, row.id)
    if (res.data.code === 1) {
      ElMessage.success(value ? '已启用' : '已禁用')
      fetchData()
    }
  } catch {
    // Revert on error – fetch fresh data
    fetchData()
  }
}

// Submit form
async function submitForm() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  dialog.submitting = true
  try {
    const api = dialog.isEdit ? editCategory : addCategory
    const res = await api(dialog.form)
    if (res.data.code === 1) {
      ElMessage.success(dialog.isEdit ? '编辑成功' : '新增成功')
      dialog.visible = false
      fetchData()
    }
  } catch {
    // Error handled by interceptor
  } finally {
    dialog.submitting = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

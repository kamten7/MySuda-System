<template>
  <div class="dish-page">
    <!-- Search Bar -->
    <div class="bg-white rounded-lg p-4 mb-4 shadow-sm">
      <el-form :model="searchForm" :inline="true" size="default">
        <el-form-item label="菜品名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入菜品名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="菜品分类">
          <el-select
            v-model="searchForm.categoryId"
            placeholder="请选择分类"
            clearable
            style="width: 180px"
          >
            <el-option
              v-for="cat in categoryOptions"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 130px"
          >
            <el-option label="起售" :value="1" />
            <el-option label="停售" :value="0" />
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
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          <span>新增菜品</span>
        </el-button>
        <el-button
          v-if="canDelete"
          type="danger"
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          <span>批量删除</span>
        </el-button>
      </div>
    </div>

    <!-- Table -->
    <div class="bg-white rounded-lg p-4 shadow-sm">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#303133', fontWeight: 600 }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column label="图片" prop="image" width="90">
          <template #default="{ row }">
            <el-image
              v-if="row.image"
              :src="row.image"
              :preview-src-list="[row.image]"
              fit="cover"
              class="dish-thumb"
            />
            <span v-else class="text-gray-400 text-xs">暂无</span>
          </template>
        </el-table-column>
        <el-table-column label="菜品名称" prop="name" min-width="140" />
        <el-table-column label="分类" prop="categoryName" min-width="100" />
        <el-table-column label="价格" prop="price" min-width="100">
          <template #default="{ row }">
            <span class="text-red-500 font-medium">&yen;{{ (row.price || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100" align="center">
          <template #default="scope: any">
            <el-switch
              :model-value="scope.row.status === 1"
              active-text="起售"
              inactive-text="停售"
              @change="(val: any) => handleToggleStatus(scope.row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="更新时间" prop="updateTime" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" fixed="right">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/date'
import {
  getDishPage,
  deleteDish,
  toggleDishStatus,
} from '@/api/modules/dish'
import { getCategoryList } from '@/api/modules/category'
import type { DishRecord, DishQuery } from '@/api/modules/dish'
import type { CategoryRecord } from '@/api/modules/category'

const router = useRouter()

// Delete permission gate
const canDelete = computed(() => import.meta.env.VITE_DELETE_PERMISSIONS === 'true')

// Category options for search
const categoryOptions = ref<CategoryRecord[]>([])

// Search form
const searchForm = reactive({
  name: '',
  categoryId: undefined as number | undefined,
  status: undefined as number | undefined,
})

// Pagination
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// Table
const loading = ref(false)
const tableData = ref<DishRecord[]>([])
const tableRef = ref()
const selectedIds = ref<number[]>([])

// Fetch categories
async function fetchCategories() {
  try {
    const res = await getCategoryList(1)
    if (res.data.code === 1 && res.data.data) {
      categoryOptions.value = res.data.data
    }
  } catch {
    // Non-blocking
  }
}

// Fetch data
async function fetchData() {
  loading.value = true
  try {
    const params: DishQuery = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (searchForm.name) params.name = searchForm.name
    if (searchForm.categoryId !== undefined) params.categoryId = searchForm.categoryId
    if (searchForm.status !== undefined) params.status = searchForm.status

    const res = await getDishPage(params)
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
  searchForm.categoryId = undefined
  searchForm.status = undefined
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

// Selection
function handleSelectionChange(rows: DishRecord[]) {
  selectedIds.value = rows.map((row) => row.id)
}

// Add
function handleAdd() {
  router.push('/dish/edit')
}

// Edit
function handleEdit(row: DishRecord) {
  router.push(`/dish/edit/${row.id}`)
}

// Delete single
async function handleDelete(row: DishRecord) {
  try {
    await ElMessageBox.confirm(`确认删除菜品「${row.name}」?`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    const res = await deleteDish([row.id])
    if (res.data.code === 1) {
      ElMessage.success('删除成功')
      fetchData()
    }
  } catch {
    // User cancelled or error
  }
}

// Batch delete
async function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的菜品')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个菜品?`, '批量删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    const res = await deleteDish(selectedIds.value)
    if (res.data.code === 1) {
      ElMessage.success('批量删除成功')
      selectedIds.value = []
      fetchData()
    }
  } catch {
    // User cancelled or error
  }
}

// Toggle status
async function handleToggleStatus(row: DishRecord, value: boolean) {
  const newStatus = value ? 1 : 0
  try {
    const res = await toggleDishStatus(newStatus, row.id)
    if (res.data.code === 1) {
      ElMessage.success(value ? '已起售' : '已停售')
      fetchData()
    }
  } catch {
    fetchData()
  }
}

onMounted(() => {
  fetchCategories()
  fetchData()
})
</script>

<style lang="scss" scoped>
.dish-thumb {
  width: 56px;
  height: 56px;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}
</style>

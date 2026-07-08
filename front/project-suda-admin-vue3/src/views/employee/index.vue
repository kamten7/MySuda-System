<template>
  <div class="employee-page">
    <!-- Search Bar -->
    <div class="bg-white rounded-lg p-4 mb-4 shadow-sm">
      <el-form :model="searchForm" :inline="true" size="default">
        <el-form-item label="员工姓名">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入员工姓名"
            clearable
            style="width: 200px"
          />
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
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        <span>新增员工</span>
      </el-button>
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
        <el-table-column label="员工姓名" prop="name" min-width="120" />
        <el-table-column label="账号" prop="username" min-width="120" />
        <el-table-column label="手机号" prop="phone" min-width="130" />
        <el-table-column label="性别" prop="sex" min-width="80" align="center" />
        <el-table-column label="状态" min-width="100" align="center">
          <template #default="scope: any">
            <el-switch
              :model-value="scope.row.status === 1"
              :disabled="scope.row.username === 'admin'"
              active-text="启用"
              inactive-text="禁用"
              @change="(val: any) => handleToggleStatus(scope.row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="更新时间" prop="updateTime" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="160" fixed="right">
          <template #default="scope: any">
            <div class="flex gap-1">
              <el-button
                type="primary"
                size="small"
                :disabled="scope.row.username === 'admin'"
                @click="handleEdit(scope.row)"
              >
                编辑
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
      :title="dialog.isEdit ? '编辑员工' : '新增员工'"
      width="520px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="dialog.form"
        :rules="dialog.rules"
        label-width="100px"
      >
        <el-form-item label="账号" prop="username">
          <el-input
            v-model="dialog.form.username"
            placeholder="请输入账号"
            :disabled="dialog.isEdit"
            maxlength="20"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!dialog.isEdit">
          <el-input
            v-model="dialog.form.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="员工姓名" prop="name">
          <el-input v-model="dialog.form.name" placeholder="请输入员工姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="dialog.form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="dialog.form.sex">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身份证号" prop="idNumber">
          <el-input v-model="dialog.form.idNumber" placeholder="请输入身份证号" maxlength="18" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { formatDateTime } from '@/utils/date'
import {
  getEmployeePage,
  addEmployee,
  editEmployee,
  queryEmployeeById,
  toggleEmployeeStatus,
} from '@/api/modules/employee'
import type { EmployeeRecord, EmployeeQuery, EmployeeForm } from '@/api/modules/employee'

// Search form
const searchForm = reactive({
  name: '',
})

// Pagination
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// Table
const loading = ref(false)
const tableData = ref<EmployeeRecord[]>([])

// Dialog
const formRef = ref<FormInstance>()
const dialog = reactive({
  visible: false,
  isEdit: false,
  submitting: false,
  form: {
    username: '',
    password: '',
    name: '',
    phone: '',
    sex: '男',
    idNumber: '',
  } as EmployeeForm & { password?: string; id?: number },
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' },
      { min: 3, max: 20, message: '账号长度3-20个字符', trigger: 'blur' },
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, message: '密码至少6个字符', trigger: 'blur' },
    ],
    name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
    ],
    sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
    idNumber: [
      { required: true, message: '请输入身份证号', trigger: 'blur' },
      {
        pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/,
        message: '身份证号格式不正确',
        trigger: 'blur',
      },
    ],
  } as FormRules,
})

// Fetch data
async function fetchData() {
  loading.value = true
  try {
    const params: EmployeeQuery = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (searchForm.name) params.name = searchForm.name

    const res = await getEmployeePage(params)
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
function handleAdd() {
  dialog.visible = true
  dialog.isEdit = false
  dialog.form = {
    username: '',
    password: '',
    name: '',
    phone: '',
    sex: '男',
    idNumber: '',
  }
}

// Edit
async function handleEdit(row: EmployeeRecord) {
  dialog.visible = true
  dialog.isEdit = true
  dialog.form = {
    id: row.id,
    username: row.username,
    name: row.name,
    phone: row.phone,
    sex: row.sex,
    idNumber: row.idNumber,
  }

  // Fetch full details if needed
  try {
    const res = await queryEmployeeById(row.id)
    if (res.data.code === 1 && res.data.data) {
      const emp = res.data.data
      dialog.form = {
        id: emp.id,
        username: emp.username,
        name: emp.name,
        phone: emp.phone,
        sex: emp.sex,
        idNumber: emp.idNumber,
      }
    }
  } catch {
    // Use basic row data as fallback
  }
}

// Toggle status
async function handleToggleStatus(row: EmployeeRecord, value: boolean) {
  const newStatus = value ? 1 : 0
  try {
    const res = await toggleEmployeeStatus(newStatus, row.id)
    if (res.data.code === 1) {
      ElMessage.success(value ? '已启用' : '已禁用')
      fetchData()
    }
  } catch {
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
    const api = dialog.isEdit ? editEmployee : addEmployee
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

<template>
  <div class="employee-edit-page">
    <!-- Page Header -->
    <div class="flex items-center gap-3 mb-4">
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </el-button>
      <h2 class="text-lg font-semibold text-gray-800">
        {{ isEdit ? '编辑员工' : '新增员工' }}
      </h2>
    </div>

    <!-- Form Card -->
    <div class="bg-white rounded-lg p-6 shadow-sm">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="default"
      >
        <el-form-item label="账号" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入账号"
            :disabled="isEdit"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>

        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="员工姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入员工姓名" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            maxlength="11"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="form.sex">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="身份证号" prop="idNumber">
          <el-input
            v-model="form.idNumber"
            placeholder="请输入身份证号"
            maxlength="18"
            show-word-limit
          />
        </el-form-item>

        <!-- Submit -->
        <el-form-item>
          <div class="flex gap-4">
            <el-button @click="handleBack">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="submitForm">
              {{ isEdit ? '保存修改' : '确认新增' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  addEmployee,
  editEmployee,
  queryEmployeeById,
} from '@/api/modules/employee'
import type { EmployeeForm } from '@/api/modules/employee'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)

// Form
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive<EmployeeForm & { password?: string; id?: number }>({
  username: '',
  password: '',
  name: '',
  phone: '',
  sex: '男',
  idNumber: '',
})

const rules = computed<FormRules>(() => {
  const baseRules: FormRules = {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' },
      { min: 3, max: 20, message: '账号长度3-20个字符', trigger: 'blur' },
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
  }

  // Only require password for new employees
  if (!isEdit.value) {
    baseRules.password = [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, message: '密码至少6个字符', trigger: 'blur' },
    ]
  }

  return baseRules
})

// Fetch employee detail for edit mode
async function fetchEmployeeDetail() {
  if (!isEdit.value) return
  try {
    const id = route.params.id as string
    const res = await queryEmployeeById(id)
    if (res.data.code === 1 && res.data.data) {
      const emp = res.data.data
      form.id = emp.id
      form.username = emp.username
      form.name = emp.name
      form.phone = emp.phone
      form.sex = emp.sex
      form.idNumber = emp.idNumber
    }
  } catch {
    ElMessage.error('获取员工信息失败')
  }
}

// Submit
async function submitForm() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const api = isEdit.value ? editEmployee : addEmployee
    const res = await api(form)
    if (res.data.code === 1) {
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      router.back()
    }
  } catch {
    // Error handled by interceptor
  } finally {
    submitting.value = false
  }
}

// Back
function handleBack() {
  router.back()
}

onMounted(() => {
  fetchEmployeeDetail()
})
</script>

<style lang="scss" scoped>
.employee-edit-page {
  max-width: 600px;
}
</style>

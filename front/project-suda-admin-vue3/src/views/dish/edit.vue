<template>
  <div class="dish-edit-page">
    <!-- Page Header -->
    <div class="flex items-center gap-3 mb-4">
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </el-button>
      <h2 class="text-lg font-semibold text-gray-800">
        {{ isEdit ? '编辑菜品' : '新增菜品' }}
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
        <el-form-item label="菜品名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入菜品名称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="菜品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 240px">
            <el-option
              v-for="cat in categoryOptions"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="form.price"
            :min="0"
            :precision="2"
            :step="0.5"
            placeholder="请输入价格"
          />
          <span class="ml-2 text-gray-400 text-sm">元</span>
        </el-form-item>

        <el-form-item label="菜品图片" prop="image">
          <ImageUpload v-model="form.image" />
        </el-form-item>

        <el-form-item label="菜品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入菜品描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- Flavors Section -->
        <el-divider content-position="left">口味管理</el-divider>

        <div class="flavors-section">
          <div
            v-for="(flavor, fIndex) in form.flavors"
            :key="fIndex"
            class="flavor-item bg-gray-50 rounded-lg p-4 mb-3 border border-gray-200"
          >
            <div class="flex items-start gap-4">
              <div class="flex-1">
                <div class="flex items-center gap-3 mb-3">
                  <span class="text-sm text-gray-600 w-12">口味名:</span>
                  <el-input
                    v-model="flavor.name"
                    placeholder="例如: 辣度、温度、甜度"
                    style="width: 200px"
                  />
                </div>
                <div class="flex items-center gap-3">
                  <span class="text-sm text-gray-600 w-12">口味值:</span>
                  <div class="flex flex-wrap items-center gap-2">
                    <el-tag
                      v-for="(val, vIndex) in getFlavorValues(flavor)"
                      :key="vIndex"
                      closable
                      size="default"
                      @close="removeFlavorValue(fIndex, vIndex)"
                    >
                      {{ val }}
                    </el-tag>
                    <el-input
                      v-if="flavorInputVisible[fIndex]"
                      ref="flavorInputRef"
                      v-model="flavorInputValue[fIndex]"
                      size="small"
                      style="width: 100px"
                      @keyup.enter="addFlavorValue(fIndex)"
                      @blur="addFlavorValue(fIndex)"
                    />
                    <el-button
                      v-else
                      size="small"
                      @click="showFlavorInput(fIndex)"
                    >
                      + 新增口味值
                    </el-button>
                  </div>
                </div>
              </div>
              <el-button type="danger" size="small" plain @click="removeFlavor(fIndex)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>

          <el-button type="primary" plain @click="addFlavor">
            <el-icon><Plus /></el-icon>
            <span>添加口味</span>
          </el-button>
        </div>

        <!-- Submit -->
        <div class="mt-8 flex justify-center gap-4">
          <el-button @click="handleBack">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">
            {{ isEdit ? '保存修改' : '确认新增' }}
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import {
  addDish,
  editDish,
  queryDishById,
} from '@/api/modules/dish'
import { getCategoryList } from '@/api/modules/category'
import type { DishForm, DishFlavor } from '@/api/modules/dish'
import type { CategoryRecord } from '@/api/modules/category'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)

// Category options
const categoryOptions = ref<CategoryRecord[]>([])

// Form
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive<DishForm>({
  name: '',
  categoryId: 0,
  price: 0,
  image: '',
  description: '',
  flavors: [],
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择菜品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  image: [{ required: true, message: '请上传菜品图片', trigger: 'change' }],
}

// Flavor management - using reactive objects for dynamic keys
const flavorInputVisible = reactive<Record<number, boolean>>({})
const flavorInputValue = reactive<Record<number, string>>({})

function getFlavorValues(flavor: DishFlavor): string[] {
  if (!flavor.value) return []
  return flavor.value.split(',').filter(Boolean)
}

function showFlavorInput(index: number) {
  flavorInputVisible[index] = true
  flavorInputValue[index] = ''
}

function addFlavorValue(fIndex: number) {
  const val = (flavorInputValue[fIndex] || '').trim()
  if (!val) {
    flavorInputVisible[fIndex] = false
    return
  }
  const flavor = form.flavors[fIndex]
  const currentValues = getFlavorValues(flavor)
  if (currentValues.includes(val)) {
    ElMessage.warning('口味值已存在')
    flavorInputValue[fIndex] = ''
    return
  }
  currentValues.push(val)
  flavor.value = currentValues.join(',')
  flavorInputValue[fIndex] = ''
  flavorInputVisible[fIndex] = false
}

function removeFlavorValue(fIndex: number, vIndex: number) {
  const flavor = form.flavors[fIndex]
  const values = getFlavorValues(flavor)
  values.splice(vIndex, 1)
  flavor.value = values.join(',')
}

function addFlavor() {
  form.flavors.push({ name: '', value: '' })
}

function removeFlavor(index: number) {
  form.flavors.splice(index, 1)
}

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

// Fetch dish detail for edit mode
async function fetchDishDetail() {
  if (!isEdit.value) return
  try {
    const id = route.params.id as string
    const res = await queryDishById(id)
    if (res.data.code === 1 && res.data.data) {
      const dish = res.data.data
      form.name = dish.name
      form.categoryId = dish.categoryId
      form.price = dish.price
      form.image = dish.image
      form.description = dish.description
      form.flavors = dish.flavors?.length ? [...dish.flavors] : []
      // Store id for edit
      ;(form as any).id = dish.id
    }
  } catch {
    ElMessage.error('获取菜品信息失败')
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
    const api = isEdit.value ? editDish : addDish
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
  fetchCategories()
  fetchDishDetail()
})
</script>

<style lang="scss" scoped>
.dish-edit-page {
  max-width: 800px;
}

.flavors-section {
  margin-left: 100px;
}

.flavor-item {
  transition: box-shadow 0.3s;
}

.flavor-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
</style>

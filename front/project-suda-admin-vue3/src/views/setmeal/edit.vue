<template>
  <div class="setmeal-edit-page">
    <!-- Page Header -->
    <div class="flex items-center gap-3 mb-4">
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </el-button>
      <h2 class="text-lg font-semibold text-gray-800">
        {{ isEdit ? '编辑套餐' : '新增套餐' }}
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
        <el-form-item label="套餐名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入套餐名称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="套餐分类" prop="categoryId">
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

        <el-form-item label="图片" prop="image">
          <ImageUpload v-model="form.image" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入套餐描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- Dish Selection Section -->
        <el-divider content-position="left">菜品选择</el-divider>

        <div class="dishes-section ml-[100px]">
          <!-- Selected dishes list -->
          <div v-if="form.setmealDishes.length > 0" class="mb-4">
            <div class="text-sm text-gray-600 mb-2">已选菜品 ({{ form.setmealDishes.length }}):</div>
            <div class="space-y-2">
              <div
                v-for="(sd, index) in form.setmealDishes"
                :key="index"
                class="flex items-center justify-between bg-gray-50 rounded-lg px-4 py-2 border border-gray-200"
              >
                <div class="flex items-center gap-6">
                  <span class="text-sm font-medium text-gray-700 min-w-[120px]">{{ sd.name }}</span>
                  <span class="text-sm text-red-500">&yen;{{ (sd.price || 0).toFixed(2) }}</span>
                  <div class="flex items-center gap-2">
                    <span class="text-sm text-gray-500">份数:</span>
                    <el-input-number
                      v-model="sd.copies"
                      :min="1"
                      :max="99"
                      size="small"
                      style="width: 100px"
                    />
                  </div>
                </div>
                <el-button type="danger" size="small" plain @click="removeDish(index)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
          <div v-else class="text-gray-400 text-sm mb-4 py-4 bg-gray-50 rounded-lg text-center border border-dashed border-gray-300">
            暂未选择菜品，请点击下方按钮选择
          </div>

          <el-button type="primary" plain @click="dishSelectVisible = true">
            <el-icon><Plus /></el-icon>
            <span>选择菜品</span>
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

    <!-- Dish Select Dialog -->
    <el-dialog
      v-model="dishSelectVisible"
      title="选择菜品"
      width="700px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div v-loading="dishLoading">
        <!-- Category filter tabs -->
        <div class="flex flex-wrap gap-2 mb-4">
          <div
            class="dish-cat-tab"
            :class="{ active: dishCatFilter === 0 }"
            @click="dishCatFilter = 0"
          >
            全部
          </div>
          <div
            v-for="cat in categoryOptions"
            :key="cat.id"
            class="dish-cat-tab"
            :class="{ active: dishCatFilter === cat.id }"
            @click="dishCatFilter = cat.id"
          >
            {{ cat.name }}
          </div>
        </div>

        <!-- Dish list -->
        <div class="dish-grid">
          <div
            v-for="dish in filteredDishList"
            :key="dish.id"
            class="dish-card border rounded-lg p-3 cursor-pointer transition-all"
            :class="{ selected: isDishSelected(dish.id) }"
            @click="toggleDish(dish)"
          >
            <div class="flex items-start gap-3">
              <el-checkbox
                :model-value="isDishSelected(dish.id)"
                @click.stop
                @change="toggleDish(dish)"
              />
              <div class="flex-1 min-w-0">
                <div class="text-sm font-medium text-gray-700 truncate">{{ dish.name }}</div>
                <div class="text-sm text-red-500 mt-1">&yen;{{ (dish.price || 0).toFixed(2) }}</div>
              </div>
              <div class="flex items-center gap-1" v-if="isDishSelected(dish.id)">
                <span class="text-xs text-gray-500">份数</span>
                <el-input-number
                  :model-value="getDishCopies(dish.id)"
                  :min="1"
                  :max="99"
                  size="small"
                  style="width: 80px"
                  @click.stop
                  @update:model-value="(val: number | undefined) => updateDishCopies(dish.id, val ?? 1)"
                />
              </div>
            </div>
          </div>
        </div>

        <div v-if="filteredDishList.length === 0 && !dishLoading" class="text-center text-gray-400 py-8">
          暂无菜品数据
        </div>
      </div>
      <template #footer>
        <el-button @click="dishSelectVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDishSelect">
          确定 (已选 {{ tempSelectedDishes.length }} 个)
        </el-button>
      </template>
    </el-dialog>
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
  addSetmeal,
  editSetmeal,
  querySetmealById,
} from '@/api/modules/setmeal'
import { getDishList } from '@/api/modules/dish'
import { getCategoryList } from '@/api/modules/category'
import type { SetmealForm, SetmealDish } from '@/api/modules/setmeal'
import type { DishRecord } from '@/api/modules/dish'
import type { CategoryRecord } from '@/api/modules/category'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)

// Category options (type=2 for setmeal)
const categoryOptions = ref<CategoryRecord[]>([])

// Form
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive<SetmealForm>({
  name: '',
  categoryId: 0,
  price: 0,
  image: '',
  description: '',
  setmealDishes: [],
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择套餐分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  image: [{ required: true, message: '请上传图片', trigger: 'change' }],
}

// Dish selection dialog
const dishSelectVisible = ref(false)
const dishLoading = ref(false)
const dishList = ref<DishRecord[]>([])
const dishCatFilter = ref(0)
const tempSelectedDishes = ref<SetmealDish[]>([])

const filteredDishList = computed(() => {
  if (dishCatFilter.value === 0) return dishList.value
  return dishList.value.filter((d) => d.categoryId === dishCatFilter.value)
})

function isDishSelected(dishId: number): boolean {
  return tempSelectedDishes.value.some((d) => d.dishId === dishId)
}

function getDishCopies(dishId: number): number {
  const found = tempSelectedDishes.value.find((d) => d.dishId === dishId)
  return found?.copies ?? 1
}

function updateDishCopies(dishId: number, copies: number) {
  const found = tempSelectedDishes.value.find((d) => d.dishId === dishId)
  if (found) {
    found.copies = copies
  }
}

function toggleDish(dish: DishRecord) {
  const idx = tempSelectedDishes.value.findIndex((d) => d.dishId === dish.id)
  if (idx >= 0) {
    tempSelectedDishes.value.splice(idx, 1)
  } else {
    tempSelectedDishes.value.push({
      dishId: dish.id,
      name: dish.name,
      price: dish.price,
      copies: 1,
    })
  }
}

function removeDish(index: number) {
  form.setmealDishes.splice(index, 1)
}

function confirmDishSelect() {
  form.setmealDishes = [...tempSelectedDishes.value]
  dishSelectVisible.value = false
}

// Fetch categories
async function fetchCategories() {
  try {
    const res = await getCategoryList(2)
    if (res.data.code === 1 && res.data.data) {
      categoryOptions.value = res.data.data
    }
  } catch {
    // Non-blocking
  }
}

// Fetch all dishes for selection
async function fetchDishes() {
  dishLoading.value = true
  try {
    const res = await getDishList()
    if (res.data.code === 1 && res.data.data) {
      dishList.value = res.data.data
    }
  } catch {
    dishList.value = []
  } finally {
    dishLoading.value = false
  }
}

// Fetch setmeal detail for edit mode
async function fetchSetmealDetail() {
  if (!isEdit.value) return
  try {
    const id = route.params.id as string
    const res = await querySetmealById(id)
    if (res.data.code === 1 && res.data.data) {
      const setmeal = res.data.data
      form.name = setmeal.name
      form.categoryId = setmeal.categoryId
      form.price = setmeal.price
      form.image = setmeal.image
      form.description = setmeal.description
      form.setmealDishes = setmeal.setmealDishes?.length ? [...setmeal.setmealDishes] : []
      // Store id for edit
      ;(form as any).id = setmeal.id
    }
  } catch {
    ElMessage.error('获取套餐信息失败')
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

  if (form.setmealDishes.length === 0) {
    ElMessage.warning('请至少选择一个菜品')
    return
  }

  submitting.value = true
  try {
    const api = isEdit.value ? editSetmeal : addSetmeal
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

// Watch dish select dialog open to reset temp selection
import { watch } from 'vue'
watch(dishSelectVisible, (val) => {
  if (val) {
    tempSelectedDishes.value = form.setmealDishes.map((d) => ({ ...d }))
    fetchDishes()
  }
})

onMounted(() => {
  fetchCategories()
  fetchSetmealDetail()
})
</script>

<style lang="scss" scoped>
.setmeal-edit-page {
  max-width: 900px;
}

.dish-cat-tab {
  padding: 4px 14px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  transition: all 0.3s;

  &:hover {
    color: #1a56db;
  }

  &.active {
    color: #fff;
    background: #1a56db;
  }
}

.dish-grid {
  max-height: 400px;
  overflow-y: auto;
}

.dish-card {
  border-color: #e4e7ed;
  margin-bottom: 8px;

  &:hover {
    border-color: #1a56db;
    background: #eff6ff;
  }

  &.selected {
    border-color: #1a56db;
    background: #dbeafe;
  }
}
</style>

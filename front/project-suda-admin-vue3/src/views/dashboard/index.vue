<template>
  <div class="dashboard-page p-6">
    <!-- Page Header -->
    <div class="mb-6">
      <h2 class="text-xl font-bold text-gray-800">工作台</h2>
      <p class="text-sm text-gray-400 mt-1">欢迎回来，查看今日经营数据</p>
    </div>

    <!-- Row 1: Business Data KPI Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4 mb-6">
      <StatCard
        title="营业额"
        :value="formatAmount(businessData.turnover)"
        unit="元"
        icon="Money"
        color="red"
        :loading="loadingBusiness"
      />
      <StatCard
        title="有效订单"
        :value="businessData.validOrderCount"
        unit="单"
        icon="ShoppingCartFull"
        color="blue"
        :loading="loadingBusiness"
      />
      <StatCard
        title="订单完成率"
        :value="formatPercent(businessData.orderCompletionRate)"
        unit="%"
        icon="CircleCheck"
        color="green"
        :loading="loadingBusiness"
      />
      <StatCard
        title="平均客单价"
        :value="formatAmount(businessData.unitPrice)"
        unit="元"
        icon="Coin"
        color="orange"
        :loading="loadingBusiness"
      />
      <StatCard
        title="新增用户"
        :value="businessData.newUsers"
        unit="人"
        icon="User"
        color="purple"
        :loading="loadingBusiness"
      />
    </div>

    <!-- Row 2: Order Status Overview -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-6">
      <h3 class="text-base font-semibold text-gray-800 mb-5">订单概览</h3>

      <!-- Loading skeleton -->
      <div v-if="loadingOrders" class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-4">
        <div
          v-for="i in 5"
          :key="i"
          class="animate-pulse flex flex-col items-center gap-3 p-5 rounded-xl bg-gray-50"
        >
          <div class="h-5 w-16 bg-gray-200 rounded"></div>
          <div class="h-8 w-14 bg-gray-200 rounded"></div>
        </div>
      </div>

      <!-- Order stat items -->
      <div v-else class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-4">
        <div
          v-for="item in orderCards"
          :key="item.key"
          class="order-stat-card"
        >
          <span class="text-2xl font-bold" :class="item.colorClass">{{ item.value }}</span>
          <span class="text-sm text-gray-500 mt-1">{{ item.label }}</span>
        </div>
      </div>
    </div>

    <!-- Row 3: Dish & Setmeal Two-Column Overview -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <!-- Dish Overview -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
        <div class="flex items-center justify-between mb-5">
          <h3 class="text-base font-semibold text-gray-800">菜品概览</h3>
          <router-link
            to="/dish"
            class="text-sm text-[#1a56db] hover:text-[#1e3a8a] font-medium hover:underline transition-colors"
          >
            查看全部
          </router-link>
        </div>

        <div v-if="loadingDishes" class="flex justify-center py-10">
          <el-icon class="is-loading" :size="28" color="#1a56db">
            <Loading />
          </el-icon>
        </div>
        <div v-else class="grid grid-cols-2 gap-4">
          <div class="overview-item overview-sold">
            <div class="text-3xl font-bold">{{ dishData.sold }}</div>
            <div class="text-sm mt-1">已上架</div>
          </div>
          <div class="overview-item overview-discontinued">
            <div class="text-3xl font-bold">{{ dishData.discontinued }}</div>
            <div class="text-sm mt-1">已停售</div>
          </div>
        </div>

        <div class="mt-5 pt-5 border-t border-gray-100">
          <router-link to="/dish/edit">
            <el-button type="primary" :icon="Plus">新增菜品</el-button>
          </router-link>
        </div>
      </div>

      <!-- Setmeal Overview -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
        <div class="flex items-center justify-between mb-5">
          <h3 class="text-base font-semibold text-gray-800">套餐概览</h3>
          <router-link
            to="/setmeal"
            class="text-sm text-[#1a56db] hover:text-[#1e3a8a] font-medium hover:underline transition-colors"
          >
            查看全部
          </router-link>
        </div>

        <div v-if="loadingSetmeals" class="flex justify-center py-10">
          <el-icon class="is-loading" :size="28" color="#1a56db">
            <Loading />
          </el-icon>
        </div>
        <div v-else class="grid grid-cols-2 gap-4">
          <div class="overview-item overview-sold">
            <div class="text-3xl font-bold">{{ setmealData.sold }}</div>
            <div class="text-sm mt-1">已上架</div>
          </div>
          <div class="overview-item overview-discontinued">
            <div class="text-3xl font-bold">{{ setmealData.discontinued }}</div>
            <div class="text-sm mt-1">已停售</div>
          </div>
        </div>

        <div class="mt-5 pt-5 border-t border-gray-100">
          <router-link to="/setmeal/edit">
            <el-button type="primary" :icon="Plus">新增套餐</el-button>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Loading, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import StatCard from '@/components/StatCard.vue'
import {
  getBusinessData,
  getOverviewOrders,
  getOverviewDishes,
  getOverviewSetmeals,
} from '@/api/modules/workspace'
import type {
  BusinessDataVO,
  OrderOverViewVO,
  DishOverViewVO,
  SetmealOverViewVO,
} from '@/api/modules/workspace'

// ── Loading states ──
const loadingBusiness = ref(true)
const loadingOrders = ref(true)
const loadingDishes = ref(true)
const loadingSetmeals = ref(true)

// ── Reactive data ──
const businessData = reactive<BusinessDataVO>({
  turnover: 0,
  validOrderCount: 0,
  orderCompletionRate: 0,
  unitPrice: 0,
  newUsers: 0,
})

const orderData = reactive<OrderOverViewVO>({
  allOrders: 0,
  cancelledOrders: 0,
  completedOrders: 0,
  deliveredOrders: 0,
  waitingOrders: 0,
})

const orderCards = computed(() => [
  { key: 'all',       label: '全部订单', value: orderData.allOrders,        colorClass: 'text-blue-600' },
  { key: 'waiting',   label: '待处理',   value: orderData.waitingOrders,    colorClass: 'text-orange-500' },
  { key: 'delivered', label: '派送中',   value: orderData.deliveredOrders,  colorClass: 'text-blue-500' },
  { key: 'completed', label: '已完成',   value: orderData.completedOrders,  colorClass: 'text-green-600' },
  { key: 'cancelled', label: '已取消',   value: orderData.cancelledOrders,  colorClass: 'text-gray-400' },
])

const dishData = reactive<DishOverViewVO>({ sold: 0, discontinued: 0 })
const setmealData = reactive<SetmealOverViewVO>({ sold: 0, discontinued: 0 })

// ── Format helpers ──
function formatAmount(value: number): string {
  if (value == null) return '0'
  return value % 1 === 0 ? value.toLocaleString() : value.toFixed(2)
}

function formatPercent(value: number): string {
  if (value == null) return '0'
  return value % 1 === 0 ? String(value) : value.toFixed(1)
}

// ── API calls ──
async function fetchBusinessData() {
  try {
    const { data } = await getBusinessData()
    if (data.code === 1 && data.data) {
      Object.assign(businessData, data.data)
    }
  } catch {
    ElMessage.error('获取营业数据失败')
  } finally {
    loadingBusiness.value = false
  }
}

async function fetchOverviewOrders() {
  try {
    const { data } = await getOverviewOrders()
    if (data.code === 1 && data.data) {
      Object.assign(orderData, data.data)
    }
  } catch {
    ElMessage.error('获取订单概览失败')
  } finally {
    loadingOrders.value = false
  }
}

async function fetchOverviewDishes() {
  try {
    const { data } = await getOverviewDishes()
    if (data.code === 1 && data.data) {
      Object.assign(dishData, data.data)
    }
  } catch {
    ElMessage.error('获取菜品概览失败')
  } finally {
    loadingDishes.value = false
  }
}

async function fetchOverviewSetmeals() {
  try {
    const { data } = await getOverviewSetmeals()
    if (data.code === 1 && data.data) {
      Object.assign(setmealData, data.data)
    }
  } catch {
    ElMessage.error('获取套餐概览失败')
  } finally {
    loadingSetmeals.value = false
  }
}

onMounted(() => {
  fetchBusinessData()
  fetchOverviewOrders()
  fetchOverviewDishes()
  fetchOverviewSetmeals()
})
</script>

<style scoped>
.dashboard-page {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
}

html.dark .dashboard-page {
  background: #0f172a;
}

/* Order status cards */
.order-stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 12px;
  border-radius: 12px;
  background: #fafbfc;
  border: 1px solid #f0f0f0;
  transition: all 0.2s ease;
  cursor: default;
}

html.dark .order-stat-card {
  background: #1e293b;
  border-color: #334155;
}

.order-stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  border-color: #e0e0e0;
}

html.dark .order-stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.35);
  border-color: #475569;
}

/* Dish/Setmeal overview items */
.overview-item {
  border-radius: 12px;
  padding: 24px 16px;
  text-align: center;
}

.overview-sold {
  background: #e8f5e9;
}

html.dark .overview-sold {
  background: rgba(16, 185, 129, 0.12);
}

.overview-sold .text-3xl {
  color: #2e7d32;
}

html.dark .overview-sold .text-3xl {
  color: #10b981;
}

.overview-sold .text-sm {
  color: #4caf50;
}

html.dark .overview-sold .text-sm {
  color: #34d399;
}

.overview-discontinued {
  background: #fafafa;
}

html.dark .overview-discontinued {
  background: #1e293b;
}

.overview-discontinued .text-3xl {
  color: #9e9e9e;
}

html.dark .overview-discontinued .text-3xl {
  color: #94a3b8;
}

.overview-discontinued .text-sm {
  color: #bdbdbd;
}

html.dark .overview-discontinued .text-sm {
  color: #64748b;
}
</style>

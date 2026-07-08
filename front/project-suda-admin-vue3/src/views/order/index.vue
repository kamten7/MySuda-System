<template>
  <div class="order-page">
    <!-- Status Tabs -->
    <div class="bg-white rounded-lg p-4 mb-4 shadow-sm">
      <div class="flex flex-wrap gap-2">
        <div
          v-for="tab in statusTabs"
          :key="tab.value"
          class="status-tab"
          :class="{ active: activeStatus === tab.value }"
          @click="handleTabChange(tab.value)"
        >
          <span>{{ tab.label }}</span>
          <span v-if="tab.count !== null" class="tab-badge">{{ tab.count }}</span>
        </div>
      </div>
    </div>

    <!-- Search Bar -->
    <div class="bg-white rounded-lg p-4 mb-4 shadow-sm">
      <el-form :model="searchForm" :inline="true" size="default">
        <el-form-item label="订单号">
          <el-input
            v-model="searchForm.number"
            placeholder="请输入订单号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="searchForm.phone"
            placeholder="请输入手机号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px"
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
        <el-table-column label="订单号" prop="number" min-width="180" />

        <!-- Status column for "全部" tab -->
        <el-table-column v-if="activeStatus === 0" label="订单状态" prop="status" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- Dishes column for status 2,3,4 -->
        <el-table-column
          v-if="[2, 3, 4].includes(activeStatus)"
          label="菜品信息"
          prop="orderDishes"
          min-width="200"
          show-overflow-tooltip
        />

        <!-- Consignee & phone for status 0,5,6 -->
        <el-table-column
          v-if="[0, 5, 6].includes(activeStatus)"
          label="收货人"
          prop="consignee"
          min-width="100"
        />
        <el-table-column
          v-if="[0, 5, 6].includes(activeStatus)"
          label="手机号"
          prop="phone"
          min-width="130"
        />

        <!-- Address for status 0,2,3,4,5,6 -->
        <el-table-column
          v-if="[0, 2, 3, 4, 5, 6].includes(activeStatus)"
          label="地址"
          prop="address"
          min-width="200"
          show-overflow-tooltip
        />

        <!-- Order time for status 0,6 -->
        <el-table-column v-if="[0, 6].includes(activeStatus)" label="下单时间" prop="orderTime" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.orderTime) }}
          </template>
        </el-table-column>

        <!-- Cancel info for status 6 -->
        <el-table-column v-if="activeStatus === 6" label="取消时间" prop="cancelTime" min-width="170">
          <template #default="{ row }">
            {{ row.cancelTime ? formatDateTime(row.cancelTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column
          v-if="activeStatus === 6"
          label="取消原因"
          prop="cancelReason"
          min-width="150"
          show-overflow-tooltip
        />

        <!-- Amount -->
        <el-table-column label="金额" prop="amount" min-width="100">
          <template #default="{ row }">
            <span class="text-primary font-medium">&yen;{{ (row.amount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>

        <!-- Actions -->
        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default="scope: any">
            <div class="flex gap-1 flex-wrap">
              <!-- 接单 (status 2) -->
              <el-button
                v-if="scope.row.status === 2"
                type="success"
                size="small"
                @click="handleConfirm(scope.row)"
              >
                接单
              </el-button>
              <!-- 拒单 (status 2) -->
              <el-button
                v-if="scope.row.status === 2"
                type="danger"
                size="small"
                @click="handleReject(scope.row)"
              >
                拒单
              </el-button>
              <!-- 派送 (status 3) -->
              <el-button
                v-if="scope.row.status === 3"
                type="primary"
                size="small"
                @click="handleDeliver(scope.row)"
              >
                派送
              </el-button>
              <!-- 完成 (status 4) -->
              <el-button
                v-if="scope.row.status === 4"
                type="success"
                size="small"
                @click="handleComplete(scope.row)"
              >
                完成
              </el-button>
              <!-- 详情 -->
              <el-button type="info" size="small" @click="handleDetail(scope.row)">详情</el-button>
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
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- Reject / Cancel Reason Dialog -->
    <el-dialog
      v-model="reasonDialog.visible"
      :title="reasonDialog.title"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form :model="reasonDialog" label-width="80px">
        <el-form-item label="原因">
          <el-input
            v-model="reasonDialog.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reasonDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitReason">确定</el-button>
      </template>
    </el-dialog>

    <!-- Order Detail Drawer -->
    <el-drawer
      v-model="detailDrawer.visible"
      title="订单详情"
      size="520px"
      :close-on-click-modal="false"
    >
      <template v-if="detailDrawer.order">
        <div class="px-4">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="订单号">{{ detailDrawer.order.number }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusTagType(detailDrawer.order.status)" size="small">
                {{ getStatusLabel(detailDrawer.order.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="收货人">{{ detailDrawer.order.consignee }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ detailDrawer.order.phone }}</el-descriptions-item>
            <el-descriptions-item label="地址">{{ detailDrawer.order.address }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">
              {{ detailDrawer.order.orderTime ? formatDateTime(detailDrawer.order.orderTime) : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="金额">
              <span class="text-primary font-medium">&yen;{{ (detailDrawer.order.amount || 0).toFixed(2) }}</span>
            </el-descriptions-item>
            <el-descriptions-item v-if="detailDrawer.order.cancelTime" label="取消时间">
              {{ formatDateTime(detailDrawer.order.cancelTime) }}
            </el-descriptions-item>
            <el-descriptions-item v-if="detailDrawer.order.cancelReason" label="取消原因">
              {{ detailDrawer.order.cancelReason }}
            </el-descriptions-item>
            <el-descriptions-item v-if="detailDrawer.order.rejectionReason" label="拒单原因">
              {{ detailDrawer.order.rejectionReason }}
            </el-descriptions-item>
            <el-descriptions-item v-if="detailDrawer.order.remark" label="备注">
              {{ detailDrawer.order.remark }}
            </el-descriptions-item>
          </el-descriptions>

          <!-- Order items -->
          <div v-if="detailDrawer.order.orderDetailList && detailDrawer.order.orderDetailList.length > 0" class="mt-4">
            <h4 class="text-base font-semibold mb-2">菜品明细</h4>
            <el-table :data="detailDrawer.order.orderDetailList" size="small" border>
              <el-table-column label="菜品名称" prop="name" />
              <el-table-column label="数量" prop="number" width="80" />
              <el-table-column label="金额" width="100">
                <template #default="{ row: item }">
                  &yen;{{ (item.amount || 0).toFixed(2) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/date'
import {
  getOrderPage,
  getOrderStatistics,
  getOrderDetail,
  confirmOrder,
  rejectOrder,
  deliverOrder,
  completeOrder,
} from '@/api/modules/order'
import type { OrderRecord, OrderQuery, OrderDetail } from '@/api/modules/order'

// Status tabs configuration
const statusTabs = ref([
  { label: '全部', value: 0, count: null as number | null },
  { label: '待付款', value: 1, count: null as number | null },
  { label: '待接单', value: 2, count: null as number | null },
  { label: '待派送', value: 3, count: null as number | null },
  { label: '派送中', value: 4, count: null as number | null },
  { label: '已完成', value: 5, count: null as number | null },
  { label: '已取消', value: 6, count: null as number | null },
])

const activeStatus = ref(0)

const statusLabelMap: Record<number, string> = {
  1: '待付款',
  2: '待接单',
  3: '待派送',
  4: '派送中',
  5: '已完成',
  6: '已取消',
}

const statusColorMap: Record<number, string> = {
  1: 'warning',
  2: 'danger',
  3: 'info',
  4: 'primary',
  5: 'success',
  6: 'info',
}

function getStatusLabel(status: number): string {
  return statusLabelMap[status] || '未知'
}

function getStatusTagType(status: number): 'success' | 'warning' | 'info' | 'danger' | 'primary' {
  return (statusColorMap[status] || 'info') as 'success' | 'warning' | 'info' | 'danger' | 'primary'
}

// Search form
const searchForm = reactive({
  number: '',
  phone: '',
  dateRange: null as [string, string] | null,
})

// Pagination
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// Table
const loading = ref(false)
const tableData = ref<OrderRecord[]>([])

// Reason dialog (for reject / cancel)
const reasonDialog = reactive({
  visible: false,
  title: '',
  reason: '',
  orderId: 0,
  type: '' as 'reject' | 'cancel',
})

// Detail drawer
const detailDrawer = reactive({
  visible: false,
  order: null as OrderRecord | null,
})

// Fetch statistics
async function fetchStatistics() {
  try {
    const res = await getOrderStatistics()
    if (res.data.code === 1 && res.data.data) {
      const stats = res.data.data
      // Map API stats to tabs
      // API returns: { toBeConfirmed, confirmed, deliveryInProgress }
      statusTabs.value = statusTabs.value.map((tab) => {
        switch (tab.value) {
          case 2: // 待接单
            return { ...tab, count: stats.toBeConfirmed ?? 0 }
          case 3: // 待派送
            return { ...tab, count: stats.confirmed ?? 0 }
          case 4: // 派送中
            return { ...tab, count: stats.deliveryInProgress ?? 0 }
          default:
            return { ...tab, count: null }
        }
      })
    }
  } catch {
    // Statistics fetch failure is non-blocking
  }
}

// Fetch table data
async function fetchData() {
  loading.value = true
  try {
    const params: OrderQuery = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }

    if (searchForm.number) params.number = searchForm.number
    if (searchForm.phone) params.phone = searchForm.phone
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.beginTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
    }
    if (activeStatus.value !== 0) {
      params.status = activeStatus.value
    }

    const res = await getOrderPage(params)
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

// Handle tab change
function handleTabChange(status: number) {
  activeStatus.value = status
  pagination.page = 1
  fetchData()
}

// Handle search
function handleSearch() {
  pagination.page = 1
  fetchData()
}

// Handle reset
function handleReset() {
  searchForm.number = ''
  searchForm.phone = ''
  searchForm.dateRange = null
  activeStatus.value = 0
  pagination.page = 1
  fetchData()
}

// Handle pagination
function handlePageChange(page: number) {
  pagination.page = page
  fetchData()
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchData()
}

// Confirm order (接单)
async function handleConfirm(row: OrderRecord) {
  try {
    await ElMessageBox.confirm(`确认接单: ${row.number}?`, '接单确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    })
    const res = await confirmOrder({ id: row.id })
    if (res.data.code === 1) {
      ElMessage.success('接单成功')
      fetchData()
      fetchStatistics()
    }
  } catch {
    // User cancelled or error
  }
}

// Reject order (拒单)
function handleReject(row: OrderRecord) {
  reasonDialog.visible = true
  reasonDialog.title = '拒单原因'
  reasonDialog.reason = ''
  reasonDialog.orderId = row.id
  reasonDialog.type = 'reject'
}

// Deliver order (派送)
async function handleDeliver(row: OrderRecord) {
  try {
    await ElMessageBox.confirm(`确认派送订单: ${row.number}?`, '派送确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    })
    const res = await deliverOrder(row.id)
    if (res.data.code === 1) {
      ElMessage.success('派送成功')
      fetchData()
      fetchStatistics()
    }
  } catch {
    // User cancelled or error
  }
}

// Complete order (完成)
async function handleComplete(row: OrderRecord) {
  try {
    await ElMessageBox.confirm(`确认完成订单: ${row.number}?`, '完成确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    })
    const res = await completeOrder(row.id)
    if (res.data.code === 1) {
      ElMessage.success('订单已完成')
      fetchData()
      fetchStatistics()
    }
  } catch {
    // User cancelled or error
  }
}

// Submit reason dialog
async function submitReason() {
  if (!reasonDialog.reason.trim()) {
    ElMessage.warning('请输入原因')
    return
  }

  try {
    if (reasonDialog.type === 'reject') {
      const res = await rejectOrder({
        id: reasonDialog.orderId,
        rejectionReason: reasonDialog.reason,
      })
      if (res.data.code === 1) {
        ElMessage.success('已拒单')
      }
    }
    reasonDialog.visible = false
    fetchData()
    fetchStatistics()
  } catch {
    // Error handled by interceptor
  }
}

// View detail
async function handleDetail(row: OrderRecord) {
  detailDrawer.visible = true
  detailDrawer.order = null
  try {
    const res = await getOrderDetail(row.id)
    if (res.data.code === 1) {
      detailDrawer.order = res.data.data
    }
  } catch {
    detailDrawer.order = null
  }
}

onMounted(() => {
  fetchStatistics()
  fetchData()
})
</script>

<style lang="scss" scoped>
.order-page {
  padding: 0;
}

.status-tab {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  background: #f5f7fa;
  transition: all 0.3s;
  user-select: none;

  &:hover {
    color: #1a56db;
    background: #eff6ff;
  }

  &.active {
    color: #fff;
    background: #1a56db;
    font-weight: 600;

    .tab-badge {
      background: rgba(255, 255, 255, 0.25);
      color: #fff;
    }
  }
}

.tab-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  font-size: 12px;
  background: #e6e8eb;
  color: #606266;
  line-height: 1;
}
</style>

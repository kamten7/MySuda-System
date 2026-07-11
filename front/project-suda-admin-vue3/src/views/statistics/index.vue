<template>
  <div class="page-container">
    <!-- Header: Time Range Tabs + Export -->
    <div class="content-card mb-16">
      <div class="statistics-header">
        <div class="time-tabs">
          <button
            v-for="tab in timeTabs"
            :key="tab.value"
            class="time-tab-btn"
            :class="{ active: activeType === tab.value }"
            @click="handleTabChange(tab.value)"
          >
            {{ tab.label }}
          </button>
        </div>
        <el-button type="primary" :loading="exportLoading" @click="handleExport">
          <el-icon class="mr-8"><Download /></el-icon>
          导出报表
        </el-button>
      </div>
    </div>

    <!-- Content -->
    <div v-loading="loading">
      <!-- Row 1: Overview Cards -->
      <el-row :gutter="16" class="mb-16">
        <el-col
          v-for="card in overviewCards"
          :key="card.label"
          :xs="24"
          :sm="12"
          :md="6"
          class="mb-16-xs"
        >
          <div class="stat-card overview-card">
            <div class="overview-card-inner">
              <div class="overview-icon" :style="{ backgroundColor: card.bgColor }">
                <el-icon :size="24" :color="card.iconColor">
                  <component :is="card.icon" />
                </el-icon>
              </div>
              <div class="overview-info">
                <span class="overview-label">{{ card.label }}</span>
                <span class="overview-value" :style="{ color: card.valueColor }">
                  {{ card.prefix }}{{ card.value }}
                </span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- Row 2: Turnover Trend (full width) -->
      <div class="content-card mb-16">
        <div class="chart-header">
          <h3 class="chart-title">营业额趋势</h3>
        </div>
        <div ref="turnoverChartRef" class="chart-container"></div>
      </div>

      <!-- Row 3: User Statistics | Order Statistics -->
      <el-row :gutter="16" class="mb-16">
        <el-col :xs="24" :lg="12" class="mb-16-xs">
          <div class="content-card">
            <div class="chart-header">
              <h3 class="chart-title">用户统计</h3>
            </div>
            <div ref="userChartRef" class="chart-container"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="content-card">
            <div class="chart-header">
              <h3 class="chart-title">订单统计</h3>
            </div>
            <div ref="orderChartRef" class="chart-container"></div>
          </div>
        </el-col>
      </el-row>

      <!-- Row 4: Top10 Sales | Data Summary -->
      <el-row :gutter="16" class="mb-16">
        <el-col :xs="24" :lg="12" class="mb-16-xs">
          <div class="content-card">
            <div class="chart-header">
              <h3 class="chart-title">销量Top10</h3>
            </div>
            <div ref="top10ChartRef" class="chart-container chart-container--bar"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="content-card">
            <div class="chart-header">
              <h3 class="chart-title">当前数据概况</h3>
              <span class="chart-subtitle">
                {{ dateRangeLabel }}
              </span>
            </div>
            <div class="summary-list">
              <div class="summary-item">
                <span class="summary-label">营业额</span>
                <span class="summary-value summary-value--primary">
                  &yen; {{ formatAmount(summaryData.turnover) }}
                </span>
              </div>
              <div class="summary-item">
                <span class="summary-label">有效订单数</span>
                <span class="summary-value">{{ summaryData.validOrders }}</span>
              </div>
              <div class="summary-item">
                <span class="summary-label">订单完成率</span>
                <span class="summary-value summary-value--accent">
                  {{ formatRate(summaryData.completionRate) }}
                </span>
              </div>
              <div class="summary-item">
                <span class="summary-label">新增用户</span>
                <span class="summary-value">{{ summaryData.newUsers }}</span>
              </div>
              <div class="summary-item">
                <span class="summary-label">累计用户</span>
                <span class="summary-value">{{ summaryData.totalUsers }}</span>
              </div>
              <div class="summary-item">
                <span class="summary-label">总订单数</span>
                <span class="summary-value">{{ summaryData.totalOrders }}</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue'
import * as echarts from 'echarts'
import type { DateRangeType } from '@/utils/date'
import { getDateRange, formatAmount, formatRate } from '@/utils/date'
import {
  getTurnoverStatistics,
  getUserStatistics,
  getOrderReportStatistics,
  getTop10,
  exportReport,
  type TurnoverReportVO,
  type UserReportVO,
  type OrderReportVO,
  type SalesTop10ReportVO,
} from '@/api/modules/report'
import { useChart } from '@/composables/useChart'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

// ==================== Time range ====================
const timeTabs = [
  { label: '昨日', value: 'yesterday' as DateRangeType },
  { label: '近7日', value: 'last7' as DateRangeType },
  { label: '近30日', value: 'last30' as DateRangeType },
  { label: '本周', value: 'thisWeek' as DateRangeType },
  { label: '本月', value: 'thisMonth' as DateRangeType },
]

const activeType = ref<DateRangeType>('last7')

const dateRangeLabel = computed(() => {
  const range = getDateRange(activeType.value)
  return `${range.start} ~ ${range.end}`
})

function handleTabChange(type: DateRangeType) {
  activeType.value = type
  fetchAllData()
}

// ==================== Loading & export ====================
const loading = ref(false)
const exportLoading = ref(false)

async function handleExport() {
  exportLoading.value = true
  try {
    const response = await exportReport()
    const blob =
      response.data instanceof Blob
        ? response.data
        : new Blob([response.data], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `速达外卖数据报表_${dayjs().format('YYYY-MM-DD')}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.warning('报表导出失败，请稍后重试')
  } finally {
    exportLoading.value = false
  }
}

// ==================== API data ====================
const turnoverData = ref<TurnoverReportVO>({ dateList: [], turnoverList: [] })
const userData = ref<UserReportVO>({ dateList: [], totalUserList: [], newUserList: [] })
const orderData = ref<OrderReportVO>({
  dateList: [],
  orderCountList: [],
  validOrderCountList: [],
  orderCompletionRate: 0,
  validOrderCount: 0,
  totalOrderCount: 0,
})
const top10Data = ref<SalesTop10ReportVO>({ nameList: [], numberList: [] })

async function fetchAllData() {
  loading.value = true

  try {
    const range = getDateRange(activeType.value)
    const params = { begin: range.start, end: range.end }

    const [turnoverRes, userRes, orderRes, top10Res] = await Promise.allSettled([
      getTurnoverStatistics(params),
      getUserStatistics(params),
      getOrderReportStatistics(params),
      getTop10(params),
    ])

    // Count API failures for accurate error reporting
    let failures = 0

    if (turnoverRes.status === 'fulfilled' && turnoverRes.value.data.code === 1) {
      turnoverData.value = turnoverRes.value.data.data
    } else {
      failures++
    }
    if (userRes.status === 'fulfilled' && userRes.value.data.code === 1) {
      userData.value = userRes.value.data.data
    } else {
      failures++
    }
    if (orderRes.status === 'fulfilled' && orderRes.value.data.code === 1) {
      orderData.value = orderRes.value.data.data
    } else {
      failures++
    }
    if (top10Res.status === 'fulfilled' && top10Res.value.data.code === 1) {
      top10Data.value = top10Res.value.data.data
    } else {
      failures++
    }

    if (failures > 0) {
      console.warn(`Statistics: ${failures}/4 APIs failed`)
      if (failures === 4) {
        ElMessage.error('数据加载失败，请确认后端服务已启动')
      }
    }
  } catch (e) {
    console.error('Statistics error:', e)
    ElMessage.error('数据加载异常，请刷新页面重试')
  } finally {
    loading.value = false
  }

  // Chart update outside try-catch — chart errors shouldn't show API error messages
  await nextTick()
  updateAllCharts()
}

// ==================== Overview cards ====================
interface OverviewCard {
  label: string
  value: string
  prefix: string
  bgColor: string
  iconColor: string
  valueColor: string
  icon: string
}

const overviewCards = computed<OverviewCard[]>(() => {
  const turnoverTotal = turnoverData.value.turnoverList.reduce((sum, v) => sum + v, 0)
  const newUserTotal = userData.value.newUserList.reduce((sum, v) => sum + v, 0)

  return [
    {
      label: '营业额（元）',
      value: formatAmount(turnoverTotal),
      prefix: '¥ ',
      bgColor: 'rgba(26, 86, 219, 0.1)',
      iconColor: '#1a56db',
      valueColor: '#1a56db',
      icon: 'Money',
    },
    {
      label: '有效订单',
      value: String(orderData.value.validOrderCount ?? 0),
      prefix: '',
      bgColor: 'rgba(64, 158, 255, 0.1)',
      iconColor: '#409eff',
      valueColor: '#409eff',
      icon: 'DocumentChecked',
    },
    {
      label: '订单完成率',
      value: formatRate(orderData.value.orderCompletionRate ?? 0),
      prefix: '',
      bgColor: 'rgba(103, 194, 58, 0.1)',
      iconColor: '#67c23a',
      valueColor: '#67c23a',
      icon: 'TrendCharts',
    },
    {
      label: '新增用户',
      value: String(newUserTotal),
      prefix: '',
      bgColor: 'rgba(245, 158, 11, 0.1)',
      iconColor: '#f59e0b',
      valueColor: '#d97706',
      icon: 'UserFilled',
    },
  ]
})

// ==================== Summary data ====================
const summaryData = computed(() => {
  const turnoverTotal = turnoverData.value.turnoverList.reduce((sum, v) => sum + v, 0)
  const newUserTotal = userData.value.newUserList.reduce((sum, v) => sum + v, 0)
  const totalUsers =
    userData.value.totalUserList.length > 0
      ? userData.value.totalUserList[userData.value.totalUserList.length - 1]
      : 0

  return {
    turnover: turnoverTotal,
    validOrders: orderData.value.validOrderCount ?? 0,
    completionRate: orderData.value.orderCompletionRate ?? 0,
    newUsers: newUserTotal,
    totalUsers,
    totalOrders: orderData.value.totalOrderCount ?? 0,
  }
})

// ==================== Chart refs & composables ====================
const turnoverChartRef = ref<HTMLElement>()
const userChartRef = ref<HTMLElement>()
const orderChartRef = ref<HTMLElement>()
const top10ChartRef = ref<HTMLElement>()

const { setOption: setTurnoverOption } = useChart(turnoverChartRef)
const { setOption: setUserOption } = useChart(userChartRef)
const { setOption: setOrderOption } = useChart(orderChartRef)
const { setOption: setTop10Option } = useChart(top10ChartRef)

function updateAllCharts() {
  updateTurnoverChart()
  updateUserChart()
  updateOrderChart()
  updateTop10Chart()
}

// ==================== Turnover line chart ====================
function updateTurnoverChart() {
  const { dateList, turnoverList } = turnoverData.value
  if (!dateList.length) return

  setTurnoverOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const p = params[0]
        return `<div style="font-size:13px">${p.axisValue}</div>
          <div style="font-weight:600;color:#1a56db;margin-top:4px">
            &yen; ${formatAmount(p.value)}
          </div>`
      },
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dateList,
      axisLine: { lineStyle: { color: '#ccc' } },
      axisTick: { show: false },
      axisLabel: { color: '#666', fontSize: 12 },
    },
    yAxis: {
      type: 'value',
      name: '元',
      nameTextStyle: { color: '#999', fontSize: 12 },
      splitLine: { lineStyle: { color: '#eee', type: 'dashed' } },
      axisLabel: {
        color: '#666',
        fontSize: 12,
        formatter: (val: number) => (val >= 1000 ? `${(val / 1000).toFixed(1)}k` : String(val)),
      },
    },
    series: [
      {
        name: '营业额',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        showSymbol: false,
        emphasis: { focus: 'series' },
        lineStyle: { color: '#1a56db', width: 2 },
        itemStyle: { color: '#1a56db' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(26, 86, 219, 0.28)' },
            { offset: 1, color: 'rgba(26, 86, 219, 0.02)' },
          ]),
        },
        data: turnoverList,
      },
    ],
  })
}

// ==================== User dual-line chart ====================
function updateUserChart() {
  const { dateList, totalUserList, newUserList } = userData.value
  if (!dateList.length) return

  setUserOption({
    tooltip: {
      trigger: 'axis',
    },
    legend: {
      data: ['累计用户', '新增用户'],
      bottom: 0,
      textStyle: { fontSize: 12, color: '#666' },
      itemWidth: 12,
      itemHeight: 8,
    },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '5%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dateList,
      axisLine: { lineStyle: { color: '#ccc' } },
      axisTick: { show: false },
      axisLabel: { color: '#666', fontSize: 12 },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#eee', type: 'dashed' } },
      axisLabel: { color: '#666', fontSize: 12 },
    },
    series: [
      {
        name: '累计用户',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        showSymbol: false,
        emphasis: { focus: 'series' },
        lineStyle: { color: '#409eff', width: 2 },
        itemStyle: { color: '#409eff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.25)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.02)' },
          ]),
        },
        data: totalUserList,
      },
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        showSymbol: false,
        emphasis: { focus: 'series' },
        lineStyle: { color: '#67c23a', width: 2 },
        itemStyle: { color: '#67c23a' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.25)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.02)' },
          ]),
        },
        data: newUserList,
      },
    ],
  })
}

// ==================== Order dual-line chart ====================
function updateOrderChart() {
  const { dateList, orderCountList, validOrderCountList, orderCompletionRate } = orderData.value
  if (!dateList.length) return

  setOrderOption({
    tooltip: {
      trigger: 'axis',
    },
    legend: {
      data: ['订单总数', '有效订单'],
      bottom: 0,
      textStyle: { fontSize: 12, color: '#666' },
      itemWidth: 12,
      itemHeight: 8,
    },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '13%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dateList,
      axisLine: { lineStyle: { color: '#ccc' } },
      axisTick: { show: false },
      axisLabel: { color: '#666', fontSize: 12 },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#eee', type: 'dashed' } },
      axisLabel: { color: '#666', fontSize: 12 },
    },
    graphic: [
      {
        type: 'text',
        left: 'center',
        top: '2%',
        style: {
          text: `订单完成率：${formatRate(orderCompletionRate ?? 0)}`,
          fontSize: 13,
          fontWeight: 'bold',
          fill: '#1a56db',
          fontFamily: 'Inter, PingFang SC, Microsoft YaHei, sans-serif',
        },
      },
    ],
    series: [
      {
        name: '订单总数',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        showSymbol: false,
        emphasis: { focus: 'series' },
        lineStyle: { color: '#f59e0b', width: 2 },
        itemStyle: { color: '#f59e0b' },
        data: orderCountList,
      },
      {
        name: '有效订单',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        showSymbol: false,
        emphasis: { focus: 'series' },
        lineStyle: { color: '#1a56db', width: 2 },
        itemStyle: { color: '#1a56db' },
        data: validOrderCountList,
      },
    ],
  })
}

// ==================== Top10 horizontal bar chart ====================
function updateTop10Chart() {
  const { nameList, numberList } = top10Data.value
  if (!nameList.length) return

  setTop10Option({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const p = params[0]
        return `<div style="font-size:13px">${p.name}</div>
          <div style="font-weight:600;color:#1a56db;margin-top:4px">
            ${p.value} 单
          </div>`
      },
    },
    grid: { left: '3%', right: '8%', bottom: '3%', top: '3%', containLabel: true },
    xAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#eee', type: 'dashed' } },
      axisLabel: { color: '#666', fontSize: 12 },
    },
    yAxis: {
      type: 'category',
      data: nameList,
      axisLine: { lineStyle: { color: '#ccc' } },
      axisTick: { show: false },
      axisLabel: { color: '#333', fontSize: 12 },
      inverse: true,
    },
    series: [
      {
        name: '销量',
        type: 'bar',
        data: numberList.map((v, i) => ({
          value: v,
          itemStyle: {
            borderRadius: [0, 4, 4, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#1a56db' },
              { offset: 1, color: i < 3 ? '#93c5fd' : '#dbeafe' },
            ]),
          },
        })),
        barMaxWidth: 28,
        label: {
          show: true,
          position: 'right',
          color: '#333',
          fontSize: 12,
          fontWeight: 500,
        },
      },
    ],
  })
}

// ==================== Init ====================
onMounted(() => {
  // onMounted 确保 useChart 的 onMounted 先初始化 ECharts 实例，再拉取数据
  nextTick(() => fetchAllData())
})
</script>

<style scoped lang="scss">
// ==================== Header ====================
.statistics-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.time-tabs {
  display: flex;
  gap: 4px;
  background: #f5f5f5;
  border-radius: 8px;
  padding: 4px;
}

.time-tab-btn {
  padding: 6px 18px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.25s ease;
  white-space: nowrap;

  &:hover {
    color: #1a56db;
    background: rgba(26, 86, 219, 0.06);
  }

  &.active {
    background: #1a56db;
    color: #fff;
    box-shadow: 0 2px 8px rgba(26, 86, 219, 0.25);
    font-weight: 500;
  }
}

// ==================== Overview Cards ====================
.overview-card {
  padding: 18px 20px;
  height: 100%;
}

.overview-card-inner {
  display: flex;
  align-items: center;
  gap: 16px;
}

.overview-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.overview-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.overview-label {
  font-size: 13px;
  color: #999;
  white-space: nowrap;
}

.overview-value {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
}

// ==================== Chart ====================
.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;

  &::before {
    content: '';
    display: inline-block;
    width: 4px;
    height: 16px;
    background-color: #1a56db;
    border-radius: 2px;
  }
}

.chart-subtitle {
  font-size: 12px;
  color: #999;
}

.chart-container {
  width: 100%;
  height: 360px;
}

.chart-container--bar {
  height: 400px;
}

// ==================== Summary ====================
.summary-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.summary-label {
  font-size: 14px;
  color: #666;
}

.summary-value {
  font-size: 16px;
  font-weight: 600;
  color: #333;

  &--primary {
    color: #1a56db;
    font-size: 18px;
  }

  &--accent {
    color: #67c23a;
    font-size: 16px;
  }
}

// ==================== Responsive ====================
.mb-16-xs {
  @media (max-width: 768px) {
    margin-bottom: 16px;
  }
}
</style>

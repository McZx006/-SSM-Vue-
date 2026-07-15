<template>
  <div class="dashboard-page">
    <div class="page-head">
      <div>
        <h2>仪表板</h2>
        <p>像小程序首页一样，快速查看本月收支、预算状态和消费结构。</p>
      </div>
      <div class="filters">
        <el-select v-model="selectedMonth" size="small" @change="loadDashboardData">
          <el-option
            v-for="month in availableMonths"
            :key="month.value"
            :label="month.label"
            :value="month.value"
          />
        </el-select>
        <el-select v-model="selectedYear" size="small" @change="loadDashboardData">
          <el-option
            v-for="year in availableYears"
            :key="year"
            :label="`${year}年`"
            :value="year"
          />
        </el-select>
      </div>
    </div>

    <el-card class="hero-card">
      <div class="hero-copy">
        <span class="eyebrow">本月账本</span>
        <h3>{{ selectedMonth }} 还剩 {{ monthRemainDays }} 天</h3>
        <p>{{ budgetStatusText }}</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="goBills">
          <el-icon><Plus /></el-icon>
          记一笔
        </el-button>
        <el-button plain @click="goStatistics">看分析</el-button>
        <el-button plain @click="goExport">导出账单</el-button>
      </div>
      <el-progress
        :percentage="budgetProgress"
        :status="dashboardData.budgetExceeded ? 'exception' : undefined"
        :stroke-width="12"
      />
    </el-card>

    <el-row :gutter="18" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card income clickable-card" @click="goBillsList('income')">
          <span class="stat-icon">入</span>
          <p>本月收入</p>
          <strong>¥{{ formatNumber(dashboardData.monthIncome) }}</strong>
          <small>点击查看收入账单</small>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card expense clickable-card" @click="goBillsList('expense')">
          <span class="stat-icon">出</span>
          <p>本月支出</p>
          <strong>¥{{ formatNumber(dashboardData.monthExpense) }}</strong>
          <small>点击查看支出账单</small>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card balance clickable-card" @click="goBillsList()">
          <span class="stat-icon">余</span>
          <p>本月结余</p>
          <strong>¥{{ formatNumber(dashboardData.monthBalance) }}</strong>
          <small>点击查看本月账单</small>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card budget clickable-card" @click="goBudgetSettings">
          <span class="stat-icon">预</span>
          <p>预算使用率</p>
          <strong>{{ formatNumber(dashboardData.budgetUsage) }}%</strong>
          <small>点击编辑月度预算</small>
        </el-card>
      </el-col>
    </el-row>

    <el-alert
      v-if="dashboardData.budgetExceeded"
      title="本月支出已超过预算，请注意控制开支。"
      type="warning"
      :closable="false"
      show-icon
      class="budget-alert"
    />

    <el-card class="panel-card advice-card">
      <template #header>
        <div class="panel-header">
          <span>智能消费建议</span>
          <span class="subtle">本地规则分析</span>
        </div>
      </template>
      <div class="advice-grid">
        <div v-for="item in dashboardAdvice" :key="item.title" class="advice-item">
          <strong>{{ item.title }}</strong>
          <p>{{ item.content }}</p>
        </div>
      </div>
    </el-card>

    <el-row :gutter="18">
      <el-col :xs="24" :xl="12">
        <el-card class="panel-card">
          <template #header>
            <div class="panel-header">
              <span>支出分类占比</span>
              <span class="subtle">{{ selectedMonth }}</span>
            </div>
          </template>
          <div class="chart-shell">
            <div v-if="!dashboardData.categoryStatistics.length" class="chart-empty">该月份暂无支出分类数据</div>
            <div ref="pieChartRef" class="chart-box"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="12">
        <el-card class="panel-card">
          <template #header>
            <div class="panel-header">
              <span>月度收支趋势</span>
              <span class="subtle">{{ selectedYear }}年</span>
            </div>
          </template>
          <div class="chart-shell">
            <div v-if="!dashboardData.monthlyStatistics.length" class="chart-empty">该年份暂无月度收支数据</div>
            <div ref="barChartRef" class="chart-box"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card recent-card">
      <template #header>
        <div class="panel-header">
          <span>最近账单</span>
          <router-link to="/bills">查看全部</router-link>
        </div>
      </template>

      <el-table :data="recentBills" stripe v-loading="loading">
        <el-table-column prop="date" label="日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.date) }}
          </template>
        </el-table-column>
        <el-table-column label="分类" width="140">
          <template #default="{ row }">
            <el-tag :type="row.type === 'income' ? 'success' : 'danger'">
              {{ formatCategoryName(row.category?.name) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column prop="amount" label="金额" width="140" align="right">
          <template #default="{ row }">
            <span :class="row.type === 'income' ? 'income-text' : 'expense-text'">
              {{ row.type === 'income' ? '+' : '-' }}¥{{ formatNumber(row.amount) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import api from '@/utils/api'
import { buildDashboardAdvice } from '@/utils/advice'
import { formatCategoryName } from '@/utils/category'

const router = useRouter()
const loading = ref(false)
const pieChartRef = ref(null)
const barChartRef = ref(null)
const selectedMonth = ref(dayjs().format('YYYY-MM'))
const selectedYear = ref(dayjs().year())

const dashboardData = reactive({
  monthIncome: 0,
  monthExpense: 0,
  monthBalance: 0,
  categoryStatistics: [],
  monthlyStatistics: [],
  budgetUsage: 0,
  budgetExceeded: false
})

const recentBills = ref([])

let pieChart = null
let barChart = null

const availableMonths = computed(() => {
  return Array.from({ length: 6 }).map((_, index) => {
    const date = dayjs().subtract(index, 'month')
    return {
      value: date.format('YYYY-MM'),
      label: date.format('YYYY年MM月')
    }
  })
})

const availableYears = computed(() => {
  const currentYear = dayjs().year()
  return [currentYear, currentYear - 1, currentYear - 2]
})

const monthRemainDays = computed(() => {
  const current = dayjs(selectedMonth.value)
  const today = dayjs()
  if (!current.isSame(today, 'month')) return current.daysInMonth()
  return Math.max(today.endOf('month').diff(today, 'day'), 0)
})

const budgetProgress = computed(() => {
  return Math.min(Number(dashboardData.budgetUsage || 0), 100)
})

const budgetStatusText = computed(() => {
  if (dashboardData.budgetExceeded) return '预算已经超支，建议优先查看支出排行并控制非必要消费。'
  if (Number(dashboardData.budgetUsage || 0) >= 80) return '预算使用接近上限，接下来几天可以稍微收紧一点。'
  return '预算状态健康，继续保持清晰记账就很稳。'
})

const dashboardAdvice = computed(() => buildDashboardAdvice(dashboardData))

const formatNumber = (value) => {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const formatDate = (value) => dayjs(value).format('YYYY-MM-DD')

const goBills = () => window.dispatchEvent(new CustomEvent('open-quick-bill'))
const goStatistics = () => router.push('/statistics')
const goExport = () => router.push({ path: '/settings', query: { section: 'data' } })
const goBudgetSettings = () => router.push({ path: '/settings', query: { section: 'profile', focus: 'budget' } })
const goBillsList = (type = '') => {
  router.push({
    path: '/bills',
    query: {
      type,
      month: selectedMonth.value
    }
  })
}

const normalizeMonthlyStatistics = (statistics, year) => {
  const sourceMap = new Map(statistics.map((item) => [item.month, item]))
  return Array.from({ length: 12 }).map((_, index) => {
    const month = `${year}-${String(index + 1).padStart(2, '0')}`
    const item = sourceMap.get(month)
    return {
      month,
      income: Number(item?.income || 0),
      expense: Number(item?.expense || 0)
    }
  })
}

const ensureChartsReady = async () => {
  await nextTick()
  await new Promise((resolve) => requestAnimationFrame(resolve))

  if (pieChartRef.value && !pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }

  if (barChartRef.value && !barChart) {
    barChart = echarts.init(barChartRef.value)
  }
}

const renderPieChart = async () => {
  await ensureChartsReady()
  if (!pieChart) return

  const data = dashboardData.categoryStatistics.map((item) => ({
    value: Number(item.amount || 0),
    name: formatCategoryName(item.categoryName),
    itemStyle: {
      color: item.categoryColor || '#409EFF'
    }
  }))

  pieChart.clear()
  pieChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}<br/>金额：¥{c}<br/>占比：{d}%'
    },
    legend: {
      bottom: 0
    },
    series: [
      {
        name: '支出分类',
        type: 'pie',
        radius: ['38%', '68%'],
        center: ['50%', '42%'],
        avoidLabelOverlap: true,
        label: {
          show: true,
          formatter: '{b}'
        },
        data: data.length ? data : [{ value: 1, name: '暂无数据', itemStyle: { color: '#dbeafe' } }]
      }
    ]
  }, true)
  pieChart.resize()
}

const renderBarChart = async () => {
  await ensureChartsReady()
  if (!barChart) return

  const monthlyData = normalizeMonthlyStatistics(dashboardData.monthlyStatistics, selectedYear.value)

  barChart.clear()
  barChart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      top: 0,
      data: ['收入', '支出']
    },
    grid: {
      left: 18,
      right: 18,
      bottom: 18,
      top: 42,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: monthlyData.map((item) => item.month.slice(5))
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '收入',
        type: 'bar',
        barMaxWidth: 22,
        itemStyle: {
          color: '#22c55e',
          borderRadius: [8, 8, 0, 0]
        },
        data: monthlyData.map((item) => item.income)
      },
      {
        name: '支出',
        type: 'bar',
        barMaxWidth: 22,
        itemStyle: {
          color: '#f97316',
          borderRadius: [8, 8, 0, 0]
        },
        data: monthlyData.map((item) => item.expense)
      }
    ]
  }, true)
  barChart.resize()
}

const renderCharts = async () => {
  await Promise.all([renderPieChart(), renderBarChart()])
}

const handleResize = () => {
  pieChart?.resize()
  barChart?.resize()
}

const handleBillCreated = () => {
  loadDashboardData()
}

const loadDashboardData = async () => {
  loading.value = true
  try {
    const [dashboardResponse, billsResponse] = await Promise.all([
      api.get('/statistics/dashboard', {
        params: {
          month: selectedMonth.value,
          year: selectedYear.value
        }
      }),
      api.get('/bills/recent', {
        params: { limit: 5 }
      })
    ])

    if (dashboardResponse.data.code === 200) {
      Object.assign(dashboardData, dashboardResponse.data.data)
    }

    if (billsResponse.data.code === 200) {
      recentBills.value = billsResponse.data.data
    }

    await renderCharts()
  } catch (error) {
    console.error('加载仪表板失败:', error)
    ElMessage.error('加载仪表板失败')
  } finally {
    loading.value = false
  }
}

watch(
  () => [dashboardData.categoryStatistics, dashboardData.monthlyStatistics],
  () => {
    renderCharts()
  },
  { deep: true }
)

onMounted(async () => {
  await ensureChartsReady()
  await loadDashboardData()
  setTimeout(handleResize, 120)
  window.addEventListener('resize', handleResize)
  window.addEventListener('bill-created', handleBillCreated)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('bill-created', handleBillCreated)
  pieChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.page-head h2 {
  margin: 0;
  font-size: 28px;
}

.page-head p {
  margin-top: 6px;
  color: #64748b;
}

.hero-card {
  overflow: hidden;
  border: none;
  border-radius: 26px;
  color: #fff;
  background:
    radial-gradient(circle at 78% 10%, rgba(255, 255, 255, 0.28), transparent 18%),
    linear-gradient(135deg, #0f766e 0%, #14b8a6 48%, #f97316 100%);
}

.hero-card :deep(.el-card__body) {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
  align-items: center;
}

.hero-copy h3 {
  margin: 8px 0;
  font-size: 28px;
}

.hero-copy p,
.eyebrow {
  color: rgba(255, 255, 255, 0.82);
}

.eyebrow {
  font-size: 13px;
  letter-spacing: 0.16em;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.hero-card :deep(.el-progress) {
  grid-column: 1 / -1;
}

.hero-card :deep(.el-progress-bar__outer) {
  background: rgba(255, 255, 255, 0.24);
}

.hero-card :deep(.el-progress__text) {
  color: #fff;
}

.filters {
  display: flex;
  gap: 10px;
}

.stat-card {
  position: relative;
  overflow: hidden;
  border: none;
  border-radius: 20px;
}

.clickable-card {
  cursor: pointer;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease;
}

.clickable-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 18px 38px rgba(15, 23, 42, 0.13);
}

.stat-icon {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 38px;
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  color: #fff;
  font-weight: 700;
  background: rgba(15, 23, 42, 0.20);
}

.stat-card p {
  color: #64748b;
  margin-bottom: 10px;
}

.stat-card strong {
  font-size: 28px;
}

.stat-card small {
  display: block;
  margin-top: 8px;
  color: #64748b;
}

.stat-card.income {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.12), rgba(240, 253, 244, 0.95));
}

.stat-card.expense {
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.14), rgba(255, 247, 237, 0.96));
}

.stat-card.balance {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.14), rgba(239, 246, 255, 0.96));
}

.stat-card.budget {
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.13), rgba(240, 253, 250, 0.96));
}

.budget-alert,
.panel-card {
  border-radius: 20px;
}

.advice-card {
  border: none;
}

.advice-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.advice-item {
  min-height: 112px;
  padding: 16px;
  border-radius: 18px;
  background:
    linear-gradient(135deg, rgba(15, 118, 110, 0.10), rgba(255, 247, 237, 0.92));
}

.advice-item strong {
  display: block;
  margin-bottom: 8px;
  color: #0f172a;
}

.advice-item p {
  margin: 0;
  color: #64748b;
  line-height: 1.7;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.panel-header a {
  color: #0f766e;
  text-decoration: none;
}

.subtle {
  color: #64748b;
  font-size: 13px;
}

.chart-shell {
  position: relative;
  min-height: 340px;
}

.chart-box {
  width: 100%;
  height: 340px;
}

.chart-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 14px;
  pointer-events: none;
  z-index: 0;
}

.recent-card {
  margin-top: 2px;
}

.income-text {
  color: #16a34a;
  font-weight: 600;
}

.expense-text {
  color: #ea580c;
  font-weight: 600;
}

@media (max-width: 900px) {
  .page-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .filters {
    width: 100%;
    flex-wrap: wrap;
  }

  .hero-card :deep(.el-card__body) {
    grid-template-columns: 1fr;
  }

  .hero-actions {
    justify-content: flex-start;
  }

  .advice-grid {
    grid-template-columns: 1fr;
  }
}
</style>

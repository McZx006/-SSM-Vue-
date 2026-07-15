<template>
  <div class="statistics-page">
    <div class="page-head">
      <div>
        <h2>统计分析</h2>
        <p>查看分类占比、分类排行和全年月度趋势。</p>
      </div>
    </div>

    <el-row :gutter="18">
      <el-col :xs="24" :xl="8">
        <el-card class="panel-card filter-card">
          <div class="filter-grid">
            <el-form-item label="统计口径">
              <el-radio-group v-model="selectedType" @change="loadStatistics">
                <el-radio value="expense">支出</el-radio>
                <el-radio value="income">收入</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="分类月份">
              <el-date-picker
                v-model="selectedMonth"
                type="month"
                value-format="YYYY-MM"
                @change="loadStatistics"
              />
            </el-form-item>
            <el-form-item label="趋势年份">
              <el-date-picker
                v-model="selectedYear"
                type="year"
                value-format="YYYY"
                @change="loadStatistics"
              />
            </el-form-item>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8" :xl="5">
        <el-card class="panel-card stat-card">
          <p>总金额</p>
          <strong>¥{{ formatNumber(statistics.total) }}</strong>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8" :xl="5">
        <el-card class="panel-card stat-card">
          <p>分类数量</p>
          <strong>{{ statistics.count }}</strong>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8" :xl="6">
        <el-card class="panel-card stat-card">
          <p>平均金额</p>
          <strong>¥{{ formatNumber(statistics.average) }}</strong>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card insight-card">
      <div class="insight-item">
        <span>最高分类</span>
        <strong>{{ topCategory ? formatCategoryName(topCategory.categoryName) : '暂无数据' }}</strong>
        <p>{{ topCategory ? `占本月${selectedType === 'expense' ? '支出' : '收入'} ${Number(topCategory.percentage || 0).toFixed(1)}%` : '添加几条账单后会自动生成结论。' }}</p>
      </div>
      <div class="insight-item">
        <span>年度支出峰值</span>
        <strong>{{ peakExpenseMonth.month || '暂无数据' }}</strong>
        <p>该月支出 ¥{{ formatNumber(peakExpenseMonth.expense) }}</p>
      </div>
      <div class="insight-item">
        <span>结余表现</span>
        <strong>{{ yearlyBalance >= 0 ? '整体为正' : '需要关注' }}</strong>
        <p>全年累计结余 ¥{{ formatNumber(yearlyBalance) }}</p>
      </div>
    </el-card>

    <el-card class="panel-card ai-advice-card">
      <template #header>
        <div class="panel-header">
          <span>智能分析建议</span>
          <span class="subtle">根据分类和年度趋势生成</span>
        </div>
      </template>
      <div class="ai-advice-list">
        <div v-for="item in trendAdvice" :key="item.title" class="ai-advice-item">
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
              <span>分类占比</span>
              <span class="subtle">{{ selectedMonth }}</span>
            </div>
          </template>
          <div ref="pieChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="12">
        <el-card class="panel-card">
          <template #header>
            <div class="panel-header">
              <span>分类排行</span>
              <span class="subtle">{{ selectedType === 'expense' ? '支出' : '收入' }}</span>
            </div>
          </template>
          <div ref="barChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card">
      <template #header>
        <div class="panel-header">
          <span>月度趋势</span>
          <span class="subtle">{{ selectedYear }}年</span>
        </div>
      </template>
      <div ref="lineChartRef" class="chart-box large"></div>
    </el-card>

    <el-card class="panel-card">
      <template #header>
        <span>分类明细</span>
      </template>
      <el-table :data="categoryStatistics" stripe>
        <el-table-column prop="categoryName" label="分类" min-width="160">
          <template #default="{ row }">
            <div class="category-cell">
              <span class="category-dot" :style="{ backgroundColor: row.categoryColor || '#409EFF' }"></span>
              <span>{{ formatCategoryName(row.categoryName) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="160" align="right">
          <template #default="{ row }">¥{{ formatNumber(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="percentage" label="占比" width="120" align="right">
          <template #default="{ row }">{{ Number(row.percentage || 0).toFixed(2) }}%</template>
        </el-table-column>
        <el-table-column label="进度">
          <template #default="{ row }">
            <el-progress :percentage="Number(row.percentage || 0)" :color="row.categoryColor || '#409EFF'" />
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!categoryStatistics.length" description="暂无分类统计数据，请先添加账单或切换月份。" />
    </el-card>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import api from '@/utils/api'
import { buildTrendAdvice } from '@/utils/advice'
import { formatCategoryName } from '@/utils/category'

const pieChartRef = ref(null)
const barChartRef = ref(null)
const lineChartRef = ref(null)

const selectedMonth = ref(dayjs().format('YYYY-MM'))
const selectedYear = ref(dayjs().format('YYYY'))
const selectedType = ref('expense')

const statistics = reactive({
  total: 0,
  count: 0,
  average: 0
})

const categoryStatistics = ref([])
const monthlyStatistics = ref([])

const topCategory = computed(() => categoryStatistics.value[0] || null)
const fullMonthlyData = computed(() => normalizeMonthlyStatistics(monthlyStatistics.value, selectedYear.value))
const peakExpenseMonth = computed(() => {
  return fullMonthlyData.value.reduce(
    (max, item) => (Number(item.expense || 0) > Number(max.expense || 0) ? item : max),
    { month: '', expense: 0 }
  )
})
const yearlyBalance = computed(() => {
  return fullMonthlyData.value.reduce((sum, item) => sum + Number(item.balance || 0), 0)
})
const trendAdvice = computed(() => buildTrendAdvice(categoryStatistics.value, monthlyStatistics.value))

let pieChart = null
let barChart = null
let lineChart = null

const formatNumber = (value) => {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const normalizeMonthlyStatistics = (statisticsList, year) => {
  const sourceMap = new Map(statisticsList.map((item) => [item.month, item]))
  return Array.from({ length: 12 }).map((_, index) => {
    const month = `${year}-${String(index + 1).padStart(2, '0')}`
    const item = sourceMap.get(month)
    return {
      month,
      income: Number(item?.income || 0),
      expense: Number(item?.expense || 0),
      balance: Number(item?.balance || 0)
    }
  })
}

const syncSummary = () => {
  const total = categoryStatistics.value.reduce((sum, item) => sum + Number(item.amount || 0), 0)
  statistics.total = total
  statistics.count = categoryStatistics.value.length
  statistics.average = statistics.count ? total / statistics.count : 0
}

const renderPieChart = () => {
  if (!pieChartRef.value) return
  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }

  const data = categoryStatistics.value.map((item) => ({
    value: Number(item.amount || 0),
    name: formatCategoryName(item.categoryName),
    itemStyle: {
      color: item.categoryColor || '#409EFF'
    }
  }))

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
        type: 'pie',
        radius: ['38%', '68%'],
        center: ['50%', '45%'],
        data: data.length ? data : [{ value: 1, name: '暂无数据', itemStyle: { color: '#dbeafe' } }]
      }
    ]
  })
}

const renderBarChart = () => {
  if (!barChartRef.value) return
  if (!barChart) {
    barChart = echarts.init(barChartRef.value)
  }

  const reversed = [...categoryStatistics.value].reverse()

  barChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 18, right: 18, top: 12, bottom: 18, containLabel: true },
    xAxis: { type: 'value' },
    yAxis: {
      type: 'category',
      data: reversed.map((item) => formatCategoryName(item.categoryName))
    },
    series: [
      {
        type: 'bar',
        barMaxWidth: 24,
        data: reversed.map((item) => ({
          value: Number(item.amount || 0),
          itemStyle: {
            color: item.categoryColor || '#409EFF',
            borderRadius: [0, 8, 8, 0]
          }
        }))
      }
    ]
  })
}

const renderLineChart = () => {
  if (!lineChartRef.value) return
  if (!lineChart) {
    lineChart = echarts.init(lineChartRef.value)
  }

  const fullMonths = normalizeMonthlyStatistics(monthlyStatistics.value, selectedYear.value)

  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: {
      top: 0,
      data: ['收入', '支出', '结余']
    },
    grid: { left: 18, right: 18, top: 42, bottom: 18, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: fullMonths.map((item) => item.month.slice(5))
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '收入',
        type: 'line',
        smooth: true,
        data: fullMonths.map((item) => item.income),
        itemStyle: { color: '#16a34a' },
        areaStyle: { color: 'rgba(22, 163, 74, 0.10)' }
      },
      {
        name: '支出',
        type: 'line',
        smooth: true,
        data: fullMonths.map((item) => item.expense),
        itemStyle: { color: '#ea580c' },
        areaStyle: { color: 'rgba(234, 88, 12, 0.10)' }
      },
      {
        name: '结余',
        type: 'line',
        smooth: true,
        data: fullMonths.map((item) => item.balance),
        itemStyle: { color: '#2563eb' }
      }
    ]
  })
}

const renderCharts = async () => {
  await nextTick()
  renderPieChart()
  renderBarChart()
  renderLineChart()
}

const handleResize = () => {
  pieChart?.resize()
  barChart?.resize()
  lineChart?.resize()
}

const handleBillCreated = () => {
  loadStatistics()
}

const loadStatistics = async () => {
  try {
    const [categoryResponse, monthlyResponse] = await Promise.all([
      api.get('/statistics/category', {
        params: {
          month: selectedMonth.value,
          type: selectedType.value
        }
      }),
      api.get('/statistics/monthly', {
        params: {
          year: selectedYear.value
        }
      })
    ])

    if (categoryResponse.data.code === 200) {
      categoryStatistics.value = categoryResponse.data.data
      syncSummary()
    }

    if (monthlyResponse.data.code === 200) {
      monthlyStatistics.value = monthlyResponse.data.data
    }

    await renderCharts()
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

onMounted(() => {
  loadStatistics()
  window.addEventListener('resize', handleResize)
  window.addEventListener('bill-created', handleBillCreated)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('bill-created', handleBillCreated)
  pieChart?.dispose()
  barChart?.dispose()
  lineChart?.dispose()
})
</script>

<style scoped>
.statistics-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page-head h2 {
  margin: 0;
  font-size: 28px;
}

.page-head p {
  margin-top: 6px;
  color: #64748b;
}

.panel-card {
  border-radius: 20px;
}

.insight-card :deep(.el-card__body) {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.insight-item {
  padding: 16px;
  border-radius: 18px;
  background: linear-gradient(135deg, #f8fafc, #eefdf8);
}

.insight-item span,
.insight-item strong,
.insight-item p {
  display: block;
}

.insight-item span {
  color: #64748b;
  font-size: 13px;
}

.insight-item strong {
  margin: 8px 0;
  font-size: 22px;
}

.insight-item p {
  margin: 0;
  color: #64748b;
}

.ai-advice-list {
  display: grid;
  gap: 12px;
}

.ai-advice-item {
  padding: 14px 16px;
  border: 1px solid rgba(15, 118, 110, 0.12);
  border-radius: 16px;
  background: #f8fffb;
}

.ai-advice-item strong {
  display: block;
  margin-bottom: 6px;
  color: #0f766e;
}

.ai-advice-item p {
  margin: 0;
  color: #64748b;
  line-height: 1.7;
}

.filter-card {
  height: 100%;
}

.filter-grid {
  display: grid;
  gap: 10px;
}

.stat-card p {
  color: #64748b;
  margin-bottom: 12px;
}

.stat-card strong {
  font-size: 28px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.subtle {
  color: #64748b;
  font-size: 13px;
}

.chart-box {
  height: 330px;
}

.chart-box.large {
  height: 420px;
}

.category-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.category-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

@media (max-width: 900px) {
  .insight-card :deep(.el-card__body) {
    grid-template-columns: 1fr;
  }
}
</style>

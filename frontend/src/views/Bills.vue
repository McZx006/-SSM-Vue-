<template>
  <div class="page-wrap">
    <div class="page-head">
      <div>
        <h2>账单管理</h2>
        <p>支持账单的新增、筛选、编辑和删除。</p>
      </div>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        添加账单
      </el-button>
    </div>

    <el-card class="panel-card">
      <div class="quick-filter-row">
        <el-button round @click="applyQuickFilter('month')">本月账单</el-button>
        <el-button round @click="applyQuickFilter('expense')">只看支出</el-button>
        <el-button round @click="applyQuickFilter('income')">只看收入</el-button>
        <el-button round @click="applyQuickFilter('clear')">全部记录</el-button>
      </div>

      <el-form :model="filterForm" inline>
        <el-form-item label="类型">
          <el-select v-model="filterForm.type" placeholder="全部" clearable>
            <el-option label="收入" value="income" />
            <el-option label="支出" value="expense" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filterForm.categoryId" placeholder="全部" clearable>
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="formatCategoryName(category.name)"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filterForm.keyword" placeholder="搜索备注或分类" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="panel-card">
      <div class="table-summary">
        <div>
          <span>当前页收入</span>
          <strong class="income-text">¥{{ formatNumber(pageIncome) }}</strong>
        </div>
        <div>
          <span>当前页支出</span>
          <strong class="expense-text">¥{{ formatNumber(pageExpense) }}</strong>
        </div>
        <div>
          <span>筛选结果</span>
          <strong>{{ pagination.total }} 条</strong>
        </div>
      </div>

      <el-table :data="bills" stripe v-loading="loading">
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
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            {{ row.type === 'income' ? '收入' : '支出' }}
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
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !bills.length" description="暂时没有符合条件的账单，换个筛选条件试试。" />

      <div class="pagination-box">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadBills"
          @current-change="loadBills"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑账单' : '添加账单'" width="520px">
      <el-form ref="billFormRef" :model="billForm" :rules="rules" label-width="80px">
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="billForm.type">
            <el-radio value="income">收入</el-radio>
            <el-radio value="expense">支出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number
            v-model="billForm.amount"
            :min="0.01"
            :precision="2"
            :step="10"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="billForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="category in filteredCategories"
              :key="category.id"
              :label="formatCategoryName(category.name)"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="date">
          <el-date-picker
            v-model="billForm.date"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="billForm.remark" type="textarea" :rows="3" placeholder="可填写用途说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '立即添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import api from '@/utils/api'
import { formatCategoryName } from '@/utils/category'

const loading = ref(false)
const route = useRoute()
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const billFormRef = ref(null)

const bills = ref([])
const categories = ref([])
const dateRange = ref([])

const filterForm = reactive({
  type: '',
  categoryId: null,
  startDate: '',
  endDate: '',
  keyword: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const billForm = reactive({
  id: null,
  type: 'expense',
  amount: null,
  categoryId: null,
  date: dayjs().format('YYYY-MM-DD'),
  remark: ''
})

const rules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于 0', trigger: 'blur' }
  ],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  date: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

const filteredCategories = computed(() => {
  return categories.value.filter((item) => item.type === billForm.type)
})

const pageIncome = computed(() => {
  return bills.value
    .filter((item) => item.type === 'income')
    .reduce((sum, item) => sum + Number(item.amount || 0), 0)
})

const pageExpense = computed(() => {
  return bills.value
    .filter((item) => item.type === 'expense')
    .reduce((sum, item) => sum + Number(item.amount || 0), 0)
})

const formatDate = (value) => dayjs(value).format('YYYY-MM-DD')

const formatNumber = (value) => {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const loadCategories = async () => {
  const response = await api.get('/categories')
  if (response.data.code === 200) {
    categories.value = response.data.data
  }
}

const loadBills = async () => {
  loading.value = true
  try {
    const response = await api.get('/bills', {
      params: {
        ...filterForm,
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize
      }
    })

    if (response.data.code === 200) {
      bills.value = response.data.data.list
      pagination.total = response.data.data.total
    }
  } catch (error) {
    console.error('加载账单失败:', error)
    ElMessage.error('加载账单失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  filterForm.startDate = dateRange.value?.[0] || ''
  filterForm.endDate = dateRange.value?.[1] || ''
  pagination.pageNum = 1
  loadBills()
}

const resetFilter = () => {
  Object.assign(filterForm, {
    type: '',
    categoryId: null,
    startDate: '',
    endDate: '',
    keyword: ''
  })
  dateRange.value = []
  pagination.pageNum = 1
  loadBills()
}

const applyQuickFilter = (type) => {
  if (type === 'month') {
    dateRange.value = [
      dayjs().startOf('month').format('YYYY-MM-DD'),
      dayjs().endOf('month').format('YYYY-MM-DD')
    ]
    filterForm.type = ''
  } else if (type === 'expense' || type === 'income') {
    filterForm.type = type
  } else {
    resetFilter()
    return
  }

  handleSearch()
}

const applyRouteQuery = () => {
  const queryType = route.query.type
  const queryMonth = route.query.month

  if (queryType === 'income' || queryType === 'expense') {
    filterForm.type = queryType
  }

  if (typeof queryMonth === 'string' && /^\d{4}-\d{2}$/.test(queryMonth)) {
    const month = dayjs(queryMonth)
    dateRange.value = [
      month.startOf('month').format('YYYY-MM-DD'),
      month.endOf('month').format('YYYY-MM-DD')
    ]
    filterForm.startDate = dateRange.value[0]
    filterForm.endDate = dateRange.value[1]
  }
}

const showAddDialog = () => {
  isEdit.value = false
  Object.assign(billForm, {
    id: null,
    type: 'expense',
    amount: null,
    categoryId: null,
    date: dayjs().format('YYYY-MM-DD'),
    remark: ''
  })
  dialogVisible.value = true
}

const showEditDialog = (bill) => {
  isEdit.value = true
  Object.assign(billForm, {
    id: bill.id,
    type: bill.type,
    amount: Number(bill.amount),
    categoryId: bill.categoryId,
    date: dayjs(bill.date).format('YYYY-MM-DD'),
    remark: bill.remark || ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!billFormRef.value) return

  await billFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const url = isEdit.value ? `/bills/${billForm.id}` : '/bills'
      const method = isEdit.value ? 'put' : 'post'
      const response = await api[method](url, billForm)

      if (response.data.code === 200) {
        ElMessage.success(isEdit.value ? '账单更新成功' : '账单添加成功')
        dialogVisible.value = false
        loadBills()
      } else {
        ElMessage.error(response.data.message || '操作失败')
      }
    } catch (error) {
      console.error('保存账单失败:', error)
      ElMessage.error('保存账单失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (bill) => {
  try {
    await ElMessageBox.confirm(`确认删除“${bill.remark || formatCategoryName(bill.category?.name) || '该账单'}”吗？`, '删除提示', {
      type: 'warning'
    })

    const response = await api.delete(`/bills/${bill.id}`)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      loadBills()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除账单失败:', error)
      ElMessage.error('删除账单失败')
    }
  }
}

const handleBillCreated = () => {
  pagination.pageNum = 1
  loadBills()
}

onMounted(async () => {
  try {
    await loadCategories()
    applyRouteQuery()
    await loadBills()
    window.addEventListener('bill-created', handleBillCreated)
  } catch (error) {
    console.error('初始化账单页失败:', error)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('bill-created', handleBillCreated)
})

watch(
  () => route.query,
  () => {
    applyRouteQuery()
    pagination.pageNum = 1
    loadBills()
  }
)
</script>

<style scoped>
.page-wrap {
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

.panel-card {
  border-radius: 20px;
}

.quick-filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.table-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.table-summary > div {
  padding: 14px;
  border-radius: 16px;
  background: #f8fafc;
}

.table-summary span,
.table-summary strong {
  display: block;
}

.table-summary span {
  margin-bottom: 6px;
  color: #64748b;
  font-size: 13px;
}

.pagination-box {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
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

  .table-summary {
    grid-template-columns: 1fr;
  }
}
</style>

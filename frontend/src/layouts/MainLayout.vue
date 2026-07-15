<template>
  <div class="layout-shell">
    <el-container>
      <el-header class="layout-header">
        <div class="brand">
          <div class="brand-mark">¥</div>
          <div>
            <h1>个人记账看板</h1>
            <p>收支趋势与分类分析</p>
          </div>
        </div>

        <el-dropdown @command="handleCommand">
          <div class="user-trigger">
            <el-avatar :size="36" :src="userStore.user?.avatar">
              {{ userStore.user?.username?.slice(0, 1)?.toUpperCase() }}
            </el-avatar>
            <div class="user-meta">
              <strong>{{ userStore.user?.username || '用户' }}</strong>
              <span>{{ userStore.user?.email || '欢迎回来' }}</span>
            </div>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="settings">系统设置</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-container>
        <el-aside width="220px" class="layout-aside">
          <el-menu :default-active="activeMenu" router class="layout-menu">
            <el-menu-item index="/">
              <el-icon><Odometer /></el-icon>
              <span>仪表板</span>
            </el-menu-item>
            <el-menu-item index="/bills">
              <el-icon><List /></el-icon>
              <span>账单管理</span>
            </el-menu-item>
            <el-menu-item index="/statistics">
              <el-icon><DataAnalysis /></el-icon>
              <span>统计分析</span>
            </el-menu-item>
            <el-menu-item index="/settings">
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-main class="layout-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>

    <button class="quick-add-button" type="button" @click="openQuickBill">
      <el-icon><Plus /></el-icon>
      <span>记一笔</span>
    </button>

    <nav class="mobile-tabbar">
      <router-link v-for="item in navItems" :key="item.path" :to="item.path" class="tabbar-item">
        <el-icon>
          <component :is="item.icon" />
        </el-icon>
        <span>{{ item.label }}</span>
      </router-link>
    </nav>

    <el-dialog v-model="quickBillVisible" title="快速记一笔" width="520px" class="quick-bill-dialog">
      <el-form ref="quickBillFormRef" :model="quickBillForm" :rules="quickBillRules" label-position="top">
        <div class="quick-template-row">
          <button
            v-for="template in quickTemplates"
            :key="template.name"
            type="button"
            class="quick-template"
            @click="applyQuickTemplate(template)"
          >
            <span>{{ template.name }}</span>
            <small>¥{{ template.amount }}</small>
          </button>
        </div>

        <el-form-item label="收支类型" prop="type">
          <el-radio-group v-model="quickBillForm.type" @change="handleTypeChange">
            <el-radio-button value="expense">支出</el-radio-button>
            <el-radio-button value="income">收入</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="金额" prop="amount">
          <el-input-number
            v-model="quickBillForm.amount"
            :min="0.01"
            :precision="2"
            :step="10"
            style="width: 100%"
            placeholder="请输入金额"
          />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select
            v-model="quickBillForm.categoryId"
            placeholder="请选择分类"
            empty-text="暂无分类，可在下方新建"
            style="width: 100%"
          >
            <el-option
              v-for="category in quickCategories"
              :key="category.id"
              :label="formatCategoryName(category.name)"
              :value="category.id"
            />
          </el-select>

          <div v-if="!showQuickCategoryCreator" class="category-create-toggle">
            <el-button plain @click="showQuickCategoryCreator = true">
              <el-icon><Plus /></el-icon>
              添加分类
            </el-button>
          </div>

          <div v-else class="inline-category-create">
            <el-input
              v-model.trim="quickCategoryForm.name"
              clearable
              maxlength="12"
              placeholder="没有合适分类？输入名称后添加"
              @keyup.enter="createQuickCategory"
            />
            <el-color-picker v-model="quickCategoryForm.color" />
            <el-button :loading="creatingCategory" @click="createQuickCategory">添加分类</el-button>
            <el-button text @click="hideQuickCategoryCreator">收起</el-button>
          </div>
        </el-form-item>

        <el-form-item label="日期" prop="date">
          <el-date-picker
            v-model="quickBillForm.date"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="quickBillForm.remark" placeholder="例如：午餐、兼职、教材" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="quickBillVisible = false">取消</el-button>
        <el-button type="primary" :loading="quickSubmitting" @click="submitQuickBill">保存账单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores/user'
import api from '@/utils/api'
import { formatCategoryName, normalizeCategoryName } from '@/utils/category'
import {
  ArrowDown,
  DataAnalysis,
  List,
  Odometer,
  Plus,
  Setting
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const quickBillVisible = ref(false)
const quickSubmitting = ref(false)
const creatingCategory = ref(false)
const showQuickCategoryCreator = ref(false)
const quickBillFormRef = ref(null)
const categories = ref([])

const quickBillForm = reactive({
  type: 'expense',
  amount: null,
  categoryId: null,
  date: dayjs().format('YYYY-MM-DD'),
  remark: ''
})

const quickCategoryForm = reactive({
  name: '',
  color: '#0f766e'
})

const quickBillRules = {
  type: [{ required: true, message: '请选择收支类型', trigger: 'change' }],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于 0', trigger: 'blur' }
  ],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  date: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

const quickTemplates = [
  { name: '早餐', type: 'expense', category: '餐饮', amount: 12, remark: '早餐' },
  { name: '通勤', type: 'expense', category: '交通', amount: 8, remark: '地铁/公交' },
  { name: '教材', type: 'expense', category: '学习', amount: 45, remark: '学习资料' },
  { name: '兼职', type: 'income', category: '兼职', amount: 100, remark: '兼职收入' }
]

const activeMenu = computed(() => route.path)
const quickCategories = computed(() => {
  return dedupeCategories(categories.value).filter((item) => item.type === quickBillForm.type)
})

const navItems = [
  { path: '/', label: '首页', icon: Odometer },
  { path: '/bills', label: '账单', icon: List },
  { path: '/statistics', label: '分析', icon: DataAnalysis },
  { path: '/settings', label: '我的', icon: Setting }
]

const dedupeCategories = (source = []) => {
  const map = new Map()

  source.forEach((category) => {
    if (!category?.id || !category?.type) return

    const key = `${category.type}:${normalizeCategoryName(category.name)}`
    const existing = map.get(key)

    if (!existing || shouldPreferCategory(category, existing)) {
      map.set(key, category)
    }
  })

  return [...map.values()].sort((a, b) => {
    if (a.type !== b.type) return a.type.localeCompare(b.type)
    return (a.sortOrder ?? 99) - (b.sortOrder ?? 99) || a.id - b.id
  })
}

const shouldPreferCategory = (candidate, existing) => {
  if (existing.userId == null && candidate.userId != null) return true
  return (candidate.sortOrder ?? 99) < (existing.sortOrder ?? 99)
}

const resetQuickBillForm = () => {
  Object.assign(quickBillForm, {
    type: 'expense',
    amount: null,
    categoryId: null,
    date: dayjs().format('YYYY-MM-DD'),
    remark: ''
  })
  resetQuickCategoryForm()
}

const resetQuickCategoryForm = () => {
  Object.assign(quickCategoryForm, {
    name: '',
    color: quickBillForm.type === 'income' ? '#22c55e' : '#0f766e'
  })
}

const hideQuickCategoryCreator = () => {
  showQuickCategoryCreator.value = false
  resetQuickCategoryForm()
}

const handleTypeChange = () => {
  quickBillForm.categoryId = null
  showQuickCategoryCreator.value = false
  resetQuickCategoryForm()
}

const findCategoryByName = (type, name) => {
  const targetName = normalizeCategoryName(name)
  return quickCategories.value.find((category) => {
    return category.type === type && normalizeCategoryName(category.name) === targetName
  })
}

const applyQuickTemplate = async (template) => {
  if (!categories.value.length) {
    await loadCategories()
  }

  Object.assign(quickBillForm, {
    type: template.type,
    amount: template.amount,
    categoryId: null,
    date: dayjs().format('YYYY-MM-DD'),
    remark: template.remark
  })

  resetQuickCategoryForm()
  const matchedCategory = findCategoryByName(template.type, template.category)
  quickBillForm.categoryId = matchedCategory?.id || null
}

const loadCategories = async () => {
  try {
    const response = await api.get('/categories')
    if (response.data.code === 200) {
      categories.value = dedupeCategories(response.data.data || [])
    }
  } catch (error) {
    console.error('加载快捷分类失败:', error)
    ElMessage.error('加载分类失败')
  }
}

const createQuickCategory = async () => {
  const name = quickCategoryForm.name.trim()
  if (!name) {
    ElMessage.warning('请输入分类名称')
    return
  }

  const existing = findCategoryByName(quickBillForm.type, name)
  if (existing) {
    quickBillForm.categoryId = existing.id
    quickCategoryForm.name = ''
    showQuickCategoryCreator.value = false
    ElMessage.info('分类已存在，已自动选中')
    return
  }

  creatingCategory.value = true
  try {
    const response = await api.post('/categories', {
      name,
      type: quickBillForm.type,
      icon: '',
      color: quickCategoryForm.color,
      sortOrder: 99
    })

    if (response.data.code !== 200) {
      ElMessage.error(response.data.message || '添加分类失败')
      return
    }

    await loadCategories()
    const saved = response.data.data
    const matched = saved?.id ? categories.value.find((item) => item.id === saved.id) : findCategoryByName(quickBillForm.type, name)
    quickBillForm.categoryId = matched?.id || saved?.id || null
    quickCategoryForm.name = ''
    showQuickCategoryCreator.value = false
    ElMessage.success('分类已添加')
  } catch (error) {
    console.error('添加快捷分类失败:', error)
    ElMessage.error('添加分类失败')
  } finally {
    creatingCategory.value = false
  }
}

const openQuickBill = async () => {
  resetQuickBillForm()
  showQuickCategoryCreator.value = false
  if (!categories.value.length) {
    await loadCategories()
  }
  quickBillVisible.value = true
}

const submitQuickBill = async () => {
  if (!quickBillFormRef.value) return

  await quickBillFormRef.value.validate(async (valid) => {
    if (!valid) return

    quickSubmitting.value = true
    try {
      const response = await api.post('/bills', {
        type: quickBillForm.type,
        amount: quickBillForm.amount,
        categoryId: quickBillForm.categoryId,
        date: quickBillForm.date,
        remark: quickBillForm.remark
      })

      if (response.data.code === 200) {
        ElMessage.success('账单已保存')
        quickBillVisible.value = false
        window.dispatchEvent(new CustomEvent('bill-created'))
      } else {
        ElMessage.error(response.data.message || '保存账单失败')
      }
    } catch (error) {
      console.error('快捷记账失败:', error)
      ElMessage.error('保存账单失败')
    } finally {
      quickSubmitting.value = false
    }
  })
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    await userStore.logout()
    router.push('/login')
    return
  }

  if (command === 'settings') {
    router.push('/settings')
  }
}

onMounted(() => {
  loadCategories()
  window.addEventListener('open-quick-bill', openQuickBill)
})

onBeforeUnmount(() => {
  window.removeEventListener('open-quick-bill', openQuickBill)
})
</script>

<style scoped>
.layout-shell {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(20, 184, 166, 0.12), transparent 24%),
    radial-gradient(circle at right 18%, rgba(251, 146, 60, 0.10), transparent 18%),
    linear-gradient(180deg, var(--app-bg) 0%, var(--app-bg) 100%);
}

.layout-header {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: color-mix(in srgb, var(--app-card) 92%, transparent);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--app-border);
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-mark {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f766e 0%, #22c55e 100%);
  color: #fff;
  font-size: 22px;
  font-weight: 700;
}

.brand h1 {
  font-size: 22px;
  margin: 0;
}

.brand p {
  margin: 2px 0 0;
  color: var(--app-muted);
  font-size: 13px;
}

.layout-aside {
  position: sticky;
  top: 60px;
  min-height: calc(100vh - 60px);
  align-self: flex-start;
  background: color-mix(in srgb, var(--app-card) 96%, transparent);
  border-right: 1px solid var(--app-border);
}

.layout-menu {
  border-right: none;
  padding-top: 16px;
}

.layout-main {
  min-height: calc(100vh - 60px);
  padding: 24px;
}

.mobile-tabbar {
  display: none;
}

.quick-add-button {
  position: fixed;
  right: 28px;
  bottom: 28px;
  z-index: 26;
  height: 54px;
  padding: 0 20px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: none;
  border-radius: 999px;
  color: #fff;
  background: linear-gradient(135deg, #0f766e, #f97316);
  box-shadow: 0 16px 34px rgba(15, 118, 110, 0.28);
  cursor: pointer;
  font-size: 16px;
  font-weight: 700;
}

.quick-add-button:hover {
  transform: translateY(-1px);
}

.quick-template-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 18px;
}

.quick-template {
  min-height: 62px;
  border: 1px solid var(--app-border);
  border-radius: 16px;
  background: var(--app-soft-card);
  color: var(--app-text);
  cursor: pointer;
}

.quick-template span,
.quick-template small {
  display: block;
}

.quick-template span {
  margin-bottom: 4px;
  font-weight: 700;
}

.quick-template small {
  color: var(--app-muted);
}

.inline-category-create {
  width: 100%;
  display: grid;
  grid-template-columns: 1fr auto auto auto;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}

.category-create-toggle {
  width: 100%;
  margin-top: 10px;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 16px;
  transition: background 0.2s ease;
}

.user-trigger:hover {
  background: var(--app-soft-card);
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.user-meta span {
  font-size: 12px;
  color: var(--app-muted);
}

@media (max-width: 900px) {
  .layout-header {
    padding: 0 16px;
  }

  .layout-aside {
    display: none;
  }

  .layout-main {
    padding: 16px 16px 86px;
  }

  .brand p,
  .user-meta {
    display: none;
  }

  .mobile-tabbar {
    position: fixed;
    left: 12px;
    right: 12px;
    bottom: 12px;
    z-index: 30;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 4px;
    padding: 8px;
    border: 1px solid var(--app-border);
    border-radius: 22px;
    background: color-mix(in srgb, var(--app-card) 92%, transparent);
    box-shadow: 0 18px 45px rgba(15, 23, 42, 0.16);
    backdrop-filter: blur(16px);
  }

  .quick-add-button {
    right: 20px;
    bottom: 88px;
    width: 58px;
    height: 58px;
    justify-content: center;
    padding: 0;
    border-radius: 20px;
  }

  .quick-add-button span {
    display: none;
  }

  .quick-template-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .inline-category-create {
    grid-template-columns: 1fr;
  }

  .tabbar-item {
    display: flex;
    min-height: 50px;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    gap: 4px;
    border-radius: 16px;
    color: var(--app-muted);
    text-decoration: none;
    font-size: 12px;
  }

  .tabbar-item.router-link-active {
    color: var(--app-primary);
    background: var(--app-primary-soft);
  }
}
</style>

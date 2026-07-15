<template>
  <div class="settings-page" :class="{ compact: compactMode }">
    <div class="page-head">
      <div>
        <h2>系统设置</h2>
        <p>管理个人资料、预算、分类、导出和使用偏好。</p>
      </div>
      <el-radio-group v-model="activeSection" size="large">
        <el-radio-button v-for="option in sectionOptions" :key="option.value" :value="option.value">
          {{ option.label }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <section v-show="activeSection === 'profile'" class="section-grid">
      <el-card class="panel-card profile-card">
        <template #header>
          <span>个人信息</span>
        </template>

        <div class="profile-box">
          <el-avatar :size="78" :src="userStore.user?.avatar">
            {{ userStore.user?.username?.slice(0, 1)?.toUpperCase() }}
          </el-avatar>
          <div>
            <h3>{{ userStore.user?.username || '未登录用户' }}</h3>
            <p>{{ userStore.user?.email || '未设置邮箱' }}</p>
          </div>
        </div>

        <el-form :model="userForm" label-position="top">
          <el-form-item label="用户名">
            <el-input v-model="userForm.username" disabled />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model.trim="userForm.email" placeholder="用于找回账号和展示联系方式" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model.trim="userForm.phone" placeholder="可选" />
          </el-form-item>
          <el-form-item ref="budgetFormItemRef" label="月度预算" class="budget-form-item">
            <el-input-number
              v-model="userForm.budget"
              :min="0"
              :precision="2"
              :step="100"
              style="width: 100%"
            />
          </el-form-item>
          <div class="quick-budget">
            <el-button v-for="value in budgetOptions" :key="value" plain @click="userForm.budget = value">
              ¥{{ value }}
            </el-button>
          </div>
          <el-button type="primary" :loading="updating" class="wide-button" @click="updateUserInfo">
            保存资料
          </el-button>
        </el-form>
      </el-card>

      <el-card class="panel-card">
        <template #header>
          <span>安全设置</span>
        </template>

        <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password prefix-icon="Lock" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password prefix-icon="Key" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password prefix-icon="Key" />
          </el-form-item>
          <el-button type="primary" :loading="changingPassword" class="wide-button" @click="changePassword">
            修改密码
          </el-button>
        </el-form>
      </el-card>
    </section>

    <section v-show="activeSection === 'category'" class="section-grid category-grid">
      <el-card class="panel-card">
        <template #header>
          <div class="card-head">
            <div>
              <span>分类管理</span>
              <small>可新增、编辑和删除未被账单使用的分类。</small>
            </div>
            <el-button type="primary" @click="showAddCategoryDialog">
              <el-icon><Plus /></el-icon>
              新增分类
            </el-button>
          </div>
        </template>

        <el-tabs v-model="activeCategoryTab">
          <el-tab-pane label="支出分类" name="expense">
            <div v-if="expenseCategories.length" class="category-list">
              <div v-for="category in expenseCategories" :key="category.id" class="category-item">
                <div class="category-meta">
                  <span class="category-dot" :style="{ backgroundColor: category.color || '#409EFF' }"></span>
                  <span>{{ formatCategoryName(category.name) }}</span>
                  <el-tag size="small" :type="category.userId ? 'success' : 'info'">
                    {{ category.userId ? '自定义' : '预设' }}
                  </el-tag>
                </div>
                <div class="category-actions">
                  <el-button text type="primary" @click="editCategory(category)">编辑</el-button>
                  <el-button text type="danger" @click="deleteCategory(category)">删除</el-button>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无支出分类" />
          </el-tab-pane>

          <el-tab-pane label="收入分类" name="income">
            <div v-if="incomeCategories.length" class="category-list">
              <div v-for="category in incomeCategories" :key="category.id" class="category-item">
                <div class="category-meta">
                  <span class="category-dot" :style="{ backgroundColor: category.color || '#409EFF' }"></span>
                  <span>{{ formatCategoryName(category.name) }}</span>
                  <el-tag size="small" :type="category.userId ? 'success' : 'info'">
                    {{ category.userId ? '自定义' : '预设' }}
                  </el-tag>
                </div>
                <div class="category-actions">
                  <el-button text type="primary" @click="editCategory(category)">编辑</el-button>
                  <el-button text type="danger" @click="deleteCategory(category)">删除</el-button>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无收入分类" />
          </el-tab-pane>
        </el-tabs>
      </el-card>

      <el-card class="panel-card">
        <template #header>
          <span>分类建议</span>
        </template>
        <div class="suggestion-list">
          <button v-for="item in categoryPresets" :key="item.name" type="button" @click="applyCategoryPreset(item)">
            <span class="category-dot" :style="{ backgroundColor: item.color }"></span>
            {{ item.name }}
          </button>
        </div>
      </el-card>
    </section>

    <section v-show="activeSection === 'data'" class="section-grid">
      <el-card class="panel-card">
        <template #header>
          <span>数据导出</span>
        </template>

        <el-form label-position="top">
          <el-form-item label="导出范围">
            <el-date-picker
              v-model="exportDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
          <div class="range-actions">
            <el-button plain @click="setExportRange('month')">本月</el-button>
            <el-button plain @click="setExportRange('quarter')">近三个月</el-button>
            <el-button plain @click="setExportRange('year')">今年</el-button>
          </div>
          <el-button type="primary" :loading="exporting" class="wide-button" @click="exportData">
            <el-icon><Download /></el-icon>
            导出 Excel
          </el-button>
        </el-form>
      </el-card>

      <el-card class="panel-card">
        <template #header>
          <span>使用偏好</span>
        </template>
        <div class="preference-list">
          <div>
            <strong>紧凑模式</strong>
            <span>减少页面留白，更适合小屏幕查看。</span>
          </div>
          <el-switch v-model="compactMode" @change="savePreferences" />
        </div>
        <div class="preference-list">
          <div>
            <strong>深色模式</strong>
            <span>夜间查看更柔和，适合手机端长时间使用。</span>
          </div>
          <el-switch v-model="darkMode" @change="savePreferences" />
        </div>
        <div class="preference-list">
          <div>
            <strong>默认展示支出分类</strong>
            <span>统计页优先关注支出结构。</span>
          </div>
          <el-switch v-model="expenseFirst" @change="savePreferences" />
        </div>
      </el-card>
    </section>

    <el-dialog v-model="categoryDialogVisible" :title="isEditCategory ? '编辑分类' : '新增分类'" width="420px">
      <el-form ref="categoryFormRef" :model="categoryForm" :rules="categoryRules" label-position="top">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model.trim="categoryForm.name" maxlength="12" placeholder="例如：早餐、兼职、教材" />
        </el-form-item>
        <el-form-item label="分类类型" prop="type">
          <el-radio-group v-model="categoryForm.type">
            <el-radio value="expense">支出</el-radio>
            <el-radio value="income">收入</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类颜色" prop="color">
          <div class="color-row">
            <el-color-picker v-model="categoryForm.color" />
            <span class="color-preview" :style="{ backgroundColor: categoryForm.color }"></span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingCategory" @click="saveCategory">
          {{ isEditCategory ? '保存修改' : '立即新增' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { Download, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/utils/api'
import { formatCategoryName, normalizeCategoryName } from '@/utils/category'

const userStore = useUserStore()
const route = useRoute()

const sectionOptions = [
  { label: '账户', value: 'profile' },
  { label: '分类', value: 'category' },
  { label: '数据', value: 'data' }
]

const activeSection = ref('profile')
const updating = ref(false)
const changingPassword = ref(false)
const exporting = ref(false)
const savingCategory = ref(false)
const compactMode = ref(false)
const darkMode = ref(false)
const expenseFirst = ref(true)

const passwordFormRef = ref(null)
const categoryFormRef = ref(null)
const budgetFormItemRef = ref(null)

const activeCategoryTab = ref('expense')
const categoryDialogVisible = ref(false)
const isEditCategory = ref(false)

const categories = ref([])
const exportDateRange = ref([])
const budgetOptions = [1500, 3000, 5000, 8000]
const categoryPresets = [
  { name: '早餐', type: 'expense', color: '#f97316' },
  { name: '教材', type: 'expense', color: '#2563eb' },
  { name: '社团', type: 'expense', color: '#7c3aed' },
  { name: '兼职', type: 'income', color: '#16a34a' }
]

const userForm = reactive({
  username: '',
  email: '',
  phone: '',
  budget: 0
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const categoryForm = reactive({
  id: null,
  name: '',
  type: 'expense',
  color: '#409EFF',
  sortOrder: 99
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的新密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const categoryRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择分类类型', trigger: 'change' }],
  color: [{ required: true, message: '请选择分类颜色', trigger: 'change' }]
}

const expenseCategories = computed(() => dedupeCategories(categories.value).filter((item) => item.type === 'expense'))
const incomeCategories = computed(() => dedupeCategories(categories.value).filter((item) => item.type === 'income'))

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

const loadUserInfo = () => {
  if (!userStore.user) return

  Object.assign(userForm, {
    username: userStore.user.username,
    email: userStore.user.email || '',
    phone: userStore.user.phone || '',
    budget: Number(userStore.user.budget || 0)
  })
}

const loadPreferences = () => {
  compactMode.value = localStorage.getItem('compactMode') === 'true'
  darkMode.value = localStorage.getItem('darkMode') === 'true'
  expenseFirst.value = localStorage.getItem('expenseFirst') !== 'false'
}

const savePreferences = () => {
  localStorage.setItem('compactMode', String(compactMode.value))
  localStorage.setItem('darkMode', String(darkMode.value))
  localStorage.setItem('expenseFirst', String(expenseFirst.value))
  window.dispatchEvent(new CustomEvent('theme-changed'))
  ElMessage.success('偏好已保存')
}

const loadCategories = async () => {
  try {
    const response = await api.get('/categories')
    if (response.data.code === 200) {
      categories.value = dedupeCategories(response.data.data || [])
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
  }
}

const updateUserInfo = async () => {
  updating.value = true
  try {
    const response = await api.put(`/users/${userStore.user.id}`, userForm)
    if (response.data.code === 200) {
      ElMessage.success('个人信息已更新')
      await userStore.checkAuth()
      loadUserInfo()
    } else {
      ElMessage.error(response.data.message || '更新失败')
    }
  } catch (error) {
    console.error('更新个人信息失败:', error)
    ElMessage.error('更新个人信息失败')
  } finally {
    updating.value = false
  }
}

const changePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    changingPassword.value = true
    try {
      const response = await api.post(`/users/${userStore.user.id}/change-password`, {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })

      if (response.data.code === 200) {
        ElMessage.success('密码修改成功')
        Object.assign(passwordForm, {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        })
      } else {
        ElMessage.error(response.data.message || '密码修改失败')
      }
    } catch (error) {
      console.error('修改密码失败:', error)
      ElMessage.error('修改密码失败')
    } finally {
      changingPassword.value = false
    }
  })
}

const showAddCategoryDialog = () => {
  isEditCategory.value = false
  Object.assign(categoryForm, {
    id: null,
    name: '',
    type: activeCategoryTab.value,
    color: '#409EFF',
    sortOrder: 99
  })
  categoryDialogVisible.value = true
}

const editCategory = (category) => {
  isEditCategory.value = true
  Object.assign(categoryForm, {
    id: category.id,
    name: formatCategoryName(category.name),
    type: category.type,
    color: category.color || '#409EFF',
    sortOrder: category.sortOrder ?? 99
  })
  categoryDialogVisible.value = true
}

const applyCategoryPreset = (preset) => {
  isEditCategory.value = false
  Object.assign(categoryForm, {
    id: null,
    name: preset.name,
    type: preset.type,
    color: preset.color,
    sortOrder: 99
  })
  activeCategoryTab.value = preset.type
  categoryDialogVisible.value = true
}

const saveCategory = async () => {
  if (!categoryFormRef.value) return

  await categoryFormRef.value.validate(async (valid) => {
    if (!valid) return

    savingCategory.value = true
    try {
      const payload = {
        name: categoryForm.name,
        type: categoryForm.type,
        color: categoryForm.color,
        icon: '',
        sortOrder: categoryForm.sortOrder ?? 99
      }
      const url = isEditCategory.value ? `/categories/${categoryForm.id}` : '/categories'
      const method = isEditCategory.value ? 'put' : 'post'
      const response = await api[method](url, payload)

      if (response.data.code === 200) {
        ElMessage.success(isEditCategory.value ? '分类更新成功' : '分类新增成功')
        categoryDialogVisible.value = false
        await loadCategories()
        activeCategoryTab.value = categoryForm.type
      } else {
        ElMessage.error(response.data.message || '操作失败')
      }
    } catch (error) {
      console.error('保存分类失败:', error)
      ElMessage.error('保存分类失败')
    } finally {
      savingCategory.value = false
    }
  })
}

const deleteCategory = async (category) => {
  try {
    await ElMessageBox.confirm(
      `确认删除分类“${formatCategoryName(category.name)}”吗？如果该分类已有账单，系统会阻止删除。`,
      '删除提示',
      { type: 'warning' }
    )

    const response = await api.delete(`/categories/${category.id}`)
    if (response.data.code === 200) {
      ElMessage.success('分类已删除')
      await loadCategories()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
      ElMessage.error(error?.response?.data?.message || '删除分类失败')
    }
  }
}

const setExportRange = (type) => {
  const today = dayjs()
  if (type === 'month') {
    exportDateRange.value = [today.startOf('month').format('YYYY-MM-DD'), today.endOf('month').format('YYYY-MM-DD')]
  } else if (type === 'quarter') {
    exportDateRange.value = [today.subtract(2, 'month').startOf('month').format('YYYY-MM-DD'), today.format('YYYY-MM-DD')]
  } else {
    exportDateRange.value = [today.startOf('year').format('YYYY-MM-DD'), today.format('YYYY-MM-DD')]
  }
}

const focusBudgetEditor = async () => {
  activeSection.value = 'profile'
  await nextTick()

  const element = budgetFormItemRef.value?.$el
  element?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  element?.classList.add('is-focus-hint')

  const input = element?.querySelector('input')
  input?.focus()

  window.setTimeout(() => {
    element?.classList.remove('is-focus-hint')
  }, 1800)
}

const exportData = async () => {
  if (!exportDateRange.value || exportDateRange.value.length !== 2) {
    ElMessage.warning('请选择导出日期范围')
    return
  }

  exporting.value = true
  try {
    const response = await api.get('/statistics/export', {
      params: {
        startDate: exportDateRange.value[0],
        endDate: exportDateRange.value[1]
      },
      responseType: 'blob'
    })

    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    const disposition = response.headers['content-disposition'] || ''
    const matched = disposition.match(/filename\*=UTF-8''(.+)$/)
    const fileName = matched ? decodeURIComponent(matched[1]) : `账单数据_${exportDateRange.value[0]}_${exportDateRange.value[1]}.xlsx`

    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)

    ElMessage.success('Excel 导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  loadPreferences()
  loadUserInfo()
  loadCategories()
  setExportRange('month')
  if (['profile', 'category', 'data'].includes(route.query.section)) {
    activeSection.value = route.query.section
  }
  if (route.query.focus === 'budget') {
    focusBudgetEditor()
  }
})

watch(
  () => route.query.section,
  (section) => {
    if (['profile', 'category', 'data'].includes(section)) {
      activeSection.value = section
    }
  }
)

watch(
  () => route.query.focus,
  (focus) => {
    if (focus === 'budget') {
      focusBudgetEditor()
    }
  }
)
</script>

<style scoped>
.settings-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.settings-page.compact {
  gap: 12px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.page-head h2 {
  margin: 0;
  font-size: 28px;
}

.page-head p {
  margin-top: 6px;
  color: var(--app-muted);
}

.section-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 18px;
}

.category-grid {
  grid-template-columns: minmax(0, 1.35fr) minmax(280px, 0.65fr);
}

.panel-card {
  border-radius: 8px;
}

.profile-box {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 22px;
}

.profile-box h3 {
  margin: 0 0 6px;
}

.profile-box p {
  color: var(--app-muted);
  margin: 0;
}

.quick-budget,
.range-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 0 0 18px;
}

.wide-button {
  width: 100%;
  border-radius: 8px;
}

.budget-form-item {
  border-radius: 14px;
  transition:
    background-color 0.2s ease,
    box-shadow 0.2s ease,
    padding 0.2s ease;
}

.budget-form-item.is-focus-hint {
  padding: 12px;
  background: rgba(15, 118, 110, 0.08);
  box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.16);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-head small {
  display: block;
  margin-top: 4px;
  color: var(--app-muted);
}

.category-list {
  display: grid;
  gap: 10px;
}

.category-item,
.preference-list {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 12px;
  border-radius: 8px;
  background: var(--app-soft-card);
}

.category-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.category-dot {
  width: 12px;
  height: 12px;
  flex: 0 0 auto;
  border-radius: 50%;
}

.category-actions {
  display: flex;
  gap: 4px;
}

.suggestion-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.suggestion-list button {
  min-height: 42px;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: var(--app-card);
  color: var(--app-text);
  cursor: pointer;
}

.preference-list + .preference-list {
  margin-top: 12px;
}

.preference-list strong,
.preference-list span {
  display: block;
}

.preference-list span {
  margin-top: 4px;
  color: var(--app-muted);
  font-size: 13px;
}

.color-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.color-preview {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  border: 1px solid var(--app-border);
}

@media (max-width: 980px) {
  .page-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .section-grid,
  .category-grid {
    grid-template-columns: 1fr;
  }
}
</style>

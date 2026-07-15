<template>
  <main class="auth-page">
    <section class="form-panel">
      <div class="form-heading">
        <h1>创建记账账号</h1>
        <p>注册后可使用账单管理、统计分析、分类维护和 Excel 导出。</p>
      </div>

      <el-form ref="registerFormRef" :model="registerForm" :rules="rules" class="auth-form" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="registerForm.username" placeholder="3 到 20 个字符" size="large" prefix-icon="User" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="至少 6 位"
            size="large"
            prefix-icon="Lock"
            show-password
          />
          <div class="password-meter">
            <span v-for="item in 3" :key="item" :class="{ active: item <= passwordLevel }"></span>
            <em>{{ passwordText }}</em>
          </div>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-row :gutter="12">
          <el-col :xs="24" :sm="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model.trim="registerForm.email" placeholder="可选" size="large" prefix-icon="Message" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model.trim="registerForm.phone" placeholder="可选" size="large" prefix-icon="Phone" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-checkbox v-model="agreed">我已确认这是课程项目演示账号，不填写真实敏感密码</el-checkbox>
        </el-form-item>

        <el-button type="primary" size="large" :loading="loading" class="submit-button" @click="handleRegister">
          {{ loading ? '注册中...' : '完成注册' }}
        </el-button>
      </el-form>

      <div class="switch-row">
        <span>已有账号？</span>
        <router-link to="/login">去登录</router-link>
      </div>
    </section>

    <section class="intro-panel">
      <div class="mock-phone">
        <div class="phone-header"></div>
        <div class="balance-card">
          <span>本月结余</span>
          <strong>¥2,580.00</strong>
        </div>
        <div class="mini-chart">
          <i style="height: 38%"></i>
          <i style="height: 72%"></i>
          <i style="height: 46%"></i>
          <i style="height: 86%"></i>
          <i style="height: 58%"></i>
        </div>
        <div class="phone-list">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
      <h2>小程序式轻量体验</h2>
      <p>核心流程围绕“记一笔、看趋势、做调整”展开，适合继续扩展成微信小程序原型。</p>
    </section>
  </main>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const registerFormRef = ref(null)
const loading = ref(false)
const agreed = ref(true)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: ''
})

const passwordLevel = computed(() => {
  let level = 0
  if (registerForm.password.length >= 6) level += 1
  if (/[A-Za-z]/.test(registerForm.password) && /\d/.test(registerForm.password)) level += 1
  if (/[^A-Za-z0-9]/.test(registerForm.password) || registerForm.password.length >= 10) level += 1
  return level
})

const passwordText = computed(() => {
  if (!registerForm.password) return '请输入密码'
  return ['偏弱', '可用', '较强'][passwordLevel.value - 1] || '偏弱'
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度需在 3 到 20 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  if (!agreed.value) {
    ElMessage.warning('请先确认演示账号提示')
    return
  }

  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const success = await userStore.register({
        username: registerForm.username,
        password: registerForm.password,
        email: registerForm.email,
        phone: registerForm.phone
      })

      if (success) {
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } else {
        ElMessage.error('注册失败，请检查用户名或邮箱是否已存在')
      }
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(460px, 540px) 1fr;
  background: #eef5f4;
}

.form-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px;
  background: #fff;
}

.form-heading h1 {
  margin: 0;
  color: #0f172a;
  font-size: 32px;
}

.form-heading p {
  max-width: 420px;
  margin: 10px 0 28px;
  color: #64748b;
  line-height: 1.7;
}

.auth-form {
  max-width: 460px;
}

.password-meter {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(3, 1fr) auto;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}

.password-meter span {
  height: 6px;
  border-radius: 999px;
  background: #dbe4ea;
}

.password-meter span.active {
  background: #0f766e;
}

.password-meter em {
  color: #64748b;
  font-style: normal;
  font-size: 12px;
}

.submit-button {
  width: 100%;
  height: 46px;
  border-radius: 8px;
}

.switch-row {
  display: flex;
  gap: 8px;
  margin-top: 24px;
  color: #64748b;
}

.switch-row a {
  color: #0f766e;
  font-weight: 600;
  text-decoration: none;
}

.intro-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
  color: #0f172a;
}

.mock-phone {
  width: 260px;
  min-height: 460px;
  padding: 18px;
  border: 10px solid #0f172a;
  border-radius: 36px;
  background: #f8fafc;
  box-shadow: 0 28px 70px rgba(15, 23, 42, 0.22);
}

.phone-header {
  width: 78px;
  height: 6px;
  margin: 2px auto 22px;
  border-radius: 999px;
  background: #cbd5e1;
}

.balance-card {
  padding: 18px;
  border-radius: 8px;
  background: #0f766e;
  color: #fff;
}

.balance-card span,
.balance-card strong {
  display: block;
}

.balance-card strong {
  margin-top: 8px;
  font-size: 26px;
}

.mini-chart {
  height: 140px;
  display: flex;
  align-items: end;
  gap: 12px;
  padding: 22px 6px;
}

.mini-chart i {
  flex: 1;
  border-radius: 8px 8px 0 0;
  background: #22c55e;
}

.phone-list {
  display: grid;
  gap: 12px;
}

.phone-list span {
  height: 36px;
  border-radius: 8px;
  background: #e2e8f0;
}

.intro-panel h2 {
  margin: 28px 0 8px;
  font-size: 28px;
}

.intro-panel p {
  max-width: 420px;
  color: #475569;
  line-height: 1.8;
  text-align: center;
}

@media (max-width: 920px) {
  .auth-page {
    grid-template-columns: 1fr;
  }

  .intro-panel {
    display: none;
  }

  .form-panel {
    padding: 34px 24px;
  }
}
</style>

<template>
  <main class="auth-page">
    <section class="auth-panel">
      <div class="brand-block">
        <div class="brand-icon">
          <el-icon><Wallet /></el-icon>
        </div>
        <p class="eyebrow">Campus Pocket</p>
        <h1>让每一笔收支都有迹可循</h1>
        <p class="summary">适合课程展示和小程序原型的个人记账看板，登录后即可查看账单、分类和趋势图。</p>
      </div>

      <div class="feature-strip">
        <div>
          <strong>3</strong>
          <span>类图表</span>
        </div>
        <div>
          <strong>30s</strong>
          <span>快速记账</span>
        </div>
        <div>
          <strong>Excel</strong>
          <span>账单导出</span>
        </div>
      </div>
    </section>

    <section class="form-panel">
      <div class="form-heading">
        <h2>欢迎回来</h2>
        <p>继续管理你的校园日常收支。</p>
      </div>

      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" class="auth-form" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="loginForm.username" placeholder="请输入用户名" size="large" prefix-icon="User" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <div class="form-tools">
          <el-checkbox v-model="rememberAccount">记住账号</el-checkbox>
          <el-button link type="primary" @click="fillDemoAccount">使用测试账号</el-button>
        </div>

        <el-button type="primary" size="large" :loading="loading" class="submit-button" @click="handleLogin">
          {{ loading ? '登录中...' : '登录看板' }}
        </el-button>
      </el-form>

      <div class="switch-row">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
    </section>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)
const rememberAccount = ref(true)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度需在 3 到 20 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ]
}

const fillDemoAccount = () => {
  loginForm.username = 'testuser'
  loginForm.password = '123456'
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const success = await userStore.login(loginForm.username, loginForm.password)
      if (success) {
        if (rememberAccount.value) {
          localStorage.setItem('rememberedUsername', loginForm.username)
        } else {
          localStorage.removeItem('rememberedUsername')
        }
        ElMessage.success('登录成功')
        router.push('/')
      } else {
        ElMessage.error('用户名或密码错误')
      }
    } finally {
      loading.value = false
    }
  })
}

onMounted(() => {
  const rememberedUsername = localStorage.getItem('rememberedUsername')
  if (rememberedUsername) {
    loginForm.username = rememberedUsername
  }
})
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(340px, 1fr) 460px;
  background: #f4f7fb;
}

.auth-panel {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 64px;
  background:
    linear-gradient(150deg, rgba(16, 185, 129, 0.88), rgba(14, 116, 144, 0.90)),
    url("https://images.unsplash.com/photo-1554224155-6726b3ff858f?auto=format&fit=crop&w=1400&q=80");
  background-size: cover;
  background-position: center;
  color: #fff;
}

.brand-icon {
  width: 56px;
  height: 56px;
  display: grid;
  place-items: center;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.18);
  font-size: 30px;
}

.eyebrow {
  margin: 28px 0 12px;
  font-size: 13px;
  letter-spacing: 0;
  text-transform: uppercase;
  opacity: 0.84;
}

.brand-block h1 {
  max-width: 620px;
  margin: 0;
  font-size: 48px;
  line-height: 1.16;
}

.summary {
  max-width: 520px;
  margin-top: 18px;
  font-size: 17px;
  line-height: 1.8;
  opacity: 0.92;
}

.feature-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  max-width: 560px;
}

.feature-strip div {
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.12);
}

.feature-strip strong,
.feature-strip span {
  display: block;
}

.feature-strip strong {
  font-size: 24px;
}

.feature-strip span {
  margin-top: 6px;
  opacity: 0.84;
}

.form-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px;
  background: #fff;
}

.form-heading h2 {
  margin: 0;
  color: #0f172a;
  font-size: 30px;
}

.form-heading p {
  margin: 8px 0 30px;
  color: #64748b;
}

.auth-form {
  display: grid;
  gap: 2px;
}

.form-tools {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 2px 0 18px;
}

.submit-button {
  width: 100%;
  height: 46px;
  border-radius: 8px;
}

.switch-row {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 24px;
  color: #64748b;
}

.switch-row a {
  color: #0f766e;
  font-weight: 600;
  text-decoration: none;
}

@media (max-width: 900px) {
  .auth-page {
    grid-template-columns: 1fr;
  }

  .auth-panel {
    min-height: 320px;
    padding: 34px 24px;
  }

  .brand-block h1 {
    font-size: 34px;
  }

  .feature-strip {
    grid-template-columns: repeat(3, 1fr);
  }

  .feature-strip div {
    padding: 12px;
  }

  .form-panel {
    padding: 34px 24px;
  }
}
</style>

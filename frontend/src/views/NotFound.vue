<template>
  <main class="not-found-page">
    <section class="not-found-card">
      <p class="eyebrow">404</p>
      <h1>页面走丢了</h1>
      <p class="summary">你访问的页面不存在，可能是地址输入错误，或者页面已经被移动。</p>
      <div class="actions">
        <el-button type="primary" size="large" @click="goHome">返回看板</el-button>
        <el-button size="large" @click="goLogin">去登录</el-button>
      </div>
    </section>
  </main>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background:
    radial-gradient(circle at 20% 20%, rgba(15, 118, 110, 0.16), transparent 32%),
    linear-gradient(135deg, #f8fafc, #ecfdf5);
}

.not-found-card {
  width: min(520px, 100%);
  padding: 48px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 28px 80px rgba(15, 23, 42, 0.12);
  text-align: center;
}

.eyebrow {
  margin: 0;
  color: #0f766e;
  font-size: 54px;
  font-weight: 900;
  letter-spacing: 0.08em;
}

h1 {
  margin: 10px 0;
  color: #0f172a;
  font-size: 32px;
}

.summary {
  margin: 0 auto 28px;
  color: #64748b;
  line-height: 1.8;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 14px;
}

@media (max-width: 520px) {
  .not-found-card {
    padding: 34px 22px;
  }

  .actions {
    flex-direction: column;
  }
}
</style>

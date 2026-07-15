import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue')
      },
      {
        path: 'bills',
        name: 'Bills',
        component: () => import('@/views/Bills.vue')
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics.vue')
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// Entering protected pages validates both local cache and backend Session.
router.beforeEach(async (to) => {
  const userStore = useUserStore()
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
  const isAuthPage = to.path === '/login' || to.path === '/register'

  if (!userStore.user) {
    userStore.loadUserFromStorage()
  }

  if (requiresAuth && !userStore.isLoggedIn) {
    return '/login'
  }

  if (userStore.isLoggedIn) {
    const validSession = await userStore.checkAuth({ silent: true })
    if (!validSession && requiresAuth) {
      return '/login'
    }
    if (validSession && isAuthPage) {
      return '/'
    }
  }

  return true
})

export default router

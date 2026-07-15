import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import api from '@/utils/api'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!user.value)

  function clearAuth() {
    user.value = null
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  async function login(username, password) {
    try {
      const response = await api.post('/auth/login', { username, password })
      if (response.data.code === 200) {
        user.value = response.data.data.user
        token.value = response.data.data.token
        localStorage.setItem('token', token.value)
        localStorage.setItem('user', JSON.stringify(user.value))
        return true
      }
      return false
    } catch (error) {
      console.error('登录失败:', error)
      return false
    }
  }

  async function register(userData) {
    try {
      const response = await api.post('/auth/register', userData)
      return response.data.code === 200
    } catch (error) {
      console.error('注册失败:', error)
      return false
    }
  }

  async function logout() {
    try {
      await api.post('/auth/logout')
    } catch (error) {
      console.error('退出登录失败:', error)
    } finally {
      clearAuth()
    }
  }

  async function checkAuth(options = {}) {
    try {
      const response = await api.get('/auth/check', {
        suppressErrorMessage: options.silent,
        suppressAuthRedirect: options.silent
      })
      if (response.data.code === 200) {
        user.value = response.data.data.user
        localStorage.setItem('user', JSON.stringify(user.value))
        return true
      }
      clearAuth()
      return false
    } catch (error) {
      if (!options.silent) {
        console.error('认证检查失败:', error)
      }
      clearAuth()
      return false
    }
  }

  function loadUserFromStorage() {
    const savedUser = localStorage.getItem('user')
    if (!savedUser) return

    try {
      user.value = JSON.parse(savedUser)
    } catch (error) {
      console.error('恢复用户信息失败:', error)
      clearAuth()
    }
  }

  return {
    user,
    token,
    isLoggedIn,
    clearAuth,
    login,
    register,
    logout,
    checkAuth,
    loadUserFromStorage
  }
})

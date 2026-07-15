import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true
})

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const config = error.config || {}
    const suppressErrorMessage = config.suppressErrorMessage
    const suppressAuthRedirect = config.suppressAuthRedirect

    if (error.response) {
      const { status, data } = error.response
      const message = data?.message

      if (status === 401) {
        if (!suppressErrorMessage) {
          ElMessage.error(message || '登录已过期，请重新登录')
        }
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        if (!suppressAuthRedirect && router.currentRoute.value.path !== '/login') {
          router.push('/login')
        }
      } else if (status === 403) {
        if (!suppressErrorMessage) ElMessage.error(message || '没有权限访问该资源')
      } else if (status === 404) {
        if (!suppressErrorMessage) ElMessage.error(message || '接口不存在，请检查后端服务')
      } else if (status === 500) {
        if (!suppressErrorMessage) ElMessage.error(message || '服务器内部错误')
      } else if (!suppressErrorMessage) {
        ElMessage.error(message || '请求失败')
      }
    } else if (error.request) {
      if (!suppressErrorMessage) ElMessage.error('无法连接后端服务，请确认 Tomcat 8080 已启动')
    } else if (!suppressErrorMessage) {
      ElMessage.error('请求配置错误')
    }

    return Promise.reject(error)
  }
)

export default api

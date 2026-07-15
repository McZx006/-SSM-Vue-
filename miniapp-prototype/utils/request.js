const BASE_URL = 'http://localhost:8080/account-dashboard/api'

const getCookie = () => wx.getStorageSync('SESSION_COOKIE') || ''

const isLoggedIn = () => !!wx.getStorageSync('userInfo')

const ensureLogin = () => {
  if (isLoggedIn()) return true
  wx.showToast({ title: '请先登录', icon: 'none' })
  setTimeout(() => wx.reLaunch({ url: '/pages/login/index' }), 500)
  return false
}

const saveCookie = (response) => {
  const cookie = response.header && (response.header['Set-Cookie'] || response.header['set-cookie'])
  if (cookie) {
    wx.setStorageSync('SESSION_COOKIE', cookie)
  }
}

const clearLogin = () => {
  wx.removeStorageSync('userInfo')
  wx.removeStorageSync('SESSION_COOKIE')
}

const request = ({ url, method = 'GET', data = {} }) => {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header: {
        'content-type': 'application/json',
        Cookie: getCookie()
      },
      success: (response) => {
        saveCookie(response)
        if (response.statusCode === 401) {
          clearLogin()
          wx.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
          setTimeout(() => wx.reLaunch({ url: '/pages/login/index' }), 500)
          return
        }
        resolve(response.data)
      },
      fail: reject
    })
  })
}

module.exports = {
  request,
  BASE_URL,
  ensureLogin,
  clearLogin
}

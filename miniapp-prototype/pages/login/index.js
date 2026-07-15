const { request } = require('../../utils/request')

Page({
  data: {
    loading: false,
    form: {
      username: '',
      password: ''
    }
  },
  onLoad() {
    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo && userInfo.username) {
      this.setData({
        'form.username': userInfo.username
      })
    }
  },
  handleInput(event) {
    const field = event.currentTarget.dataset.field
    this.setData({
      [`form.${field}`]: event.detail.value
    })
  },
  fillDemo() {
    this.setData({
      form: {
        username: 'testuser',
        password: '123456'
      }
    })
  },
  async handleLogin() {
    const { username, password } = this.data.form
    if (!username || !password) {
      wx.showToast({ title: '请输入用户名和密码', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    try {
      const result = await request({
        url: '/auth/login',
        method: 'POST',
        data: { username, password }
      })

      if (result.code === 200) {
        wx.setStorageSync('userInfo', result.data)
        wx.switchTab({ url: '/pages/home/index' })
      } else {
        wx.showToast({ title: result.message || '登录失败', icon: 'none' })
      }
    } catch (error) {
      wx.showToast({ title: '服务器连接失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  goRegister() {
    wx.navigateTo({ url: '/pages/register/index' })
  }
})

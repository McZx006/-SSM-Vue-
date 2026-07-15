const { request } = require('../../utils/request')

Page({
  data: {
    loading: false,
    form: {
      username: '',
      email: '',
      password: '',
      confirmPassword: ''
    },
    passwordStrength: 0,
    passwordStrengthText: '弱'
  },
  handleInput(event) {
    const field = event.currentTarget.dataset.field
    const value = event.detail.value
    this.setData({
      [`form.${field}`]: value
    })
    if (field === 'password') {
      this.updatePasswordStrength(value)
    }
  },
  updatePasswordStrength(password) {
    let score = 0
    if (password.length >= 6) score += 34
    if (/[A-Za-z]/.test(password)) score += 33
    if (/\d/.test(password)) score += 33
    this.setData({
      passwordStrength: score,
      passwordStrengthText: score >= 80 ? '强' : score >= 50 ? '中' : '弱'
    })
  },
  async handleRegister() {
    const { username, email, password, confirmPassword } = this.data.form
    if (!username || username.length < 3) {
      wx.showToast({ title: '用户名至少 3 位', icon: 'none' })
      return
    }
    if (!password || password.length < 6) {
      wx.showToast({ title: '密码至少 6 位', icon: 'none' })
      return
    }
    if (password !== confirmPassword) {
      wx.showToast({ title: '两次密码不一致', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    try {
      const result = await request({
        url: '/auth/register',
        method: 'POST',
        data: { username, email, password }
      })

      if (result.code === 200) {
        wx.showToast({ title: '注册成功' })
        setTimeout(() => wx.navigateBack(), 500)
      } else {
        wx.showToast({ title: result.message || '注册失败', icon: 'none' })
      }
    } catch (error) {
      wx.showToast({ title: '服务器连接失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  goLogin() {
    wx.navigateBack()
  }
})

Page({
  data: {
    userInfo: null,
    avatarText: 'T',
    budget: '5000.00',
    menuItems: [
      { title: '分类管理', desc: '维护餐饮、交通、兼职等常用分类' },
      { title: '预算设置', desc: '设置月度预算，超支前提醒' },
      { title: '深色模式', desc: '后续可接入微信本地主题偏好' },
      { title: '导出 Excel', desc: '复用 Web 端导出接口生成账单表格' }
    ]
  },
  onShow() {
    const userInfo = wx.getStorageSync('userInfo') || null
    this.setData({
      userInfo,
      avatarText: userInfo && userInfo.username ? userInfo.username.slice(0, 1).toUpperCase() : 'T'
    })
  },
  logout() {
    wx.removeStorageSync('userInfo')
    wx.removeStorageSync('SESSION_COOKIE')
    wx.reLaunch({ url: '/pages/login/index' })
  }
})

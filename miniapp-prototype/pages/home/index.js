const { ensureLogin, request } = require('../../utils/request')

const currentDateParts = () => {
  const date = new Date()
  return {
    month: `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`,
    year: String(date.getFullYear())
  }
}

Page({
  data: {
    loading: false,
    budgetUsage: 0,
    budgetText: '预算状态健康',
    summary: {
      income: '0.00',
      expense: '0.00',
      balance: '0.00'
    },
    advice: '坚持每天记录，月底复盘会更轻松。'
  },
  onShow() {
    if (!ensureLogin()) return
    this.loadDashboard()
  },
  async loadDashboard() {
    const { month, year } = currentDateParts()
    this.setData({ loading: true })
    try {
      const result = await request({
        url: '/statistics/dashboard',
        data: { month, year }
      })

      if (!result || result.code !== 200) {
        wx.showToast({ title: result?.message || '首页数据加载失败', icon: 'none' })
        return
      }

      const data = result.data || {}
      const usage = Math.min(Math.round(Number(data.budgetUsage || 0)), 100)
      const topCategory = (data.categoryStatistics || [])[0]
      this.setData({
        budgetUsage: usage,
        budgetText: data.budgetExceeded ? '预算已超支' : usage >= 80 ? '预算接近上限' : '预算状态健康',
        summary: {
          income: Number(data.monthIncome || 0).toFixed(2),
          expense: Number(data.monthExpense || 0).toFixed(2),
          balance: Number(data.monthBalance || 0).toFixed(2)
        },
        advice: topCategory
          ? `${topCategory.categoryName || topCategory.name} 是本月主要支出，建议每周复盘一次。`
          : '本月账单还不多，可以先从早餐、通勤、教材这类高频场景开始记录。'
      })
    } catch (error) {
      wx.showToast({ title: '服务器连接失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  openQuickBill(event) {
    const template = event.currentTarget.dataset.template || ''
    wx.navigateTo({
      url: `/pages/bill-form/index?template=${template}`
    })
  }
})

const { ensureLogin, request } = require('../../utils/request')

const now = new Date()
const currentYear = String(now.getFullYear())
const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`

Page({
  data: {
    loading: false,
    month: currentMonth,
    year: currentYear,
    categories: [],
    monthly: [],
    topCategory: null,
    advice: '记录越稳定，图表越有参考价值。建议每天固定时间补全当天账单。'
  },
  onShow() {
    if (!ensureLogin()) return
    this.loadStatistics()
  },
  changeMonth(event) {
    this.setData({ month: event.detail.value })
    this.loadStatistics()
  },
  changeYear(event) {
    this.setData({ year: String(event.detail.value).slice(0, 4) })
    this.loadStatistics()
  },
  async loadStatistics() {
    this.setData({ loading: true })
    try {
      const [categoryResult, monthlyResult] = await Promise.all([
        request({ url: '/statistics/category', data: { month: this.data.month, type: 'expense' } }),
        request({ url: '/statistics/monthly', data: { year: this.data.year } })
      ])

      const categories = categoryResult && categoryResult.code === 200
        ? this.normalizeCategories(categoryResult.data || [])
        : []
      const monthly = monthlyResult && monthlyResult.code === 200
        ? this.normalizeMonthly(monthlyResult.data || [])
        : []
      this.setData({
        categories,
        monthly,
        topCategory: categories[0] || null,
        advice: this.buildAdvice(categories, monthly)
      })
    } catch (error) {
      wx.showToast({ title: '统计加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  normalizeCategories(list) {
    const total = list.reduce((sum, item) => sum + Number(item.amount || 0), 0)
    return list
      .map((item) => {
        const amount = Number(item.amount || 0)
        return {
          name: item.categoryName || item.name || '未分类',
          amount: amount.toFixed(2),
          percent: total > 0 ? Math.round((amount / total) * 100) : 0
        }
      })
      .sort((a, b) => Number(b.amount) - Number(a.amount))
  },
  normalizeMonthly(list) {
    return list.map((item) => ({
      month: String(item.month || '').slice(5) || item.month,
      income: Number(item.income || 0).toFixed(0),
      expense: Number(item.expense || 0).toFixed(0)
    }))
  },
  buildAdvice(categories, monthly) {
    if (categories.length && categories[0].percent >= 45) {
      return `${categories[0].name} 占比达到 ${categories[0].percent}%，建议设置单独预算并优先复盘大额支出。`
    }
    const last = monthly[monthly.length - 1]
    if (last && Number(last.expense) > Number(last.income)) {
      return '最近月份支出高于收入，建议先暂停非必要消费，并把固定支出单独标记。'
    }
    return '本月消费结构较均衡，可以继续保持每日记账和每周复盘。'
  }
})

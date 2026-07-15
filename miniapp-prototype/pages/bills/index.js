const { ensureLogin, request } = require('../../utils/request')

const formatDate = (date) => {
  if (!date) return ''
  return String(date).slice(0, 10)
}

const currentMonthRange = () => {
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const lastDay = new Date(year, date.getMonth() + 1, 0).getDate()
  return {
    startDate: `${year}-${month}-01`,
    endDate: `${year}-${month}-${String(lastDay).padStart(2, '0')}`
  }
}

Page({
  data: {
    loading: false,
    activeFilter: 'month',
    bills: [],
    summary: {
      income: '0.00',
      expense: '0.00',
      balance: '0.00'
    }
  },
  onShow() {
    if (!ensureLogin()) return
    this.loadBills()
  },
  changeFilter(event) {
    this.setData({
      activeFilter: event.currentTarget.dataset.filter
    })
    this.loadBills()
  },
  buildParams() {
    const params = {
      pageNum: 1,
      pageSize: 30
    }

    if (this.data.activeFilter === 'month') {
      Object.assign(params, currentMonthRange())
    }

    if (this.data.activeFilter === 'expense' || this.data.activeFilter === 'income') {
      params.type = this.data.activeFilter
    }

    return params
  },
  normalizeBills(list) {
    return (list || []).map((item) => ({
      id: item.id,
      type: item.type,
      date: formatDate(item.date),
      remark: item.remark || '无备注',
      amount: Number(item.amount || 0).toFixed(2),
      categoryName: item.category && item.category.name ? item.category.name : '未分类'
    }))
  },
  buildSummary(bills) {
    const summary = bills.reduce((acc, item) => {
      const amount = Number(item.amount || 0)
      if (item.type === 'income') {
        acc.income += amount
      } else {
        acc.expense += amount
      }
      return acc
    }, { income: 0, expense: 0 })

    return {
      income: summary.income.toFixed(2),
      expense: summary.expense.toFixed(2),
      balance: (summary.income - summary.expense).toFixed(2)
    }
  },
  async loadBills() {
    this.setData({ loading: true })
    try {
      const result = await request({
        url: '/bills',
        data: this.buildParams()
      })

      if (result && result.code === 200) {
        const pageData = result.data || {}
        const bills = this.normalizeBills(pageData.list || [])
        this.setData({
          bills,
          summary: this.buildSummary(bills)
        })
      } else {
        wx.showToast({ title: result?.message || '加载账单失败', icon: 'none' })
      }
    } catch (error) {
      wx.showToast({ title: '服务器连接失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  }
})

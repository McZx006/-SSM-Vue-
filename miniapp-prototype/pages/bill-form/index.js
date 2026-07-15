const { request } = require('../../utils/request')

const templates = {
  breakfast: { type: 'expense', categoryName: '餐饮', amount: '12.00', remark: '早餐' },
  commute: { type: 'expense', categoryName: '交通', amount: '8.00', remark: '地铁/公交' },
  book: { type: 'expense', categoryName: '学习', amount: '45.00', remark: '学习资料' },
  parttime: { type: 'income', categoryName: '兼职', amount: '100.00', remark: '兼职收入' }
}

const today = () => {
  const date = new Date()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

Page({
  data: {
    loading: false,
    categories: [],
    filteredCategories: [],
    categoryIndex: 0,
    form: {
      type: 'expense',
      amount: '',
      categoryName: '',
      categoryId: null,
      date: today(),
      remark: ''
    }
  },
  async onLoad(options) {
    const preset = templates[options.template] || null
    if (preset) {
      this.setData({
        form: {
          ...this.data.form,
          ...preset,
          date: today()
        }
      })
    }
    await this.loadCategories()
    this.refreshFilteredCategories()
  },
  async loadCategories() {
    try {
      const result = await request({ url: '/categories' })
      if (result.code === 200) {
        this.setData({ categories: result.data || [] })
      }
    } catch (error) {
      wx.showToast({ title: '分类加载失败，可稍后重试', icon: 'none' })
    }
  },
  refreshFilteredCategories() {
    const { type, categoryName } = this.data.form
    const filtered = this.data.categories.filter((item) => item.type === type)
    const matchedIndex = filtered.findIndex((item) => item.name === categoryName)
    const categoryIndex = matchedIndex >= 0 ? matchedIndex : 0
    const selected = filtered[categoryIndex] || null
    this.setData({
      filteredCategories: filtered,
      categoryIndex,
      'form.categoryId': selected ? selected.id : null,
      'form.categoryName': selected ? selected.name : categoryName
    })
  },
  changeType(event) {
    this.setData({
      'form.type': event.currentTarget.dataset.type,
      'form.categoryId': null,
      'form.categoryName': ''
    })
    this.refreshFilteredCategories()
  },
  changeCategory(event) {
    const index = Number(event.detail.value || 0)
    const selected = this.data.filteredCategories[index]
    if (!selected) return
    this.setData({
      categoryIndex: index,
      'form.categoryId': selected.id,
      'form.categoryName': selected.name
    })
  },
  changeDate(event) {
    this.setData({
      'form.date': event.detail.value
    })
  },
  handleInput(event) {
    const field = event.currentTarget.dataset.field
    this.setData({
      [`form.${field}`]: event.detail.value
    })
  },
  async submitBill() {
    const { type, amount, categoryId, date, remark } = this.data.form
    if (!amount || Number(amount) <= 0) {
      wx.showToast({ title: '请输入正确金额', icon: 'none' })
      return
    }
    if (!categoryId) {
      wx.showToast({ title: '请选择分类', icon: 'none' })
      return
    }
    if (!date) {
      wx.showToast({ title: '请选择日期', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    try {
      const result = await request({
        url: '/bills',
        method: 'POST',
        data: {
          type,
          amount: Number(amount),
          categoryId,
          date,
          remark
        }
      })

      if (result.code === 200) {
        wx.showToast({ title: '保存成功' })
        setTimeout(() => wx.navigateBack(), 500)
      } else {
        wx.showToast({ title: result.message || '保存失败', icon: 'none' })
      }
    } catch (error) {
      wx.showToast({ title: '服务器连接失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  }
})

const categoryNameMap = {
  Food: '餐饮',
  Shopping: '购物',
  Transport: '交通',
  Entertainment: '娱乐',
  Medical: '医疗',
  Education: '学习',
  Housing: '住房',
  'Other Expense': '其他支出',
  Salary: '工资',
  Bonus: '奖金',
  Investment: '投资',
  'Part-time': '兼职',
  'Other Income': '其他收入'
}

export const formatCategoryName = (name) => {
  return categoryNameMap[name] || name || '未分类'
}

export const normalizeCategoryName = (name) => {
  return formatCategoryName(name).trim().toLowerCase()
}

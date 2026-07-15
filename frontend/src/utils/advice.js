import { formatCategoryName } from '@/utils/category'

const toNumber = (value) => Number(value || 0)

export const buildDashboardAdvice = (dashboardData) => {
  const expense = toNumber(dashboardData.monthExpense)
  const income = toNumber(dashboardData.monthIncome)
  const balance = toNumber(dashboardData.monthBalance)
  const budgetUsage = toNumber(dashboardData.budgetUsage)
  const categories = [...(dashboardData.categoryStatistics || [])]
    .sort((a, b) => toNumber(b.amount) - toNumber(a.amount))

  const topCategory = categories[0]
  const topName = formatCategoryName(topCategory?.categoryName)
  const topAmount = toNumber(topCategory?.amount)
  const expenseRatio = income > 0 ? (expense / income) * 100 : 0

  const advice = []

  if (!expense && !income) {
    return [
      {
        title: '先记一笔',
        content: '当前月份还没有账单数据，可以从右下角“记一笔”开始，系统会自动生成图表和建议。'
      }
    ]
  }

  if (budgetUsage >= 100) {
    advice.push({
      title: '预算已超支',
      content: '本月支出已经超过预算，建议优先暂停非必要消费，并查看分类排行定位主要开销。'
    })
  } else if (budgetUsage >= 80) {
    advice.push({
      title: '预算接近上限',
      content: '预算使用率已经超过 80%，后续几天可以把购物、娱乐类消费设为观察项。'
    })
  } else {
    advice.push({
      title: '预算状态健康',
      content: '当前预算使用率比较稳，继续保持每日记账，就能更准确地控制本月节奏。'
    })
  }

  if (topCategory) {
    advice.push({
      title: `${topName} 是主要支出`,
      content: `本月该分类已支出 ¥${topAmount.toFixed(2)}，可以检查是否存在可合并、可替代或可延后的消费。`
    })
  }

  if (balance < 0) {
    advice.push({
      title: '结余为负',
      content: '本月支出高于收入，建议设置一个短期目标，例如未来 7 天只保留必要消费。'
    })
  } else if (expenseRatio >= 70) {
    advice.push({
      title: '收入消耗偏高',
      content: `本月支出约占收入 ${expenseRatio.toFixed(0)}%，可以尝试把至少 20% 收入保留下来。`
    })
  } else {
    advice.push({
      title: '收支结构不错',
      content: '本月仍有正向结余，可以考虑把剩余金额拆分为储蓄、学习和应急备用。'
    })
  }

  return advice.slice(0, 3)
}

export const buildTrendAdvice = (categoryStatistics, monthlyStatistics) => {
  const sortedCategories = [...(categoryStatistics || [])].sort((a, b) => toNumber(b.amount) - toNumber(a.amount))
  const sortedMonths = [...(monthlyStatistics || [])].sort((a, b) => toNumber(b.expense) - toNumber(a.expense))
  const topCategory = sortedCategories[0]
  const peakMonth = sortedMonths[0]
  const activeMonths = (monthlyStatistics || []).filter((item) => toNumber(item.income) || toNumber(item.expense))
  const totalBalance = activeMonths.reduce((sum, item) => sum + toNumber(item.balance), 0)

  const advice = []

  if (topCategory) {
    advice.push({
      title: '分类优化',
      content: `${formatCategoryName(topCategory.categoryName)} 排名最高，占比 ${toNumber(topCategory.percentage).toFixed(1)}%，建议先从这个分类做预算控制。`
    })
  }

  if (peakMonth?.month) {
    advice.push({
      title: '峰值月份',
      content: `${peakMonth.month} 是年度支出峰值，支出 ¥${toNumber(peakMonth.expense).toFixed(2)}，可复盘是否有集中购物或周期性缴费。`
    })
  }

  if (activeMonths.length) {
    advice.push({
      title: totalBalance >= 0 ? '年度结余健康' : '年度结余预警',
      content: totalBalance >= 0
        ? `当前统计年份累计结余 ¥${totalBalance.toFixed(2)}，可以继续保持记录习惯。`
        : `当前统计年份累计结余为 ¥${totalBalance.toFixed(2)}，建议降低非必要支出占比。`
    })
  }

  return advice.length ? advice : [
    {
      title: '等待数据',
      content: '添加 5 到 6 条不同月份和分类的账单后，系统会自动生成更完整的消费建议。'
    }
  ]
}

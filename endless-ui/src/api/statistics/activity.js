import request from '@/utils/request'

// 获取活跃度概览
export function getActivityOverview() {
  return request({
    url: '/statistics/activity/overview',
    method: 'get'
  })
}

// 获取活跃度趋势
export function getActivityTrend(params) {
  return request({
    url: '/statistics/activity/trend',
    method: 'get',
    params
  })
}

// 获取玩家活跃度排行榜
export function getPlayerRanking(params) {
  return request({
    url: '/statistics/activity/ranking',
    method: 'get',
    params
  })
}

// 获取趋势分析
export function getTrendAnalysis(params) {
  return request({
    url: '/statistics/activity/analysis',
    method: 'get',
    params
  })
}

// 获取活跃度统计列表
export function getActivityStatsList(params) {
  return request({
    url: '/statistics/activity/list',
    method: 'get',
    params
  })
}

// 生成每日报告
export function generateDailyReport(data) {
  return request({
    url: '/statistics/activity/generate/daily',
    method: 'post',
    data
  })
}

// 生成周报
export function generateWeeklyReport(data) {
  return request({
    url: '/statistics/activity/generate/weekly',
    method: 'post',
    data
  })
}

// 生成月报
export function generateMonthlyReport(data) {
  return request({
    url: '/statistics/activity/generate/monthly',
    method: 'post',
    data
  })
}

// 导出统计数据
export function exportActivityStats(params) {
  return request({
    url: '/statistics/activity/export',
    method: 'post',
    params,
    responseType: 'blob'
  })
}
// 统计模块路由配置示例
export default {
  path: '/statistics',
  component: () => import('@/layout/index'),
  redirect: '/statistics/player-activity',
  name: 'Statistics',
  meta: {
    title: '数据统计',
    icon: 'chart'
  },
  children: [
    {
      path: 'player-activity',
      component: () => import('@/views/statistics/playerActivity/index'),
      name: 'PlayerActivity',
      meta: {
        title: '玩家活跃度',
        icon: 'user'
      }
    },
    {
      path: 'reports',
      component: () => import('@/views/statistics/reports/index'),
      name: 'Reports',
      meta: {
        title: '统计报告',
        icon: 'document'
      }
    }
  ]
}
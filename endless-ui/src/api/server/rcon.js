import request from '@/utils/request'

// RCON连接
export function connectRcon(serverId) {
  return request({
    url: '/server/serverlist/rcon/connect/' + serverId,
    method: 'post'
  })
}

// 执行命令
export function executeCommand(serverId, command) {
  return request({
    url: '/server/serverlist/rcon/execute/' + serverId,
    method: 'post',
    data: {
      command: command
    }
  })
}

// 获取命令历史记录
export function getCommandHistory(query) {
  return request({
    url: '/history/command/list',
    method: 'get',
    params: query
  })
} 
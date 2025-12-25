import store from '@/store'
import {getUserRconServers} from '@/api/permission/rcon'

/**
 * RCON权限检查工具
 */
class RconPermissionChecker {
  constructor() {
    this.userRconServers = new Map() // 用户RCON服务器权限缓存
    this.cacheExpireTime = 5 * 60 * 1000 // 缓存5分钟
    this.lastCacheTime = 0
  }

  /**
   * 检查用户是否有RCON服务器权限
   * @param {number} serverId 服务器ID
   * @param {string} permission 权限类型 view/command/manage/admin
   * @returns {Promise<boolean>}
   */
  async hasRconPermission(serverId, permission = 'view') {
    try {
      const userId = store.getters.userId
      if (!userId) {
        return false
      }

      // 检查是否为管理员
      if (this.isAdmin()) {
        return true
      }

      // 获取用户RCON权限
      const userPermission = await this.getUserRconPermission(userId, serverId)
      if (!userPermission) {
        return false
      }

      // 检查权限是否过期
      if (userPermission.expireTime && new Date(userPermission.expireTime) < new Date()) {
        return false
      }

      // 检查权限状态
      if (userPermission.status !== '0') {
        return false
      }

      // 检查具体权限
      switch (permission) {
        case 'view':
          return userPermission.canViewLog === 1
        case 'command':
          return userPermission.canExecuteCmd === 1
        case 'manage':
          return userPermission.canManage === 1
        case 'admin':
          return userPermission.permissionType === 'admin'
        default:
          return false
      }
    } catch (error) {
      console.error('检查RCON权限失败:', error)
      return false
    }
  }

  /**
   * 获取用户RCON服务器权限
   * @param {number} userId 用户ID
   * @param {number} serverId 服务器ID
   * @returns {Promise<object|null>}
   */
  async getUserRconPermission(userId, serverId) {
    const cacheKey = `${userId}_${serverId}`

    // 检查缓存
    if (this.userRconServers.has(cacheKey) &&
      Date.now() - this.lastCacheTime < this.cacheExpireTime) {
      return this.userRconServers.get(cacheKey)
    }

    try {
      // 从API获取权限
      const response = await getUserRconServers(userId)
      const permissions = response.data || []

      // 更新缓存
      this.userRconServers.clear()
      permissions.forEach(permission => {
        const key = `${userId}_${permission.serverId}`
        this.userRconServers.set(key, permission)
      })
      this.lastCacheTime = Date.now()

      return this.userRconServers.get(cacheKey) || null
    } catch (error) {
      console.error('获取用户RCON权限失败:', error)
      return null
    }
  }

  /**
   * 检查用户是否为管理员
   * @returns {boolean}
   */
  isAdmin() {
    const roles = store.getters.roles || []
    return roles.includes('admin') || roles.includes('super_admin')
  }

  /**
   * 检查命令是否被允许执行
   * @param {number} serverId 服务器ID
   * @param {string} command 命令
   * @returns {Promise<boolean>}
   */
  async isCommandAllowed(serverId, command) {
    try {
      const userId = store.getters.userId
      if (!userId) {
        return false
      }

      // 管理员可以执行所有命令
      if (this.isAdmin()) {
        return true
      }

      const userPermission = await this.getUserRconPermission(userId, serverId)
      if (!userPermission || userPermission.canExecuteCmd !== 1) {
        return false
      }

      // 检查命令黑名单
      if (userPermission.cmdBlacklist) {
        try {
          const blacklist = JSON.parse(userPermission.cmdBlacklist)
          if (Array.isArray(blacklist)) {
            for (const blackCmd of blacklist) {
              if (command.toLowerCase().includes(blackCmd.toLowerCase())) {
                return false
              }
            }
          }
        } catch (e) {
          // 如果不是JSON格式，按逗号分割
          const blacklist = userPermission.cmdBlacklist.split(',')
          for (const blackCmd of blacklist) {
            if (command.toLowerCase().includes(blackCmd.trim().toLowerCase())) {
              return false
            }
          }
        }
      }

      // 检查命令白名单
      if (userPermission.cmdWhitelist) {
        try {
          const whitelist = JSON.parse(userPermission.cmdWhitelist)
          if (Array.isArray(whitelist) && whitelist.length > 0) {
            for (const whiteCmd of whitelist) {
              if (command.toLowerCase().includes(whiteCmd.toLowerCase())) {
                return true
              }
            }
            return false // 有白名单但命令不在其中
          }
        } catch (e) {
          // 如果不是JSON格式，按逗号分割
          const whitelist = userPermission.cmdWhitelist.split(',').filter(cmd => cmd.trim())
          if (whitelist.length > 0) {
            for (const whiteCmd of whitelist) {
              if (command.toLowerCase().includes(whiteCmd.trim().toLowerCase())) {
                return true
              }
            }
            return false // 有白名单但命令不在其中
          }
        }
      }

      return true // 没有白名单限制且不在黑名单中
    } catch (error) {
      console.error('检查命令权限失败:', error)
      return false
    }
  }

  /**
   * 获取用户可访问的RCON服务器ID列表
   * @returns {Promise<number[]>}
   */
  async getUserRconServerIds() {
    try {
      const userId = store.getters.userId
      if (!userId) {
        return []
      }

      // 管理员可以访问所有服务器
      if (this.isAdmin()) {
        return null // null表示所有服务器
      }

      const response = await getUserRconServers(userId)
      const permissions = response.data || []

      return permissions
        .filter(permission =>
          permission.status === '0' &&
          (!permission.expireTime || new Date(permission.expireTime) > new Date())
        )
        .map(permission => permission.serverId)
    } catch (error) {
      console.error('获取用户RCON服务器列表失败:', error)
      return []
    }
  }

  /**
   * 清除权限缓存
   */
  clearCache() {
    this.userRconServers.clear()
    this.lastCacheTime = 0
  }
}

// 创建单例实例
const rconPermissionChecker = new RconPermissionChecker()

export default rconPermissionChecker

/**
 * Vue指令：v-rcon-permission
 * 用法：v-rcon-permission="{ serverId: 1, permission: 'command' }"
 */
export const rconPermissionDirective = {
  bind(el, binding, vnode) {
    checkRconPermission(el, binding, vnode)
  },
  update(el, binding, vnode) {
    checkRconPermission(el, binding, vnode)
  }
}

async function checkRconPermission(el, binding, vnode) {
  const {serverId, permission = 'view'} = binding.value || {}

  if (!serverId) {
    console.warn('v-rcon-permission 指令缺少 serverId 参数')
    return
  }

  try {
    const hasPermission = await rconPermissionChecker.hasRconPermission(serverId, permission)

    if (!hasPermission) {
      el.style.display = 'none'
      el.disabled = true
    } else {
      el.style.display = ''
      el.disabled = false
    }
  } catch (error) {
    console.error('RCON权限检查失败:', error)
    el.style.display = 'none'
    el.disabled = true
  }
}

import rconPermissionChecker from '@/utils/rconPermission'

export default {
  inserted(el, binding, vnode) {
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
      el.parentNode && el.parentNode.removeChild(el)
    }
  } catch (error) {
    console.error('RCON权限检查失败:', error)
    el.parentNode && el.parentNode.removeChild(el)
  }
}

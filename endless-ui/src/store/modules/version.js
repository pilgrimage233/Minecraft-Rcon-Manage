import {checkUpdate} from '@/api/system/update'

const state = {
  currentVersion: '',
  latestVersion: '',
  hasUpdate: false,
  releaseNotes: '',
  downloadUrl: '',
  jarDownloadUrl: '',
  checking: false,
  lastCheckTime: null
}

const mutations = {
  SET_VERSION_INFO(state, info) {
    state.currentVersion = info.currentVersion
    state.latestVersion = info.latestVersion
    state.hasUpdate = info.hasUpdate
    state.releaseNotes = info.releaseNotes
    state.downloadUrl = info.downloadUrl
    state.jarDownloadUrl = info.jarDownloadUrl
  },
  SET_CHECKING(state, checking) {
    state.checking = checking
  },
  SET_LAST_CHECK_TIME(state) {
    state.lastCheckTime = new Date().getTime()
  }
}

const actions = {
  async checkUpdate({commit, state}, options = {}) {
    const force = Boolean(options && options.force)

    // 如果正在检查，直接返回
    if (state.checking) return

    // 自动检查时启用5分钟节流；手动检查可强制刷新
    const now = new Date().getTime()
    if (!force && state.lastCheckTime && now - state.lastCheckTime < 5 * 60 * 1000) {
      return
    }

    commit('SET_CHECKING', true)
    try {
      const res = await checkUpdate()

      if (res.code === 200) {
        commit('SET_VERSION_INFO', {
          currentVersion: res.currentVersion || '',
          latestVersion: res.latestVersion || '',
          hasUpdate: Boolean(res.hasUpdate),
          releaseNotes: res.releaseNotes || '',
          downloadUrl: res.downloadUrl || '',
          jarDownloadUrl: res.jarDownloadUrl || ''
        })
        commit('SET_LAST_CHECK_TIME')
      }
    } catch (error) {
      console.error('检查更新失败:', error)
    } finally {
      commit('SET_CHECKING', false)
    }
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

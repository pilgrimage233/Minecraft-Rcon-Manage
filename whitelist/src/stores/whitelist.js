import {defineStore} from 'pinia'
import request from '../utils/request'

export const useWhitelistStore = defineStore('whitelist', {
    state: () => ({
        whitelistData: {},
        onlinePlayers: new Set(),
        loading: false,
        lastUpdateTime: null,
        error: null
    }),

    getters: {
        isStale: (state) => {
            if (!state.lastUpdateTime) return true
            // 数据超过5分钟视为过期
            return Date.now() - state.lastUpdateTime > 5 * 60 * 1000
        }
    },

    actions: {
        async fetchWhitelist(force = false) {
            // 如果数据未过期且不强制刷新，直接返回
            if (!force && !this.isStale) return

            this.loading = true
            this.error = null

            try {
                const data = await request.get('/mc/whitelist/getWhiteList')
                this.whitelistData = data
                this.lastUpdateTime = Date.now()
            } catch (err) {
                this.error = err.message
                throw err
            } finally {
                this.loading = false
            }
        },

        clearError() {
            this.error = null
        }
    },

    persist: {
        enabled: true,
        strategies: [
            {
                storage: localStorage,
                paths: ['whitelistData', 'lastUpdateTime']
            }
        ]
    }
})
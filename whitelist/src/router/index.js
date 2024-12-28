import {createRouter, createWebHashHistory} from 'vue-router'

const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        {
            path: '/',
            name: 'home',
            component: () => import('../components/WhiteListApplication.vue')
        },
        {
            path: '/whitelist-members',
            name: 'members',
            component: () => import('../components/WhiteListMember.vue')
        }
    ]
})

export default router 
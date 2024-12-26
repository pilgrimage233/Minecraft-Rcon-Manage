import {createRouter, createWebHistory} from 'vue-router'
import WhiteListApplication from '../components/WhiteListApplication.vue'
import WhiteListMember from '../components/WhiteListMember.vue'

const routes = [
    {
        path: '/',
        name: 'WhiteListApplication',
        component: WhiteListApplication
    },
    {
        path: '/whitelist-members',
        name: 'WhiteListMembers',
        component: WhiteListMember
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router 
import { createRouter, createWebHashHistory } from 'vue-router'
import routes from './routes'
import { setupRouterGuards } from './guards'

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: (_to, _from, savedPosition) => {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0, left: 0 }
  },
})

setupRouterGuards(router)

export default router

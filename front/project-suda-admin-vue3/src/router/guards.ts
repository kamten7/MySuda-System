import type { Router } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/cookies'

NProgress.configure({ showSpinner: false })

export function setupRouterGuards(router: Router) {
  router.beforeEach((to, _from, next) => {
    NProgress.start()

    const token = getToken()

    // Routes that don't need auth
    if (to.meta.requiresAuth === false) {
      // Already logged in, going to login page -> redirect to dashboard
      if (to.path === '/login' && token) {
        return next('/dashboard')
      }
      return next()
    }

    // Auth required but no token
    if (!token) {
      return next('/login')
    }

    next()
  })

  router.afterEach((to) => {
    NProgress.done()
    if (to.meta.title) {
      document.title = to.meta.title as string
    }
  })
}

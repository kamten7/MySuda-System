import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '速达外卖 - 登录', requiresAuth: false },
  },
  {
    path: '/client-login',
    name: 'ClientLogin',
    component: () => import('@/views/client-login/index.vue'),
    meta: { title: '速达外卖 - 客户端登录', requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'Monitor', affix: true },
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/index.vue'),
        meta: { title: '数据统计', icon: 'DataAnalysis' },
      },
      {
        path: 'order',
        name: 'OrderList',
        component: () => import('@/views/order/index.vue'),
        meta: { title: '订单管理', icon: 'Document' },
      },
      {
        path: 'dish',
        name: 'DishList',
        component: () => import('@/views/dish/index.vue'),
        meta: { title: '菜品管理', icon: 'Dish' },
      },
      {
        path: 'dish/edit/:id?',
        name: 'DishEdit',
        component: () => import('@/views/dish/edit.vue'),
        meta: { title: '菜品编辑', hidden: true },
      },
      {
        path: 'setmeal',
        name: 'SetmealList',
        component: () => import('@/views/setmeal/index.vue'),
        meta: { title: '套餐管理', icon: 'SetUp' },
      },
      {
        path: 'setmeal/edit/:id?',
        name: 'SetmealEdit',
        component: () => import('@/views/setmeal/edit.vue'),
        meta: { title: '套餐编辑', hidden: true },
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/category/index.vue'),
        meta: { title: '分类管理', icon: 'Menu' },
      },
      {
        path: 'employee',
        name: 'EmployeeList',
        component: () => import('@/views/employee/index.vue'),
        meta: { title: '员工管理', icon: 'User' },
      },
      {
        path: 'employee/edit/:id?',
        name: 'EmployeeEdit',
        component: () => import('@/views/employee/edit.vue'),
        meta: { title: '员工编辑', hidden: true },
      },
      {
        path: 'message',
        name: 'MessageCenter',
        component: () => import('@/views/message/index.vue'),
        meta: { title: '消息通知', icon: 'Bell' },
      },
      {
        path: 'ai',
        name: 'AIChat',
        component: () => import('@/views/ai/index.vue'),
        meta: { title: 'AI智能分析', icon: 'ChatDotRound' },
      },
    ],
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', requiresAuth: false },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    meta: { hidden: true },
  },
]

export default routes

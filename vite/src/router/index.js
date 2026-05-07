import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/Login.vue')
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../views/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/Index.vue'),
        meta: { title: '数据统计' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('../views/user/UserList.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'book',
        name: 'Book',
        component: () => import('../views/book/BookList.vue'),
        meta: { title: '图书管理' }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('../views/category/CategoryList.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'exchange',
        name: 'Exchange',
        component: () => import('../views/exchange/OrderList.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'report',
        name: 'Report',
        component: () => import('../views/report/ReportList.vue'),
        meta: { title: '举报管理' }
      },
      {
        path: 'announcement',
        name: 'Announcement',
        component: () => import('../views/announcement/List.vue'),
        meta: { title: '公告管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router

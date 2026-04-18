import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    redirect: () => {
      const token = localStorage.getItem('token')
      const roleKey = localStorage.getItem('roleKey')
      if (!token) return '/login'
      if (roleKey === 'admin') return '/admin/dashboard'
      if (roleKey === 'collector') return '/collector/workbench'
      return '/user/home'
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'UserRegister',
    component: () => import('@/views/register/UserRegister.vue'),
    meta: { title: '用户注册', public: true }
  },
  {
    path: '/collector-register',
    name: 'CollectorRegister',
    component: () => import('@/views/register/CollectorRegister.vue'),
    meta: { title: '回收员注册', public: true }
  },
  {
    path: '/collector-status-query',
    name: 'CollectorStatusQuery',
    component: () => import('@/views/collector/CollectorStatusQuery.vue'),
    meta: { title: '回收员审核状态', public: true }
  },
  {
    path: '/register-success',
    name: 'RegisterSuccess',
    component: () => import('@/views/register/RegisterSuccess.vue'),
    meta: { title: '注册成功', public: true }
  },
  {
    path: '/user',
    component: () => import('@/layout/UserLayout.vue'),
    meta: { requireAuth: true, roleKey: 'user' },
    children: [
      { path: '', redirect: '/user/home' },
      {
        path: 'home',
        name: 'UserHome',
        component: () => import('@/views/user/Home.vue'),
        meta: { title: '社区首页' }
      },
      {
        path: 'recycle',
        name: 'UserRecycle',
        component: () => import('@/views/user/Recycle.vue'),
        meta: { title: '预约回收' }
      },
      {
        path: 'orderList',
        name: 'UserOrderList',
        component: () => import('@/views/user/OrderList.vue'),
        meta: { title: '我的订单' }
      },
      {
        path: 'addressManage',
        name: 'UserAddress',
        component: () => import('@/views/user/AddressManage.vue'),
        meta: { title: '地址管理' }
      },
      {
        path: 'points',
        name: 'UserPointsMall',
        component: () => import('@/views/user/PointsMall.vue'),
        meta: { title: '积分商城' }
      },
      {
        path: 'ecoRank',
        name: 'UserEcoRank',
        component: () => import('@/views/user/EcoRank.vue'),
        meta: { title: '社区环保榜' }
      },
      {
        path: 'notice',
        name: 'UserNotice',
        component: () => import('@/views/user/Notice.vue'),
        meta: { title: '社区公告' }
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  {
    path: '/collector',
    component: () => import('@/layout/CollectorLayout.vue'),
    meta: { requireAuth: true, roleKey: 'collector' },
    children: [
      { path: '', redirect: '/collector/workbench' },
      {
        path: 'workbench',
        name: 'CollectorWorkbench',
        component: () => import('@/views/collector/Workbench.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'orderCenter',
        name: 'CollectorOrderCenter',
        component: () => import('@/views/collector/OrderCenter.vue'),
        meta: { title: '订单中心' }
      },
      {
        path: 'navigation',
        name: 'CollectorNavigation',
        component: () => import('@/views/collector/Navigation.vue'),
        meta: { title: '地图导航' }
      },
      {
        path: 'income',
        name: 'CollectorIncome',
        component: () => import('@/views/collector/Income.vue'),
        meta: { title: '收益统计' }
      },
      {
        path: 'profile',
        name: 'CollectorProfile',
        component: () => import('@/views/collector/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    meta: { requireAuth: true, roleKey: 'admin' },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据大屏' }
      },
      {
        path: 'dispatch',
        name: 'AdminDispatch',
        component: () => import('@/views/admin/Dispatch.vue'),
        meta: { title: '智能派单' }
      },
      {
        path: 'orderList',
        name: 'OrderManage',
        component: () => import('@/views/admin/OrderManage.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'collectors',
        name: 'CollectorManage',
        component: () => import('@/views/admin/CollectorManage.vue'),
        meta: { title: '回收员管理' }
      },
      {
        path: 'audit',
        name: 'CollectorAudit',
        component: () => import('@/views/admin/CollectorAudit.vue'),
        meta: { title: '资质审核' }
      },
      {
        path: 'appliance-types',
        name: 'ApplianceTypeManage',
        component: () => import('@/views/admin/ApplianceTypeManage.vue'),
        meta: { title: '品类管理' }
      },
      {
        path: 'points',
        name: 'PointsManage',
        component: () => import('@/views/admin/PointsManage.vue'),
        meta: { title: '积分管理' }
      },
      {
        path: 'notices',
        name: 'NoticeManage',
        component: () => import('@/views/admin/NoticeManage.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'statistics',
        name: 'AdminStatistics',
        component: () => import('@/views/admin/Statistics.vue'),
        meta: { title: '数据统计' }
      },
      {
        path: 'banner',
        name: 'Banner',
        component: () => import('@/views/admin/Banner.vue'),
        meta: { title: '轮播图管理' }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

const roleHomeMap = {
  user: '/user/home',
  collector: '/collector/workbench',
  admin: '/admin/dashboard'
}

router.beforeEach((to, from, next) => {
  document.title = to.meta?.title ? `${to.meta.title} - 社区回收系统` : '社区回收系统'

  if (to.meta?.public) {
    next()
    return
  }

  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }

  const roleKey = localStorage.getItem('roleKey')
  const targetRoleKey = to.meta?.roleKey
  if (roleKey && targetRoleKey && roleKey !== targetRoleKey) {
    next(roleHomeMap[roleKey] || '/login')
    return
  }

  next()
})

export default router

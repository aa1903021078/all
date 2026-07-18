import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { ElMessage } from 'element-plus'

const routes = [
  { path: '/login', component: () => import('@/views/Login.vue'), meta: { title: '登录' } },
  { path: '/register', component: () => import('@/views/Register.vue'), meta: { title: '注册' } },

  // 用户端
  {
    path: '/',
    component: () => import('@/layouts/UserLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('@/views/user/Home.vue'), meta: { title: '首页' } },
      { path: 'map', name: 'foodmap', component: () => import('@/views/user/FoodMap.vue'), meta: { title: '美食地图' } },
      { path: 'search', name: 'search', component: () => import('@/views/user/Search.vue'), meta: { title: '搜索' } },
      { path: 'shops/:id', name: 'shopDetail', component: () => import('@/views/user/ShopDetail.vue'), meta: { title: '店铺详情' } },
      { path: 'notes/:id', name: 'noteDetail', component: () => import('@/views/user/NoteDetail.vue'), meta: { title: '笔记详情' } },
      { path: 'recipes', name: 'recipes', component: () => import('@/views/user/RecipeList.vue'), meta: { title: '菜谱广场' } },
      { path: 'recipes/:id', name: 'recipeDetail', component: () => import('@/views/user/RecipeDetail.vue'), meta: { title: '菜谱详情' } },
      { path: 'publish', name: 'publish', component: () => import('@/views/user/Publish.vue'), meta: { title: '发布', requiresAuth: true } },
      { path: 'shopping', name: 'shopping', component: () => import('@/views/user/ShoppingList.vue'), meta: { title: '采购清单', requiresAuth: true } },
      { path: 'chat', name: 'chat', component: () => import('@/views/user/Chat.vue'), meta: { title: '消息', requiresAuth: true } },
      { path: 'profile', name: 'profile', component: () => import('@/views/user/Profile.vue'), meta: { title: '我的', requiresAuth: true } },
    ],
  },

  // 商家端
  {
    path: '/merchant',
    component: () => import('@/layouts/AdminLayout.vue'),
    props: { side: 'merchant' },
    meta: { requiresAuth: true, roles: ['MERCHANT'] },
    children: [
      { path: '', name: 'mDashboard', component: () => import('@/views/merchant/Dashboard.vue'), meta: { title: '商家看板' } },
      { path: 'shops', name: 'mShops', component: () => import('@/views/merchant/ShopManage.vue'), meta: { title: '店铺管理' } },
      { path: 'reservations', name: 'mReservations', component: () => import('@/views/merchant/Reservations.vue'), meta: { title: '预约管理' } },
      { path: 'reviews', name: 'mReviews', component: () => import('@/views/merchant/Reviews.vue'), meta: { title: '评价管理' } },
      { path: 'chat', name: 'mChat', component: () => import('@/views/merchant/Chat.vue'), meta: { title: '客户消息' } },
    ],
  },

  // 后台端
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    props: { side: 'admin' },
    meta: { requiresAuth: true, adminAccess: true },
    children: [
      { path: '', name: 'aDashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '控制台' } },
      { path: 'shops', name: 'aShops', component: () => import('@/views/admin/ShopAudit.vue'), meta: { title: '店铺管理', perm: 'shop:manage' } },
      { path: 'content', name: 'aContent', component: () => import('@/views/admin/ContentReview.vue'), meta: { title: '内容审核', perm: 'content:review' } },
      { path: 'users', name: 'aUsers', component: () => import('@/views/admin/Users.vue'), meta: { title: '用户管理', perm: 'user:manage' } },
      { path: 'operations', name: 'aOperations', component: () => import('@/views/admin/Operations.vue'), meta: { title: '运营管理', perm: 'operation:manage' } },
      { path: 'screen', name: 'aScreen', component: () => import('@/views/admin/BigScreen.vue'), meta: { title: '数据大屏', perm: 'dashboard:view' } },
      { path: 'config', name: 'aConfig', component: () => import('@/views/admin/SystemConfig.vue'), meta: { title: '系统配置', perm: 'config:manage' } },
    ],
  },

  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  const requiresAuth = to.matched.some((r) => r.meta && r.meta.requiresAuth)
  if (requiresAuth && !auth.isLoggedIn) {
    ElMessage.warning('请先登录')
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  // 角色校验
  const roleRoute = to.matched.find((r) => r.meta && r.meta.roles)
  if (roleRoute && !auth.isAdmin) {
    const ok = roleRoute.meta.roles.some((role) => auth.hasRole(role))
    if (!ok) {
      ElMessage.error('无权限访问')
      return next('/')
    }
  }
  // 后台访问校验
  const adminRoute = to.matched.find((r) => r.meta && r.meta.adminAccess)
  if (adminRoute && !auth.canAccessAdmin()) {
    ElMessage.error('无后台访问权限')
    return next('/')
  }
  // 具体权限点校验
  if (to.meta && to.meta.perm && !auth.isAdmin && !auth.hasPerm(to.meta.perm)) {
    ElMessage.error('无权限访问该模块')
    return next(false)
  }
  next()
})

export default router

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layouts/UserLayout.vue'),
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', name: 'Home', component: () => import('@/views/Home.vue') },
      { path: 'books', name: 'BookList', component: () => import('@/views/BookList.vue') },
      { path: 'books/:id', name: 'BookDetail', component: () => import('@/views/BookDetail.vue') },
      { path: 'books/:bookId/read/:chapterId', name: 'Reader', component: () => import('@/views/Reader.vue') },
      { path: 'items', name: 'ItemList', component: () => import('@/views/ItemList.vue') },
      { path: 'music', name: 'Music', component: () => import('@/views/Music.vue') },
      { path: 'announcements', name: 'Announcements', component: () => import('@/views/Announcements.vue') },
      { path: 'user-center', name: 'UserCenter', component: () => import('@/views/UserCenter.vue') },
      { path: 'my-orders', name: 'MyOrders', component: () => import('@/views/MyOrders.vue') },
      { path: 'favorites', name: 'Favorites', component: () => import('@/views/Favorites.vue') }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'users', component: () => import('@/views/admin/UsersManage.vue') },
      { path: 'books', component: () => import('@/views/admin/BooksManage.vue') },
      { path: 'announcements', component: () => import('@/views/admin/AnnouncementsManage.vue') },
      { path: 'items', component: () => import('@/views/admin/ItemsManage.vue') },
      { path: 'orders', component: () => import('@/views/admin/OrdersManage.vue') },
      { path: 'songs', component: () => import('@/views/admin/SongsManage.vue') },
      { path: 'comments', component: () => import('@/views/admin/CommentsManage.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const user = useUserStore()
  if (to.meta.public) return next()
  if (!user.isLogin) return next('/login')
  if (to.matched.some(r => r.meta.requiresAdmin) && !user.isAdmin) return next('/home')
  next()
})

export default router

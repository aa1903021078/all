import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: () => import('@/views/auth/Login.vue'), meta: { public: true } },
  { path: '/register', component: () => import('@/views/auth/Register.vue'), meta: { public: true } },

  {
    path: '/patient',
    component: () => import('@/layout/AppLayout.vue'),
    meta: { roles: ['PATIENT'] },
    children: [
      { path: '', redirect: '/patient/dashboard' },
      { path: 'dashboard', component: () => import('@/views/patient/Dashboard.vue') },
      { path: 'book', component: () => import('@/views/patient/Book.vue') },
      { path: 'appointments', component: () => import('@/views/patient/Appointments.vue') },
      { path: 'records', component: () => import('@/views/patient/Records.vue') },
      { path: 'referrals', component: () => import('@/views/patient/Referrals.vue') },
      { path: 'notifications', component: () => import('@/views/patient/Notifications.vue') }
    ]
  },

  {
    path: '/doctor',
    component: () => import('@/layout/AppLayout.vue'),
    meta: { roles: ['DOCTOR'] },
    children: [
      { path: '', redirect: '/doctor/workbench' },
      { path: 'workbench', component: () => import('@/views/doctor/Workbench.vue') },
      { path: 'visit/:appointmentId', component: () => import('@/views/doctor/Visit.vue'), props: true },
      { path: 'schedules', component: () => import('@/views/doctor/Schedules.vue') },
      { path: 'referrals', component: () => import('@/views/doctor/Referrals.vue') }
    ]
  },

  {
    path: '/admin',
    component: () => import('@/layout/AppLayout.vue'),
    meta: { roles: ['ADMIN'] },
    children: [
      { path: '', redirect: '/admin/overview' },
      { path: 'overview', component: () => import('@/views/admin/Overview.vue') },
      { path: 'hospitals', component: () => import('@/views/admin/Hospitals.vue') },
      { path: 'departments', component: () => import('@/views/admin/Departments.vue') },
      { path: 'users', component: () => import('@/views/admin/Users.vue') },
      { path: 'schedules', component: () => import('@/views/admin/Schedules.vue') },
      { path: 'referrals', component: () => import('@/views/admin/Referrals.vue') },
      { path: 'logs', component: () => import('@/views/admin/Logs.vue') }
    ]
  },

  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({ history: createWebHashHistory(), routes })

router.beforeEach((to) => {
  const us = useUserStore()
  if (to.meta.public) return true
  if (!us.isLogin) return { path: '/login' }
  if (to.meta.roles && !to.meta.roles.includes(us.role)) return { path: us.homePath }
  return true
})

export default router

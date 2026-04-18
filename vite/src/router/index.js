import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../views/Layout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'Home', component: () => import('../views/Home.vue'), meta: { title: '首页' } },
      // Admin routes
      { path: 'admin/users', name: 'AdminUsers', component: () => import('../views/admin/UserManage.vue'), meta: { title: '用户管理', role: 0 } },
      { path: 'admin/services', name: 'AdminServices', component: () => import('../views/admin/ServiceManage.vue'), meta: { title: '服务项目管理', role: 0 } },
      { path: 'admin/packages', name: 'AdminPackages', component: () => import('../views/admin/PackageManage.vue'), meta: { title: '套餐管理', role: 0 } },
      { path: 'admin/orders', name: 'AdminOrders', component: () => import('../views/admin/OrderManage.vue'), meta: { title: '订单管理', role: 0 } },
      { path: 'admin/staff-arrange', name: 'AdminStaffArrange', component: () => import('../views/admin/StaffArrange.vue'), meta: { title: '护理人员安排', role: 0 } },
      { path: 'admin/care-records', name: 'AdminCareRecords', component: () => import('../views/admin/CareRecordView.vue'), meta: { title: '护理记录查看', role: 0 } },
      { path: 'admin/evaluations', name: 'AdminEvaluations', component: () => import('../views/admin/EvaluationManage.vue'), meta: { title: '评价管理', role: 0 } },
      { path: 'admin/complaints', name: 'AdminComplaints', component: () => import('../views/admin/ComplaintManage.vue'), meta: { title: '投诉处理', role: 0 } },
      { path: 'admin/staff-change', name: 'AdminStaffChange', component: () => import('../views/admin/StaffChangeManage.vue'), meta: { title: '人员更换管理', role: 0 } },
      // Staff routes - 月嫂
      { path: 'staff/life-care', name: 'StaffLifeCare', component: () => import('../views/staff/LifeCareRecord.vue'), meta: { title: '生活护理记录', role: 1 } },
      // Staff routes - 营养师
      { path: 'staff/diet-plan', name: 'StaffDietPlan', component: () => import('../views/staff/DietPlanManage.vue'), meta: { title: '饮食方案制定', role: 2 } },
      // Staff routes - 护理人员
      { path: 'staff/medical-care', name: 'StaffMedicalCare', component: () => import('../views/staff/MedicalCareRecord.vue'), meta: { title: '医疗护理记录', role: 3 } },
      // Customer routes
      { path: 'customer/services', name: 'CustomerServices', component: () => import('../views/customer/ServiceBrowse.vue'), meta: { title: '服务浏览', role: 4 } },
      { path: 'customer/packages', name: 'CustomerPackages', component: () => import('../views/customer/PackageBrowse.vue'), meta: { title: '套餐浏览', role: 4 } },
      { path: 'customer/orders', name: 'CustomerOrders', component: () => import('../views/customer/MyOrders.vue'), meta: { title: '我的订单', role: 4 } },
      { path: 'customer/care-records', name: 'CustomerCareRecords', component: () => import('../views/customer/MyCareRecords.vue'), meta: { title: '护理记录查看', role: 4 } },
      { path: 'customer/evaluations', name: 'CustomerEvaluations', component: () => import('../views/customer/MyEvaluations.vue'), meta: { title: '评价', role: 4 } },
      { path: 'customer/complaints', name: 'CustomerComplaints', component: () => import('../views/customer/MyComplaints.vue'), meta: { title: '投诉', role: 4 } },
      { path: 'customer/staff-change', name: 'CustomerStaffChange', component: () => import('../views/customer/StaffChangeRequest.vue'), meta: { title: '工作人员更换', role: 4 } },
      { path: 'customer/profile', name: 'CustomerProfile', component: () => import('../views/customer/Profile.vue'), meta: { title: '个人中心', role: 4 } },
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (to.path !== '/login' && to.path !== '/register' && !user) {
    next('/login')
  } else {
    next()
  }
})

export default router

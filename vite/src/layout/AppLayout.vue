<template>
  <el-container class="h-screen">
    <el-aside width="220px" class="bg-slate-900 text-white">
      <div class="flex items-center justify-center py-5 text-lg font-semibold tracking-wide border-b border-slate-700">
        <el-icon class="mr-2"><FirstAidKit /></el-icon>
        双向转诊系统
      </div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#0f172a"
        text-color="#cbd5e1"
        active-text-color="#38bdf8"
        class="!border-0"
      >
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="!h-14 flex items-center justify-between bg-white shadow-sm px-6">
        <div class="text-slate-700 font-medium">
          {{ currentTitle }}
        </div>
        <el-dropdown trigger="click">
          <span class="cursor-pointer flex items-center">
            <el-avatar :size="30" class="mr-2">
              {{ user?.realName?.[0] || user?.username?.[0] || 'U' }}
            </el-avatar>
            <span class="text-slate-700">{{ user?.realName || user?.username }}</span>
            <span class="ml-2 text-xs text-slate-400">{{ roleLabel }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="bg-slate-50 p-6 overflow-auto">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const us = useUserStore()
const user = computed(() => us.user)

const roleLabel = computed(() => ({ PATIENT: '患者', DOCTOR: '医生', ADMIN: '管理员' })[us.role] || '')

const menusByRole = {
  PATIENT: [
    { path: '/patient/dashboard', title: '工作台', icon: 'House' },
    { path: '/patient/book', title: '在线挂号', icon: 'Calendar' },
    { path: '/patient/appointments', title: '我的预约', icon: 'Tickets' },
    { path: '/patient/records', title: '就诊记录', icon: 'Document' },
    { path: '/patient/referrals', title: '转诊跟踪', icon: 'Connection' },
    { path: '/patient/notifications', title: '站内消息', icon: 'BellFilled' }
  ],
  DOCTOR: [
    { path: '/doctor/workbench', title: '接诊工作台', icon: 'List' },
    { path: '/doctor/schedules', title: '排班管理', icon: 'Calendar' },
    { path: '/doctor/referrals', title: '双向转诊', icon: 'Connection' }
  ],
  ADMIN: [
    { path: '/admin/overview', title: '运营总览', icon: 'DataAnalysis' },
    { path: '/admin/hospitals', title: '机构管理', icon: 'OfficeBuilding' },
    { path: '/admin/departments', title: '科室管理', icon: 'Menu' },
    { path: '/admin/users', title: '用户与权限', icon: 'User' },
    { path: '/admin/schedules', title: '排班调配', icon: 'Calendar' },
    { path: '/admin/referrals', title: '转诊监管', icon: 'Connection' },
    { path: '/admin/logs', title: '操作日志', icon: 'Tickets' }
  ]
}

const menus = computed(() => menusByRole[us.role] || [])
const currentTitle = computed(() => {
  const m = menus.value.find(x => route.path.startsWith(x.path))
  return m ? m.title : ''
})

function logout() {
  us.logout()
  router.push('/login')
}
</script>

<template>
  <div class="admin-layout">
    <aside class="sidebar" :class="{ collapsed }">
      <div class="brand" @click="go(homePath)">
        <span class="brand-icon">{{ side === 'admin' ? '🛡️' : '🏪' }}</span>
        <span v-show="!collapsed" class="brand-text">{{ title }}</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="side-menu"
        :collapse="collapsed"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#ff6a3d"
        router
      >
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon>
          <template #title>{{ m.title }}</template>
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="main">
      <header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn pointer" @click="collapsed = !collapsed">
            <Fold v-if="!collapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>{{ title }}</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-button text @click="go('/')">
            <el-icon><HomeFilled /></el-icon>&nbsp;返回前台
          </el-button>
          <el-dropdown @command="onCommand">
            <span class="user-chip">
              <el-avatar :size="30" :src="auth.user?.avatar" />
              <span class="nick">{{ auth.user?.nickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile"><el-icon><User /></el-icon>个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="view">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'

const props = defineProps({
  side: { type: String, default: 'admin' },
})

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const collapsed = ref(false)

const merchantMenus = [
  { path: '/merchant', title: '商家看板', icon: 'Odometer' },
  { path: '/merchant/shops', title: '店铺管理', icon: 'Shop' },
  { path: '/merchant/reservations', title: '预约管理', icon: 'Calendar' },
  { path: '/merchant/reviews', title: '评价管理', icon: 'ChatLineSquare' },
  { path: '/merchant/chat', title: '客户消息', icon: 'ChatDotRound' },
]

const adminMenus = [
  { path: '/admin', title: '控制台', icon: 'Odometer', perm: null },
  { path: '/admin/shops', title: '店铺管理', icon: 'Shop', perm: 'shop:manage' },
  { path: '/admin/content', title: '内容审核', icon: 'Document', perm: 'content:review' },
  { path: '/admin/users', title: '用户管理', icon: 'UserFilled', perm: 'user:manage' },
  { path: '/admin/operations', title: '运营管理', icon: 'TrendCharts', perm: 'operation:manage' },
  { path: '/admin/screen', title: '数据大屏', icon: 'DataLine', perm: 'dashboard:view' },
  { path: '/admin/config', title: '系统配置', icon: 'Setting', perm: 'config:manage' },
]

const title = computed(() => (props.side === 'admin' ? '运营后台' : '商家中心'))
const homePath = computed(() => (props.side === 'admin' ? '/admin' : '/merchant'))

const menus = computed(() => {
  if (props.side === 'merchant') return merchantMenus
  return adminMenus.filter((m) => !m.perm || auth.isAdmin || auth.hasPerm(m.perm))
})

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => (route.meta && route.meta.title) || '')

function go(path) {
  router.push(path)
}
function onCommand(cmd) {
  if (cmd === 'logout') {
    auth.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
    return
  }
  if (cmd === 'profile') router.push('/profile')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}
.sidebar {
  width: 210px;
  background: #304156;
  transition: width 0.28s;
  flex-shrink: 0;
}
.sidebar.collapsed {
  width: 64px;
}
.brand {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-weight: 700;
  font-size: 17px;
  cursor: pointer;
  background: #2b3a4d;
}
.brand-icon {
  font-size: 22px;
}
.side-menu {
  border-right: none;
  height: calc(100vh - 60px);
}
.side-menu:not(.el-menu--collapse) {
  width: 210px;
}
.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--bg);
}
.header {
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.collapse-btn {
  font-size: 20px;
  color: #5a5e66;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  outline: none;
}
.nick {
  font-weight: 500;
}
.view {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="brand">管理员端</div>
      <router-link v-for="m in menus" :key="m.path" :to="m.path" class="menu-item">
        {{ m.name }}
      </router-link>
    </aside>

    <main class="main">
      <header class="topbar">
        <div>
          <strong>{{ $route.meta.title || '管理后台' }}</strong>
        </div>
        <el-dropdown>
          <span class="user-trigger">
            <el-avatar :size="32" :src="adminInfo.avatar || '/default-avatar.png'" />
            <span>{{ adminInfo.name || adminInfo.username || '管理员' }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$router.push('/admin/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <section class="content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const adminInfo = ref(JSON.parse(localStorage.getItem('user') || '{}'))

const menus = [
  { path: '/admin/dashboard', name: '数据大屏' },
  { path: '/admin/dispatch', name: '智能派单' },
  { path: '/admin/notices', name: '公告管理' },
  { path: '/admin/statistics', name: '数据统计' },
  { path: '/admin/orderList', name: '订单管理' },
  { path: '/admin/users', name: '用户管理' },
  { path: '/admin/collectors', name: '回收员管理' },
  { path: '/admin/audit', name: '资质审核' },
  { path: '/admin/appliance-types', name: '品类管理' },
  { path: '/admin/points', name: '积分管理' },
  { path: '/admin/profile', name: '个人中心' }
]

const logout = async () => {
  try {
    await ElMessageBox.confirm('确认退出登录？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('role')
    localStorage.removeItem('roleKey')
    router.push('/login')
  } catch (_) {
    // ignore cancel
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: flex;
  background: #f8fafc;
}

.sidebar {
  width: 240px;
  background: #111827;
  color: #fff;
  padding: 16px;
  overflow-y: auto;
}

.brand {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 16px;
}

.menu-item {
  display: block;
  color: #d1d5db;
  text-decoration: none;
  padding: 10px 12px;
  border-radius: 8px;
  margin-bottom: 4px;
}

.menu-item.router-link-active {
  background: #1f2937;
  color: #fff;
}

.main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.topbar {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.content {
  padding: 20px 16px;
}
</style>

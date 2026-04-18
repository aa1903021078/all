<template>
  <div class="collector-layout">
    <aside class="sidebar">
      <div class="brand">回收员端</div>
      <router-link v-for="m in menus" :key="m.path" :to="m.path" class="menu-item">
        {{ m.name }}
        <span v-if="m.path === '/collector/orderCenter' && pendingCount > 0" class="badge">{{ pendingCount }}</span>
      </router-link>
      <div class="work-switch">
        <span>{{ isWorking ? '接单中' : '休息中' }}</span>
        <el-switch v-model="isWorking" @change="toggleStatus" />
      </div>
    </aside>

    <main class="main">
      <header class="topbar">
        <div>
          <strong>{{ $route.meta.title || '回收员端' }}</strong>
        </div>
        <el-dropdown>
          <span class="user-trigger">
            <el-avatar :size="32" :src="user.avatar || '/default-avatar.png'" />
            <span>{{ user.name || user.username || '回收员' }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$router.push('/collector/profile')">个人中心</el-dropdown-item>
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
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const isWorking = ref((user.value?.workStatus || 0) === 1)
const pendingCount = ref(0)
let timer = null

const menus = [
  { path: '/collector/workbench', name: '工作台' },
  { path: '/collector/orderCenter', name: '订单中心' },
  { path: '/collector/navigation', name: '地图导航' },
  { path: '/collector/income', name: '收益统计' },
  { path: '/collector/profile', name: '个人中心' }
]

const loadPendingCount = async () => {
  try {
    const res = await request.get(`/recycleOrder/selectByCollector/${user.value.id}`)
    const list = res?.data || []
    pendingCount.value = list.filter(item => item.status === 1).length
  } catch (_) {
    pendingCount.value = 0
  }
}

const toggleStatus = async (val) => {
  try {
    await request.put('/collector/updateWorkStatus', null, {
      params: { id: user.value.id, workStatus: val ? 1 : 0 }
    })
    user.value.workStatus = val ? 1 : 0
    localStorage.setItem('user', JSON.stringify(user.value))
    ElMessage.success(val ? '已切换为接单中' : '已切换为休息中')
  } catch (error) {
    isWorking.value = !val
    ElMessage.error(error?.message || '状态切换失败')
  }
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('role')
  localStorage.removeItem('roleKey')
  router.push('/login')
}

onMounted(() => {
  loadPendingCount()
  timer = setInterval(loadPendingCount, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.collector-layout {
  min-height: 100vh;
  display: flex;
  background: #f8fafc;
}

.sidebar {
  width: 220px;
  background: #0f172a;
  color: #fff;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.brand {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 16px;
}

.menu-item {
  color: #cbd5e1;
  text-decoration: none;
  padding: 10px 12px;
  border-radius: 8px;
  margin-bottom: 4px;
  display: flex;
  justify-content: space-between;
}

.menu-item.router-link-active {
  background: #1d4ed8;
  color: #fff;
}

.badge {
  background: #ef4444;
  border-radius: 12px;
  padding: 0 8px;
  font-size: 12px;
}

.work-switch {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  color: #e2e8f0;
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

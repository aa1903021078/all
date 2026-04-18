<template>
  <div class="user-layout">
    <header class="user-header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/user/home')">
          <span class="logo-text">社区回收</span>
        </div>

        <nav class="main-nav">
          <router-link to="/user/home">首页</router-link>
          <router-link to="/user/recycle">预约回收</router-link>
          <router-link to="/user/orderList">我的订单</router-link>
          <router-link to="/user/points">积分商城</router-link>
          <router-link to="/user/ecoRank">社区环保榜</router-link>
          <router-link to="/user/notice">社区公告</router-link>
        </nav>

        <el-dropdown v-if="user?.id">
          <span class="user-trigger">
            <el-avatar :size="32" :src="user.avatar || '/default-avatar.png'" />
            <span>{{ user.name || user.username || '用户' }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$router.push('/user/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item @click="$router.push('/user/addressManage')">地址管理</el-dropdown-item>
              <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <main class="main-content">
      <router-view />
    </main>

    <footer class="user-footer">
      <div class="footer-content">
        <span>社区共建绿色回收</span>
        <span>累计回收 {{ totalWeight }} kg</span>
        <span>累计减排 {{ totalCarbon }} kg</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const totalWeight = ref(0)
const totalCarbon = ref(0)

const loadOverview = async () => {
  try {
    const res = await request.get('/recycleOrder/statistics')
    const data = res?.data || {}
    totalWeight.value = Number(data.totalWeight || 0)
    totalCarbon.value = Number(data.totalCarbonSaved || 0)
  } catch (_) {
    totalWeight.value = 0
    totalCarbon.value = 0
  }
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('role')
  localStorage.removeItem('roleKey')
  ElMessage.success('已退出登录')
  router.push('/login')
}

onMounted(loadOverview)
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f6f8fa;
}

.user-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.header-content {
  max-width: 1200px;
  height: 64px;
  margin: 0 auto;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  cursor: pointer;
  font-weight: 700;
  color: #059669;
}

.main-nav {
  display: flex;
  gap: 18px;
}

.main-nav a {
  color: #374151;
  text-decoration: none;
  font-size: 14px;
}

.main-nav a.router-link-active {
  color: #059669;
  font-weight: 600;
}

.user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.main-content {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px 16px;
}

.user-footer {
  border-top: 1px solid #e5e7eb;
  background: #fff;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 54px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #6b7280;
  font-size: 13px;
}
</style>

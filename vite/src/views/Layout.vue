<template>
  <el-container class="layout-container">
    <!-- Sidebar -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="sidebar-logo" :class="{ 'is-collapse': isCollapse }">
        <el-icon :size="22" color="#409eff"><House /></el-icon>
        <span v-show="!isCollapse" class="logo-text">月子中心管理系统</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
          router
        >
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页</template>
          </el-menu-item>

          <!-- Admin menus (role=0) -->
          <template v-if="role === 0">
            <el-menu-item index="/admin/users">
              <el-icon><User /></el-icon>
              <template #title>用户管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/services">
              <el-icon><Grid /></el-icon>
              <template #title>服务项目管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/packages">
              <el-icon><Box /></el-icon>
              <template #title>套餐管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/orders">
              <el-icon><Document /></el-icon>
              <template #title>订单管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/staff-arrange">
              <el-icon><UserFilled /></el-icon>
              <template #title>护理人员安排</template>
            </el-menu-item>
            <el-menu-item index="/admin/care-records">
              <el-icon><Notebook /></el-icon>
              <template #title>护理记录查看</template>
            </el-menu-item>
            <el-menu-item index="/admin/evaluations">
              <el-icon><Star /></el-icon>
              <template #title>评价管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/complaints">
              <el-icon><ChatDotSquare /></el-icon>
              <template #title>投诉处理</template>
            </el-menu-item>
            <el-menu-item index="/admin/staff-change">
              <el-icon><Switch /></el-icon>
              <template #title>人员更换管理</template>
            </el-menu-item>
          </template>

          <!-- 月嫂 menus (role=1) -->
          <template v-if="role === 1">
            <el-menu-item index="/staff/life-care">
              <el-icon><FirstAidKit /></el-icon>
              <template #title>生活护理记录</template>
            </el-menu-item>
          </template>

          <!-- 营养师 menus (role=2) -->
          <template v-if="role === 2">
            <el-menu-item index="/staff/diet-plan">
              <el-icon><Bowl /></el-icon>
              <template #title>饮食方案制定</template>
            </el-menu-item>
          </template>

          <!-- 护理人员 menus (role=3) -->
          <template v-if="role === 3">
            <el-menu-item index="/staff/medical-care">
              <el-icon><FirstAidKit /></el-icon>
              <template #title>医疗护理记录</template>
            </el-menu-item>
          </template>

          <!-- Customer menus (role=4) -->
          <template v-if="role === 4">
            <el-menu-item index="/customer/services">
              <el-icon><Grid /></el-icon>
              <template #title>服务浏览</template>
            </el-menu-item>
            <el-menu-item index="/customer/packages">
              <el-icon><Box /></el-icon>
              <template #title>套餐浏览</template>
            </el-menu-item>
            <el-menu-item index="/customer/orders">
              <el-icon><Document /></el-icon>
              <template #title>我的订单</template>
            </el-menu-item>
            <el-menu-item index="/customer/care-records">
              <el-icon><Notebook /></el-icon>
              <template #title>护理记录查看</template>
            </el-menu-item>
            <el-menu-item index="/customer/evaluations">
              <el-icon><Star /></el-icon>
              <template #title>评价</template>
            </el-menu-item>
            <el-menu-item index="/customer/complaints">
              <el-icon><ChatDotSquare /></el-icon>
              <template #title>投诉</template>
            </el-menu-item>
            <el-menu-item index="/customer/staff-change">
              <el-icon><Switch /></el-icon>
              <template #title>工作人员更换</template>
            </el-menu-item>
            <el-menu-item index="/customer/profile">
              <el-icon><Setting /></el-icon>
              <template #title>个人中心</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <!-- Main area -->
    <el-container class="layout-main-wrapper">
      <!-- Header -->
      <el-header class="layout-header" height="50px">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="welcome-text">
            <el-icon><UserFilled /></el-icon>
            欢迎，{{ displayName }}
          </span>
          <el-button type="danger" text @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出
          </el-button>
        </div>
      </el-header>

      <!-- Content -->
      <el-main class="layout-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessageBox, ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const store = useStore()
const isCollapse = ref(false)

const role = computed(() => store.getters.userRole)
const user = computed(() => store.getters.user)

const displayName = computed(() => {
  return user.value?.realName || user.value?.username || '用户'
})

const activeMenu = computed(() => route.path)

const currentTitle = computed(() => {
  return route.meta?.title || ''
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleLogout = () => {
  ElMessageBox.confirm('确认退出登录？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    store.dispatch('logout')
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background-color: #304156;
  transition: width 0.28s;
  overflow: hidden;
}

.sidebar-logo {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background-color: #263445;
  border-bottom: 1px solid #1f2d3d;
  overflow: hidden;
  white-space: nowrap;
}

.sidebar-logo.is-collapse .logo-text {
  display: none;
}

.logo-text {
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1px;
}

.layout-main-wrapper {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 1;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.welcome-text {
  font-size: 14px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 4px;
}

.layout-content {
  background: #f0f2f5;
  overflow-y: auto;
}

/* Override Element Plus menu styles in sidebar */
.layout-aside :deep(.el-menu) {
  border-right: none;
}

.layout-aside :deep(.el-menu-item) {
  height: 46px;
  line-height: 46px;
}

.layout-aside :deep(.el-menu-item:hover) {
  background-color: #263445 !important;
}

.layout-aside :deep(.el-menu-item.is-active) {
  background-color: #263445 !important;
}
</style>

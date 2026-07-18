<template>
  <div class="user-layout">
    <header class="topbar">
      <div class="topbar-inner">
        <div class="logo pointer" @click="go('/')">
          <span class="logo-icon">🍜</span>
          <span class="logo-text">食光探店</span>
        </div>
        <nav class="nav">
          <router-link to="/" class="nav-item" active-class="active" exact>首页</router-link>
          <router-link to="/map" class="nav-item" active-class="active">美食地图</router-link>
          <router-link to="/recipes" class="nav-item" active-class="active">菜谱广场</router-link>
        </nav>
        <div class="search-box">
          <el-input
            v-model="keyword"
            placeholder="搜索店铺 / 菜谱 / 食材"
            @keyup.enter="doSearch"
            clearable
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="actions">
          <el-button type="primary" plain @click="go('/publish')">
            <el-icon><EditPen /></el-icon>&nbsp;发布
          </el-button>
          <template v-if="auth.isLoggedIn">
            <el-dropdown @command="onCommand">
              <span class="user-chip">
                <el-avatar :size="30" :src="auth.user?.avatar" />
                <span class="nick">{{ auth.user?.nickname }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile"><el-icon><User /></el-icon>我的主页</el-dropdown-item>
                  <el-dropdown-item command="shopping"><el-icon><ShoppingCart /></el-icon>采购清单</el-dropdown-item>
                  <el-dropdown-item command="chat"><el-icon><ChatDotRound /></el-icon>我的消息</el-dropdown-item>
                  <el-dropdown-item v-if="auth.isMerchant" command="merchant" divided>
                    <el-icon><Shop /></el-icon>商家中心
                  </el-dropdown-item>
                  <el-dropdown-item v-if="auth.canAccessAdmin()" command="admin">
                    <el-icon><Setting /></el-icon>后台管理
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button text @click="go('/login')">登录</el-button>
            <el-button type="primary" @click="go('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="content">
      <router-view />
    </main>

    <footer class="footer">
      <div>食光探店 · 美食探店 &amp; 菜谱分享平台 — 课程演示项目</div>
      <div class="muted text-sm">线下探店消费闭环 · 居家菜谱制作闭环</div>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const auth = useAuthStore()
const keyword = ref('')

function go(path) {
  router.push(path)
}
function doSearch() {
  if (!keyword.value.trim()) return
  router.push({ path: '/search', query: { keyword: keyword.value.trim() } })
}
function onCommand(cmd) {
  if (cmd === 'logout') {
    auth.logout()
    ElMessage.success('已退出登录')
    router.push('/')
    return
  }
  if (cmd === 'merchant') return router.push('/merchant')
  if (cmd === 'admin') return router.push('/admin')
  router.push('/' + cmd)
}
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
}
.topbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 62px;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 0 16px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 800;
  font-size: 20px;
  color: var(--brand);
}
.logo-icon {
  font-size: 26px;
}
.nav {
  display: flex;
  gap: 4px;
}
.nav-item {
  padding: 6px 14px;
  border-radius: 8px;
  color: #606266;
  font-weight: 500;
}
.nav-item.active,
.nav-item:hover {
  color: var(--brand);
  background: var(--el-color-primary-light-9);
}
.search-box {
  flex: 1;
  max-width: 360px;
}
.actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
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
  color: #303133;
}
.content {
  flex: 1;
}
.footer {
  text-align: center;
  padding: 24px;
  color: #909399;
  border-top: 1px solid #ebeef5;
  background: #fff;
}
</style>

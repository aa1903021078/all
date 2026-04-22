<template>
  <div class="min-h-screen flex flex-col" :class="{ 'reader-night': readerNight }">
    <!-- 顶栏 -->
    <header class="flex items-center justify-between px-6 h-[60px] bg-white shadow-sm sticky top-0 z-20">
      <div class="flex items-center gap-8">
        <router-link to="/home" class="text-2xl font-bold" style="color: var(--yq-primary)">
          阅趣阁
        </router-link>
        <nav class="flex gap-6 text-sm">
          <router-link to="/home" class="hover:text-red-600">首页</router-link>
          <router-link to="/books" class="hover:text-red-600">图书</router-link>
          <router-link to="/items" class="hover:text-red-600">商品</router-link>
          <router-link to="/music" class="hover:text-red-600">音乐</router-link>
          <router-link to="/announcements" class="hover:text-red-600">公告</router-link>
          <router-link to="/chat" class="hover:text-red-600">消息</router-link>
          <router-link to="/ai" class="hover:text-red-600">AI</router-link>
          <router-link to="/support" class="hover:text-red-600">客服</router-link>
          <router-link to="/favorites" class="hover:text-red-600">我的收藏</router-link>
          <router-link to="/my-orders" class="hover:text-red-600">我的订单</router-link>
        </nav>
      </div>
      <div class="flex items-center gap-3">
        <router-link v-if="userStore.isAdmin" to="/admin" class="text-sm text-red-600">后台管理</router-link>
        <el-dropdown v-if="userStore.isLogin" @command="onCommand">
          <div class="flex items-center gap-2 cursor-pointer">
            <el-avatar :size="32" :src="userStore.user?.avatar || defaultAvatar" />
            <span>{{ userStore.user?.username }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button v-else type="primary" size="small" @click="$router.push('/login')">登录</el-button>
      </div>
    </header>

    <!-- 内容 -->
    <main class="flex-1">
      <router-view />
    </main>

    <footer class="text-center text-xs text-gray-400 py-4">
      © 2026 阅趣阁 · 图书阅读系统
    </footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useReaderStore } from '@/store/reader'

const router = useRouter()
const userStore = useUserStore()
const readerStore = useReaderStore()
const readerNight = computed(() => readerStore.night)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

function onCommand(cmd) {
  if (cmd === 'profile') router.push('/user-center')
  else if (cmd === 'logout') userStore.logout().then(() => router.push('/login'))
}
</script>

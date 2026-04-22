<template>
  <el-container class="h-screen">
    <el-aside width="220px" class="bg-[#1f1f1f] text-white">
      <div class="h-[60px] flex items-center justify-center text-xl font-bold" style="color: var(--yq-primary)">
        阅趣阁 · 后台
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#1f1f1f"
        text-color="#ccc"
        active-text-color="#ffffff"
        class="border-none">
        <el-menu-item index="/admin/dashboard">数据概览</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
        <el-menu-item index="/admin/books">图书/章节管理</el-menu-item>
        <el-menu-item index="/admin/announcements">公告管理</el-menu-item>
        <el-menu-item index="/admin/items">商品管理</el-menu-item>
        <el-menu-item index="/admin/orders">订单管理</el-menu-item>
        <el-menu-item index="/admin/songs">音乐管理</el-menu-item>
        <el-menu-item index="/admin/comments">评论管理</el-menu-item>
        <el-menu-item index="/admin/support">客服会话</el-menu-item>
        <el-menu-item index="/admin/bigscreen">数据大屏</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="flex items-center justify-between bg-white shadow-sm px-6">
        <div>
          <router-link to="/home" class="text-sm text-gray-500">← 返回前台</router-link>
        </div>
        <div class="flex items-center gap-3">
          <span class="text-sm">{{ user.username }}（{{ user.role }}）</span>
          <el-button size="small" @click="onLogout">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.user || {})
function onLogout() {
  userStore.logout().then(() => router.push('/login'))
}
</script>

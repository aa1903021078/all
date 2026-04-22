<template>
  <div class="yq-page max-w-[1200px] mx-auto">
    <!-- Hero -->
    <div class="rounded-lg overflow-hidden mb-6"
         style="background: linear-gradient(135deg, #c20c0c 0%, #1f1f1f 100%); color: #fff; padding: 40px;">
      <div class="text-3xl font-bold mb-2">欢迎来到阅趣阁</div>
      <div class="opacity-80">沉浸式阅读体验 · 精品图书推荐 · 放松音乐</div>
      <div class="mt-4 flex gap-8 text-sm">
        <div><span class="text-3xl font-bold mr-2">{{ statsData.bookTotal || 0 }}</span>本图书</div>
        <div><span class="text-3xl font-bold mr-2">{{ statsData.chapterTotal || 0 }}</span>篇章节</div>
      </div>
    </div>

    <!-- 快速入口 -->
    <div class="grid grid-cols-4 gap-4 mb-6">
      <div v-for="q in quick" :key="q.path"
           class="yq-card text-center cursor-pointer hover:shadow-lg transition"
           @click="$router.push(q.path)">
        <div class="text-3xl mb-2">{{ q.icon }}</div>
        <div class="font-medium">{{ q.label }}</div>
      </div>
    </div>

    <!-- 为你推荐（登录用户） -->
    <div v-if="userStore.isLogin && recommend.length" class="yq-card mb-6">
      <div class="flex items-center justify-between mb-4">
        <div class="text-lg font-bold">✨ 为你推荐</div>
        <span class="text-xs text-gray-400">基于你的收藏用 ItemCF 计算</span>
      </div>
      <div class="grid grid-cols-5 gap-4">
        <div v-for="b in recommend" :key="b.id"
             class="cursor-pointer hover:-translate-y-1 transition"
             @click="$router.push('/books/' + b.id)">
          <div class="aspect-[3/4] overflow-hidden rounded bg-gray-100">
            <img :src="b.coverUrl" class="w-full h-full object-cover"
                 @error="(e) => e.target.style.display='none'" />
          </div>
          <div class="mt-2 text-sm truncate">{{ b.name }}</div>
        </div>
      </div>
    </div>

    <!-- 热门推荐 -->
    <div class="yq-card mb-6">
      <div class="flex items-center justify-between mb-4">
        <div class="text-lg font-bold">🔥 热门推荐</div>
        <router-link to="/books" class="text-sm text-gray-500">查看全部 →</router-link>
      </div>
      <div class="grid grid-cols-4 gap-4">
        <div v-for="b in hot" :key="b.id"
             class="cursor-pointer hover:-translate-y-1 transition"
             @click="$router.push('/books/' + b.id)">
          <div class="aspect-[3/4] overflow-hidden rounded bg-gray-100">
            <img :src="b.coverUrl" class="w-full h-full object-cover"
                 @error="(e) => e.target.style.display='none'" />
          </div>
          <div class="mt-2 font-medium truncate">{{ b.name }}</div>
          <div class="text-xs text-gray-500 flex items-center gap-2">
            <span>{{ b.type }}</span>
            <span v-if="b.rating" class="text-red-600">★ {{ b.rating }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 最新公告 -->
    <div class="yq-card">
      <div class="flex items-center justify-between mb-4">
        <div class="text-lg font-bold">📢 最新公告</div>
        <router-link to="/announcements" class="text-sm text-gray-500">更多 →</router-link>
      </div>
      <div v-for="a in announcements.slice(0, 5)" :key="a.id"
           class="py-2 border-b last:border-0 flex justify-between">
        <span class="truncate">{{ a.title }}</span>
        <span class="text-xs text-gray-400">{{ a.createdTime?.slice(0, 10) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { hotBooks, stats, myRecommend, realtimeHotBooks } from '@/api/book'
import { listAnnouncements } from '@/api/biz'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hot = ref([])
const recommend = ref([])
const statsData = ref({})
const announcements = ref([])

const quick = [
  { label: '浏览图书', path: '/books', icon: '📚' },
  { label: '商品购买', path: '/items', icon: '🛒' },
  { label: '放松音乐', path: '/music', icon: '🎵' },
  { label: '我的订单', path: '/my-orders', icon: '📦' }
]

onMounted(async () => {
  // 首页热门：优先使用 Redis 实时热榜，失败再退回 DB
  try {
    const rt = await realtimeHotBooks(8)
    hot.value = rt && rt.length ? rt : await hotBooks(8)
  } catch (e) {
    try { hot.value = await hotBooks(8) } catch (_) {}
  }
  try { statsData.value = await stats() } catch (e) {}
  try { announcements.value = await listAnnouncements() } catch (e) {}
  if (userStore.isLogin) {
    try { recommend.value = await myRecommend(10) } catch (e) {}
  }
})
</script>

<template>
  <div class="yq-page max-w-[900px] mx-auto">
    <div class="yq-card">
      <div class="text-lg font-bold mb-4">📢 公告</div>
      <div v-for="a in list" :key="a.id" class="py-4 border-b last:border-0">
        <div class="flex items-center justify-between">
          <div class="font-medium">{{ a.title }}</div>
          <div class="text-xs text-gray-400">{{ a.createdTime?.slice(0, 16) }} · {{ a.author }}</div>
        </div>
        <div class="mt-2 text-gray-700 whitespace-pre-wrap">{{ a.content }}</div>
      </div>
      <el-empty v-if="!list.length" description="暂无公告" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listAnnouncements } from '@/api/biz'
const list = ref([])
onMounted(async () => { list.value = await listAnnouncements() })
</script>

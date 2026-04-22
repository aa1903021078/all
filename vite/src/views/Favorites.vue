<template>
  <div class="yq-page max-w-[1200px] mx-auto">
    <div class="yq-card">
      <div class="text-lg font-bold mb-4">我的收藏</div>
      <el-empty v-if="!list.length" description="还没有收藏的图书" />
      <div v-else class="grid grid-cols-4 gap-4">
        <div v-for="b in list" :key="b.id"
             class="cursor-pointer hover:-translate-y-1 transition"
             @click="$router.push('/books/' + b.id)">
          <div class="aspect-[3/4] overflow-hidden rounded bg-gray-100">
            <img :src="b.coverUrl" class="w-full h-full object-cover"
                 @error="(e) => e.target.style.display='none'" />
          </div>
          <div class="mt-2 font-medium truncate">{{ b.name }}</div>
          <div class="text-xs text-gray-500">{{ b.type }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { myFavorites } from '@/api/biz'
const list = ref([])
onMounted(async () => { list.value = await myFavorites() })
</script>

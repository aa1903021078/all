<template>
  <div class="yq-page max-w-[1100px] mx-auto">
    <div class="yq-card mb-4">
      <div class="text-lg font-bold mb-4">🎵 音乐欣赏</div>
      <div class="grid grid-cols-4 gap-4">
        <div v-for="s in list" :key="s.id" class="text-center cursor-pointer"
             @click="play(s)">
          <div class="aspect-square rounded-full overflow-hidden bg-gray-100 mb-2"
               :class="{ 'animate-spin-slow': current?.id === s.id && isPlaying }">
            <img :src="s.coverUrl" class="w-full h-full object-cover"
                 @error="(e) => e.target.style.display='none'" />
          </div>
          <div class="font-medium truncate">{{ s.name }}</div>
          <div class="text-xs text-gray-500 truncate">{{ s.artist }}</div>
        </div>
      </div>
    </div>

    <!-- 底部播放条（网易云风格） -->
    <div v-if="current" class="fixed bottom-0 left-0 right-0 bg-[#1f1f1f] text-white p-3 flex items-center gap-4 z-30">
      <img :src="current.coverUrl" class="w-12 h-12 rounded"
           @error="(e) => e.target.style.display='none'" />
      <div class="flex-1 min-w-0">
        <div class="truncate text-sm">{{ current.name }} - {{ current.artist }}</div>
        <audio ref="audioRef" :src="current.fileUrl" @play="isPlaying = true" @pause="isPlaying = false"
               @ended="isPlaying = false" controls class="w-full mt-1"></audio>
      </div>
      <el-button size="small" @click="current = null">关闭</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { listSongs } from '@/api/biz'

const list = ref([])
const current = ref(null)
const isPlaying = ref(false)
const audioRef = ref(null)

async function play(s) {
  current.value = s
  await nextTick()
  audioRef.value?.play()
}
onMounted(async () => { list.value = await listSongs() })
</script>

<style scoped>
.animate-spin-slow { animation: spin 6s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>

<template>
  <div class="yq-page max-w-[900px] mx-auto" :class="{ 'reader-night': readerStore.night }">
    <!-- 工具栏 -->
    <div class="yq-card mb-4 flex items-center justify-between sticky top-[60px] z-10">
      <div class="flex items-center gap-3">
        <el-button size="small" @click="$router.push('/books/' + route.params.bookId)">← 返回</el-button>
        <span class="font-medium" v-if="chapter">{{ chapter.chapterNumber }}. {{ chapter.chapterTitle }}</span>
      </div>
      <div class="flex items-center gap-3">
        <span class="text-xs">字号</span>
        <el-slider v-model="readerStore.fontSize" :min="12" :max="32" :step="1" class="w-[120px]"
                   @change="v => readerStore.setFont(v)" />
        <span class="text-xs">行距</span>
        <el-slider v-model="readerStore.lineHeight" :min="1.2" :max="3.0" :step="0.1" class="w-[120px]"
                   @change="v => readerStore.setLine(v)" />
        <el-button size="small" @click="readerStore.toggleNight()">
          {{ readerStore.night ? '☀ 日间' : '🌙 夜间' }}
        </el-button>
      </div>
    </div>

    <div class="yq-card" v-loading="loading">
      <article v-if="chapter"
               :style="{ fontSize: readerStore.fontSize + 'px', lineHeight: readerStore.lineHeight }"
               class="whitespace-pre-wrap leading-relaxed">
        {{ chapter.content }}
      </article>
    </div>

    <div class="yq-card mt-4 flex justify-between">
      <el-button :disabled="!prevId" @click="goto(prevId)">← 上一章</el-button>
      <el-button :disabled="!nextId" type="primary" @click="goto(nextId)">下一章 →</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getChapter, listChapters } from '@/api/book'
import { useReaderStore } from '@/store/reader'

const route = useRoute()
const router = useRouter()
const readerStore = useReaderStore()
const chapter = ref(null)
const chapters = ref([])
const loading = ref(false)

const currentIndex = computed(() => chapters.value.findIndex(c => String(c.id) === String(route.params.chapterId)))
const prevId = computed(() => currentIndex.value > 0 ? chapters.value[currentIndex.value - 1].id : null)
const nextId = computed(() => currentIndex.value >= 0 && currentIndex.value < chapters.value.length - 1
    ? chapters.value[currentIndex.value + 1].id : null)

async function load() {
  loading.value = true
  try {
    chapter.value = await getChapter(route.params.chapterId)
    if (!chapters.value.length) {
      chapters.value = await listChapters(route.params.bookId)
    }
  } finally {
    loading.value = false
  }
}

function goto(id) {
  router.push(`/books/${route.params.bookId}/read/${id}`)
}

watch(() => route.params.chapterId, load)
onMounted(load)
</script>

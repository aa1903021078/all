<template>
  <div class="card card-hover note-card" @click="$router.push('/notes/' + note.id)">
    <img v-if="firstImage" :src="firstImage" class="cover" />
    <div class="body">
      <div class="title line-2">{{ note.title || note.content }}</div>
      <div v-if="note.shopName" class="pill mt-8">📍 {{ note.shopName }}</div>
      <div class="flex items-center justify-between mt-12">
        <div class="muted text-sm flex items-center gap-8">
          <el-avatar :size="22" :src="note.authorAvatar" />
          <span class="line-1">{{ note.authorName }}</span>
        </div>
        <div class="muted text-sm flex items-center gap-8">
          <StarRating v-if="note.rating" :model-value="note.rating" :size="12" readonly />
          <span>❤️ {{ note.likeCount || 0 }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import StarRating from './StarRating.vue'

const props = defineProps({
  note: { type: Object, required: true },
})

const firstImage = computed(() => {
  try {
    const arr = JSON.parse(props.note.images || '[]')
    return arr[0] || props.note.shopCover || ''
  } catch (e) {
    return props.note.shopCover || ''
  }
})
</script>

<style scoped>
.note-card {
  display: flex;
  flex-direction: column;
}
.cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
}
.body {
  padding: 12px;
}
.title {
  font-weight: 600;
  font-size: 15px;
  min-height: 42px;
}
</style>

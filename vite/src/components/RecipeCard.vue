<template>
  <div class="card card-hover recipe-card" @click="$router.push('/recipes/' + recipe.id)">
    <div class="cover-wrap">
      <img :src="recipe.cover" class="cover" />
      <span class="diff" :class="'d' + recipe.difficulty">{{ diffText }}</span>
    </div>
    <div class="body">
      <div class="title line-1">{{ recipe.title }}</div>
      <div class="muted text-sm mt-8 flex items-center gap-8">
        <el-avatar :size="20" :src="recipe.authorAvatar" />
        <span class="line-1">{{ recipe.authorName }}</span>
      </div>
      <div class="muted text-sm mt-8 flex gap-12">
        <span>⏱ {{ recipe.cookTime }}分钟</span>
        <span>❤️ {{ recipe.likeCount || 0 }}</span>
        <span>⭐ {{ recipe.favoriteCount || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  recipe: { type: Object, required: true },
})

const diffText = computed(() => ({ 1: '简单', 2: '中等', 3: '困难' }[props.recipe.difficulty] || '简单'))
</script>

<style scoped>
.recipe-card {
  display: flex;
  flex-direction: column;
}
.cover-wrap {
  position: relative;
  height: 160px;
}
.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.diff {
  position: absolute;
  right: 8px;
  top: 8px;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 6px;
}
.d1 {
  background: #67c23a;
}
.d2 {
  background: #e6a23c;
}
.d3 {
  background: #f56c6c;
}
.body {
  padding: 12px;
}
.title {
  font-weight: 700;
  font-size: 16px;
}
</style>

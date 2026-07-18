<template>
  <div class="card card-hover shop-card" @click="$router.push('/shops/' + shop.id)">
    <div class="cover-wrap">
      <img :src="shop.cover" class="cover" />
      <span v-if="shop.recommend === 1" class="badge">推荐</span>
      <span v-if="shop.lit" class="lit-badge">🔥 已点亮</span>
    </div>
    <div class="body">
      <div class="name line-1">{{ shop.name }}</div>
      <div class="flex items-center justify-between mt-8">
        <StarRating :model-value="Number(shop.rating)" :size="14" readonly show-score />
        <span class="price">¥{{ shop.avgPrice }}/人</span>
      </div>
      <div class="muted text-sm mt-8 line-1">
        <el-icon><Location /></el-icon>
        {{ shop.categoryName || '美食' }} · {{ shop.address }}
      </div>
      <div class="muted text-sm mt-8 flex gap-12">
        <span>👀 {{ shop.viewCount || 0 }}</span>
        <span>🔥 {{ shop.checkinCount || 0 }} 点亮</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import StarRating from './StarRating.vue'

defineProps({
  shop: { type: Object, required: true },
})
</script>

<style scoped>
.shop-card {
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
.badge {
  position: absolute;
  left: 8px;
  top: 8px;
  background: var(--brand);
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 6px;
}
.lit-badge {
  position: absolute;
  right: 8px;
  top: 8px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 6px;
}
.body {
  padding: 12px;
}
.name {
  font-weight: 700;
  font-size: 16px;
}
</style>

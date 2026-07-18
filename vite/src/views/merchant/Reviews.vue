<template>
  <div v-loading="loading">
    <div class="flex items-center gap-12 mb-12">
      <span class="bold text-lg">评价管理</span>
      <el-select v-model="currentShopId" placeholder="选择店铺" style="width: 220px" @change="loadNotes">
        <el-option v-for="s in shops" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <div class="rating-summary" v-if="currentShop">
        综合评分
        <StarRating :model-value="Number(currentShop.rating)" :size="16" readonly show-score />
        <span class="muted">({{ currentShop.ratingCount || 0 }}条)</span>
      </div>
    </div>

    <div class="card" style="padding: 16px">
      <div v-for="n in notes" :key="n.id" class="review-item">
        <el-avatar :size="42" :src="n.authorAvatar" />
        <div class="review-body">
          <div class="flex items-center justify-between">
            <span class="bold">{{ n.authorName }}</span>
            <StarRating v-if="n.rating" :model-value="n.rating" :size="14" readonly />
          </div>
          <div class="review-title" v-if="n.title">{{ n.title }}</div>
          <div class="muted mt-8">{{ n.content }}</div>
          <div class="review-imgs" v-if="parseImgs(n.images).length">
            <img v-for="(img, i) in parseImgs(n.images)" :key="i" :src="img" />
          </div>
          <div class="muted text-sm mt-8">
            {{ formatTime(n.createTime) }} · ❤️ {{ n.likeCount || 0 }} · 💬 {{ n.commentCount || 0 }}
            <el-link type="primary" :underline="false" class="ml-8" @click="$router.push('/notes/' + n.id)">查看详情</el-link>
          </div>
        </div>
      </div>
      <el-empty v-if="!notes.length" description="该店铺暂无评价" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { shopApi, noteApi } from '@/api'
import StarRating from '@/components/StarRating.vue'

const loading = ref(false)
const shops = ref([])
const notes = ref([])
const currentShopId = ref(null)

const currentShop = computed(() => shops.value.find((s) => s.id === currentShopId.value))

function parseImgs(s) {
  try { return JSON.parse(s || '[]') } catch (e) { return [] }
}
function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

async function loadNotes() {
  if (!currentShopId.value) return
  const res = await noteApi.byShop(currentShopId.value)
  notes.value = res.data || []
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await shopApi.mine()
    shops.value = res.data || []
    if (shops.value.length) {
      currentShopId.value = shops.value[0].id
      await loadNotes()
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.rating-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}
.review-item {
  display: flex;
  gap: 14px;
  padding: 16px 4px;
  border-bottom: 1px solid #f2f2f2;
}
.review-body {
  flex: 1;
  min-width: 0;
}
.review-title {
  font-weight: 600;
  margin-top: 4px;
}
.review-imgs {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
}
.review-imgs img {
  width: 90px;
  height: 90px;
  object-fit: cover;
  border-radius: 6px;
}
.ml-8 {
  margin-left: 8px;
}
</style>

<template>
  <div class="page-wrap">
    <div class="section-title">🗺️ 美食地图</div>
    <div class="map-toolbar card">
      <div class="flex items-center gap-8 flex-wrap">
        <span class="muted text-sm">菜系:</span>
        <el-radio-group v-model="categoryId" size="small" @change="load">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button v-for="c in categories" :key="c.id" :value="c.id">
            {{ c.name }}
          </el-radio-button>
        </el-radio-group>
      </div>
      <el-checkbox v-model="onlyLit" @change="load">只看我点亮的 🔥</el-checkbox>
    </div>

    <div class="map-layout mt-16">
      <div class="map-main" v-loading="loading">
        <SimpleMap :shops="shops" @open="goDetail" />
      </div>
      <div class="map-side card">
        <div class="side-head">共 {{ shops.length }} 家店铺</div>
        <div class="side-list">
          <div
            v-for="s in shops"
            :key="s.id"
            class="side-item"
            @click="goDetail(s)"
          >
            <img :src="s.cover" class="side-cover" />
            <div class="side-info">
              <div class="bold line-1">{{ s.name }}</div>
              <StarRating :model-value="Number(s.rating)" :size="12" readonly show-score />
              <div class="muted text-sm line-1">{{ s.categoryName }} · ¥{{ s.avgPrice }}/人</div>
            </div>
          </div>
          <el-empty v-if="!loading && !shops.length" description="暂无点位" :image-size="80" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { shopApi, categoryApi } from '@/api'
import SimpleMap from '@/components/SimpleMap.vue'
import StarRating from '@/components/StarRating.vue'

const router = useRouter()
const shops = ref([])
const categories = ref([])
const categoryId = ref(null)
const onlyLit = ref(false)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await shopApi.map({ categoryId: categoryId.value, onlyLit: onlyLit.value })
    shops.value = res.data || []
  } finally {
    loading.value = false
  }
}

function goDetail(s) {
  router.push('/shops/' + s.id)
}

onMounted(() => {
  load()
  categoryApi.list().then((res) => (categories.value = res.data || []))
})
</script>

<style scoped>
.map-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  gap: 12px;
  flex-wrap: wrap;
}
.map-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 16px;
}
.map-side {
  padding: 12px;
  display: flex;
  flex-direction: column;
  max-height: 520px;
}
.side-head {
  font-weight: 700;
  padding: 4px 4px 10px;
  border-bottom: 1px solid var(--border);
}
.side-list {
  overflow-y: auto;
  flex: 1;
}
.side-item {
  display: flex;
  gap: 10px;
  padding: 10px 4px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.15s;
}
.side-item:hover {
  background: var(--el-color-primary-light-9);
}
.side-cover {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  object-fit: cover;
}
.side-info {
  flex: 1;
  min-width: 0;
}
@media (max-width: 900px) {
  .map-layout {
    grid-template-columns: 1fr;
  }
}
</style>

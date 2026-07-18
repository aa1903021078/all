<template>
  <div class="page-wrap">
    <!-- Hero -->
    <div class="hero card">
      <div class="hero-text">
        <h1>发现身边的好味道 🍜</h1>
        <p>探店打卡 · 真实评分 · 居家菜谱 · 一站式美食生活</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/map')">
            <el-icon><MapLocation /></el-icon>&nbsp;打开美食地图
          </el-button>
          <el-button size="large" @click="$router.push('/recipes')">逛逛菜谱广场</el-button>
        </div>
      </div>
      <div class="hero-stats">
        <div class="stat" v-for="s in statCards" :key="s.label">
          <div class="stat-num">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <!-- 菜系快捷 -->
    <div class="cats card">
      <span
        class="cat-chip"
        :class="{ active: !activeCat }"
        @click="filterCat(null)"
      >全部</span>
      <span
        v-for="c in categories"
        :key="c.id"
        class="cat-chip"
        :class="{ active: activeCat === c.id }"
        @click="filterCat(c.id)"
      >{{ c.icon || '🍴' }} {{ c.name }}</span>
    </div>

    <!-- 数据可视化 -->
    <div class="stat-charts">
      <div class="chart-card card">
        <div class="chart-title">🍽️ 平台美食类别分布</div>
        <EChart v-if="catOption" :option="catOption" height="280px" />
        <el-empty v-else description="暂无数据" :image-size="70" />
      </div>
      <div class="chart-card card">
        <div class="chart-title">💰 人均消费区间分布</div>
        <EChart v-if="priceOption" :option="priceOption" height="280px" />
        <el-empty v-else description="暂无数据" :image-size="70" />
      </div>
    </div>

    <!-- 推荐店铺 -->
    <div class="section-title">🔥 热门探店推荐</div>
    <div v-loading="loadingShops" class="grid-cards">
      <ShopCard v-for="s in shops" :key="s.id" :shop="s" />
    </div>
    <el-empty v-if="!loadingShops && !shops.length" description="暂无店铺" />

    <!-- 热门菜谱 -->
    <div class="section-title mt-16">👩‍🍳 人气菜谱</div>
    <div v-loading="loadingRecipes" class="grid-cards">
      <RecipeCard v-for="r in recipes" :key="r.id" :recipe="r" />
    </div>

    <!-- 最新探店笔记 -->
    <div class="section-title mt-16">📝 最新探店笔记</div>
    <div v-loading="loadingNotes" class="grid-cards">
      <NoteCard v-for="n in notes" :key="n.id" :note="n" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { shopApi, recipeApi, noteApi, categoryApi, statsApi } from '@/api'
import ShopCard from '@/components/ShopCard.vue'
import RecipeCard from '@/components/RecipeCard.vue'
import NoteCard from '@/components/NoteCard.vue'
import EChart from '@/components/EChart.vue'

const router = useRouter()
const shops = ref([])
const recipes = ref([])
const notes = ref([])
const categories = ref([])
const activeCat = ref(null)
const overview = ref({})
const loadingShops = ref(false)
const loadingRecipes = ref(false)
const loadingNotes = ref(false)

const statCards = ref([])
const catOption = ref(null)
const priceOption = ref(null)
const chartPalette = ['#ff6a3d', '#ffa940', '#ffc53d', '#73d13d', '#36cfc9', '#40a9ff', '#9254de', '#f759ab']

function buildCharts(cat, price) {
  if (cat && cat.length) {
    catOption.value = {
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, type: 'scroll' },
      series: [{
        type: 'pie', radius: ['40%', '66%'], center: ['50%', '44%'],
        data: cat.map((c, i) => ({ name: c.name || '其他', value: c.value, itemStyle: { color: chartPalette[i % chartPalette.length] } })),
        label: { show: false },
      }],
    }
  }
  if (price && price.length) {
    priceOption.value = {
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 16, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: price.map((p) => p.name) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{ type: 'bar', data: price.map((p) => p.value), barWidth: '50%', itemStyle: { color: '#ff6a3d', borderRadius: [6, 6, 0, 0] } }],
    }
  }
}

function buildStats() {
  statCards.value = [
    { label: '入驻店铺', value: overview.value.shopCount || 0 },
    { label: '探店笔记', value: overview.value.noteCount || 0 },
    { label: '分享菜谱', value: overview.value.recipeCount || 0 },
    { label: '美食家', value: overview.value.userCount || 0 },
  ]
}

async function loadShops() {
  loadingShops.value = true
  try {
    const res = await shopApi.recommend(8)
    shops.value = res.data || []
  } finally {
    loadingShops.value = false
  }
}

async function filterCat(id) {
  activeCat.value = id
  loadingShops.value = true
  try {
    if (id == null) {
      await loadShops()
      return
    }
    const res = await shopApi.page({ current: 1, size: 8, categoryId: id, sort: 'rating' })
    shops.value = (res.data && res.data.records) || []
  } finally {
    loadingShops.value = false
  }
}

onMounted(async () => {
  loadShops()
  categoryApi.list().then((res) => (categories.value = res.data || []))
  statsApi.overview().then((res) => {
    overview.value = res.data || {}
    buildStats()
  })
  Promise.all([statsApi.categoryDistribution(), statsApi.priceDistribution()]).then(([c, p]) => {
    buildCharts(c.data || [], p.data || [])
  })
  loadingRecipes.value = true
  recipeApi.recommend(8).then((res) => (recipes.value = res.data || [])).finally(() => (loadingRecipes.value = false))
  loadingNotes.value = true
  noteApi.feed(8).then((res) => (notes.value = res.data || [])).finally(() => (loadingNotes.value = false))
})
</script>

<style scoped>
.hero {
  display: flex;
  padding: 34px 40px;
  background: linear-gradient(120deg, #fff6f2 0%, #fff 60%);
  align-items: center;
  gap: 30px;
}
.hero-text {
  flex: 1;
}
.hero-text h1 {
  margin: 0 0 10px;
  font-size: 30px;
  color: #303133;
}
.hero-text p {
  margin: 0 0 20px;
  color: var(--text-muted);
  font-size: 15px;
}
.hero-actions {
  display: flex;
  gap: 12px;
}
.hero-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}
.stat {
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 14px 22px;
  text-align: center;
  min-width: 96px;
}
.stat-num {
  font-size: 24px;
  font-weight: 800;
  color: var(--brand);
}
.stat-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}
.cats {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 14px;
  margin: 16px 0;
}
.stat-charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
.chart-card {
  padding: 16px;
}
.chart-title {
  font-weight: 700;
  margin-bottom: 8px;
}
@media (max-width: 900px) {
  .stat-charts {
    grid-template-columns: 1fr;
  }
}
.cat-chip {
  padding: 6px 14px;
  border-radius: 18px;
  background: #f4f5f7;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}
.cat-chip:hover,
.cat-chip.active {
  background: var(--brand);
  color: #fff;
}
</style>

<template>
  <div v-loading="loading">
    <div class="stat-grid">
      <div class="stat-box card" v-for="s in statCards" :key="s.label">
        <div class="stat-icon" :style="{ background: s.color }">{{ s.icon }}</div>
        <div>
          <div class="stat-num">{{ s.value }}</div>
          <div class="muted text-sm">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <div class="card chart-card mt-16">
      <div class="flex items-center justify-between mb-12">
        <span class="bold">店铺点亮趋势</span>
        <el-select v-model="currentShopId" placeholder="选择店铺" size="small" style="width: 200px" @change="loadTrend">
          <el-option v-for="s in shops" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </div>
      <EChart v-if="trendOption" :option="trendOption" height="320px" />
      <el-empty v-else description="暂无数据" />
    </div>

    <div class="card mt-16" style="padding: 16px">
      <div class="bold mb-12">我的店铺</div>
      <el-table :data="shops" style="width: 100%">
        <el-table-column label="店铺" min-width="200">
          <template #default="{ row }">
            <div class="flex items-center gap-8">
              <img :src="row.cover" class="mini-cover" />
              <span class="bold">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="140">
          <template #default="{ row }">
            <StarRating :model-value="Number(row.rating)" :size="13" readonly show-score />
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="90" />
        <el-table-column prop="checkinCount" label="点亮" width="90" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { statsApi } from '@/api'
import EChart from '@/components/EChart.vue'
import StarRating from '@/components/StarRating.vue'

const loading = ref(false)
const overview = ref({})
const shops = ref([])
const statCards = ref([])
const currentShopId = ref(null)
const trendOption = ref(null)

const statusTextMap = { 0: '待审核', 1: '营业中', 2: '已下架', 3: '已拒绝' }
const statusTagMap = { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }
function statusText(s) {
  return statusTextMap[s] || '-'
}
function statusTag(s) {
  return statusTagMap[s] || 'info'
}

function buildStats() {
  const o = overview.value
  statCards.value = [
    { label: '店铺数量', value: o.shopCount || 0, icon: '🏪', color: '#ff6a3d' },
    { label: '累计浏览', value: o.totalView || 0, icon: '👀', color: '#409eff' },
    { label: '累计点亮', value: o.totalCheckin || 0, icon: '🔥', color: '#e6a23c' },
    { label: '平均评分', value: o.avgRating || 0, icon: '⭐', color: '#67c23a' },
    { label: '预约总数', value: o.reservationCount || 0, icon: '📅', color: '#909399' },
    { label: '待确认预约', value: o.pendingReservation || 0, icon: '⏰', color: '#f56c6c' },
  ]
}

async function loadTrend() {
  if (!currentShopId.value) return
  const res = await statsApi.merchantCheckinTrend(currentShopId.value)
  const data = res.data || []
  trendOption.value = {
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: data.map((d) => d.date) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '点亮数', type: 'line', smooth: true, data: data.map((d) => d.value),
      areaStyle: { color: 'rgba(255,106,61,0.2)' }, itemStyle: { color: '#ff6a3d' },
    }],
  }
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await statsApi.merchant()
    overview.value = res.data || {}
    shops.value = res.data.shops || []
    buildStats()
    if (shops.value.length) {
      currentShopId.value = shops.value[0].id
      loadTrend()
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}
.stat-box {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
.stat-num {
  font-size: 24px;
  font-weight: 800;
}
.chart-card {
  padding: 16px;
}
.mini-cover {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  object-fit: cover;
}
</style>

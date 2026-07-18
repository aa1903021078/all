<template>
  <div v-loading="loading">
    <div class="welcome card">
      <div>
        <div class="text-xl bold">欢迎回来,{{ auth.user?.nickname }} 👋</div>
        <div class="muted mt-8">这里是美食探店平台运营控制台,数据实时更新</div>
      </div>
      <img src="https://picsum.photos/seed/dashboard/160/100" class="welcome-img" />
    </div>

    <div class="stat-grid mt-16">
      <div class="stat-box card" v-for="s in statCards" :key="s.label" @click="s.to && $router.push(s.to)">
        <div class="stat-icon" :style="{ background: s.color }">{{ s.icon }}</div>
        <div>
          <div class="stat-num">{{ s.value }}</div>
          <div class="muted text-sm">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <div class="chart-row mt-16">
      <div class="card chart-card">
        <div class="bold mb-12">菜系分布</div>
        <EChart v-if="catOption" :option="catOption" height="300px" />
      </div>
      <div class="card chart-card">
        <div class="bold mb-12">人均消费区间</div>
        <EChart v-if="priceOption" :option="priceOption" height="300px" />
      </div>
    </div>

    <div class="card chart-card mt-16">
      <div class="bold mb-12">探店笔记发布趋势</div>
      <EChart v-if="trendOption" :option="trendOption" height="300px" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { statsApi } from '@/api'
import { useAuthStore } from '@/store/auth'
import EChart from '@/components/EChart.vue'

const auth = useAuthStore()
const loading = ref(false)
const statCards = ref([])
const catOption = ref(null)
const priceOption = ref(null)
const trendOption = ref(null)

const palette = ['#ff6a3d', '#ffb495', '#409eff', '#67c23a', '#e6a23c', '#909399', '#b37feb']

function buildCards(o) {
  statCards.value = [
    { label: '店铺总数', value: o.shopCount || 0, icon: '🏪', color: '#ff6a3d', to: '/admin/shops' },
    { label: '菜谱总数', value: o.recipeCount || 0, icon: '🍲', color: '#67c23a', to: '/admin/content' },
    { label: '笔记总数', value: o.noteCount || 0, icon: '📝', color: '#409eff', to: '/admin/content' },
    { label: '用户总数', value: o.userCount || 0, icon: '👥', color: '#e6a23c', to: '/admin/users' },
    { label: '点亮次数', value: o.checkinCount || 0, icon: '🔥', color: '#f56c6c' },
    { label: '预约总数', value: o.reservationCount || 0, icon: '📅', color: '#909399' },
  ]
}

onMounted(async () => {
  loading.value = true
  try {
    const [ov, cat, price, trend] = await Promise.all([
      statsApi.overview(),
      statsApi.categoryDistribution(),
      statsApi.priceDistribution(),
      statsApi.publishTrend(),
    ])
    buildCards(ov.data || {})
    const catData = cat.data || []
    catOption.value = {
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie', radius: ['40%', '68%'], center: ['50%', '44%'],
        data: catData.map((d, i) => ({ name: d.name || '其他', value: d.value, itemStyle: { color: palette[i % palette.length] } })),
      }],
    }
    const priceData = price.data || []
    priceOption.value = {
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: priceData.map((d) => d.name) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{ type: 'bar', data: priceData.map((d) => d.value), itemStyle: { color: '#ff6a3d', borderRadius: [6, 6, 0, 0] }, barWidth: '46%' }],
    }
    const trendData = trend.data || []
    trendOption.value = {
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: trendData.map((d) => d.date) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{ type: 'line', smooth: true, data: trendData.map((d) => d.value), areaStyle: { color: 'rgba(64,158,255,0.2)' }, itemStyle: { color: '#409eff' } }],
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.welcome {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: linear-gradient(120deg, #fff6f2, #fff);
}
.welcome-img {
  border-radius: 10px;
}
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
  gap: 16px;
}
.stat-box {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  cursor: pointer;
}
.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}
.stat-num {
  font-size: 24px;
  font-weight: 800;
}
.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.chart-card {
  padding: 16px;
}
@media (max-width: 900px) {
  .chart-row {
    grid-template-columns: 1fr;
  }
}
</style>

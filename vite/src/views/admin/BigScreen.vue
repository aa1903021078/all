<template>
  <div class="bigscreen" v-loading="loading" element-loading-background="rgba(4,17,42,0.8)">
    <div class="screen-header">
      <span class="screen-title">🍜 美食探店平台 · 数据可视化大屏</span>
      <span class="screen-time">{{ now }}</span>
    </div>

    <div class="screen-metrics">
      <div class="metric" v-for="m in metrics" :key="m.label">
        <div class="metric-num">{{ m.value }}</div>
        <div class="metric-label">{{ m.label }}</div>
      </div>
    </div>

    <div class="screen-grid">
      <div class="panel">
        <div class="panel-title">菜系分布</div>
        <EChart v-if="catOption" :option="catOption" height="300px" />
      </div>
      <div class="panel">
        <div class="panel-title">人均消费区间</div>
        <EChart v-if="priceOption" :option="priceOption" height="300px" />
      </div>
      <div class="panel span-2">
        <div class="panel-title">探店点亮 &amp; 笔记发布趋势</div>
        <EChart v-if="trendOption" :option="trendOption" height="300px" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { statsApi } from '@/api'
import EChart from '@/components/EChart.vue'

const loading = ref(false)
const metrics = ref([])
const catOption = ref(null)
const priceOption = ref(null)
const trendOption = ref(null)
const now = ref('')
let timer = null

const palette = ['#36cfc9', '#ff7a45', '#ffc53d', '#40a9ff', '#73d13d', '#9254de', '#f759ab']

function tick() {
  now.value = new Date().toLocaleString('zh-CN')
}

function axisStyle() {
  return {
    axisLine: { lineStyle: { color: '#3b5b8c' } },
    axisLabel: { color: '#9fb3d1' },
    splitLine: { lineStyle: { color: 'rgba(59,91,140,0.3)' } },
  }
}

onMounted(async () => {
  tick()
  timer = setInterval(tick, 1000)
  loading.value = true
  try {
    const res = await statsApi.dashboard()
    const d = res.data || {}
    const o = d.overview || {}
    metrics.value = [
      { label: '店铺总数', value: o.shopCount || 0 },
      { label: '菜谱总数', value: o.recipeCount || 0 },
      { label: '探店笔记', value: o.noteCount || 0 },
      { label: '注册用户', value: o.userCount || 0 },
      { label: '点亮次数', value: o.checkinCount || 0 },
      { label: '到店预约', value: o.reservationCount || 0 },
    ]
    const cat = d.categoryDistribution || []
    catOption.value = {
      tooltip: { trigger: 'item' },
      legend: { bottom: 0, textStyle: { color: '#9fb3d1' } },
      series: [{
        type: 'pie', radius: ['38%', '66%'], center: ['50%', '42%'],
        label: { color: '#cfe0f5' },
        data: cat.map((c, i) => ({ name: c.name || '其他', value: c.value, itemStyle: { color: palette[i % palette.length] } })),
      }],
    }
    const price = d.priceDistribution || []
    priceOption.value = {
      tooltip: { trigger: 'axis' },
      grid: { left: 45, right: 20, top: 30, bottom: 40 },
      xAxis: { type: 'category', data: price.map((p) => p.name), ...axisStyle() },
      yAxis: { type: 'value', minInterval: 1, ...axisStyle() },
      series: [{ type: 'bar', data: price.map((p) => p.value), barWidth: '46%', itemStyle: { color: '#36cfc9', borderRadius: [6, 6, 0, 0] } }],
    }
    const checkin = d.checkinTrend || []
    const publish = d.publishTrend || []
    const dates = Array.from(new Set([...checkin.map((x) => x.date), ...publish.map((x) => x.date)])).sort()
    const cMap = {}
    checkin.forEach((x) => (cMap[x.date] = x.value))
    const pMap = {}
    publish.forEach((x) => (pMap[x.date] = x.value))
    trendOption.value = {
      tooltip: { trigger: 'axis' },
      legend: { data: ['点亮数', '笔记数'], textStyle: { color: '#9fb3d1' }, top: 0 },
      grid: { left: 45, right: 20, top: 40, bottom: 40 },
      xAxis: { type: 'category', data: dates, ...axisStyle() },
      yAxis: { type: 'value', minInterval: 1, ...axisStyle() },
      series: [
        { name: '点亮数', type: 'line', smooth: true, data: dates.map((x) => cMap[x] || 0), itemStyle: { color: '#ff7a45' }, areaStyle: { color: 'rgba(255,122,69,0.2)' } },
        { name: '笔记数', type: 'line', smooth: true, data: dates.map((x) => pMap[x] || 0), itemStyle: { color: '#40a9ff' }, areaStyle: { color: 'rgba(64,169,255,0.2)' } },
      ],
    }
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => timer && clearInterval(timer))
</script>

<style scoped>
.bigscreen {
  min-height: calc(100vh - 92px);
  background: radial-gradient(circle at 50% 0%, #0a2a5e 0%, #04112a 60%);
  border-radius: 10px;
  padding: 20px;
  color: #fff;
}
.screen-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(59, 91, 140, 0.5);
}
.screen-title {
  font-size: 24px;
  font-weight: 800;
  background: linear-gradient(90deg, #36cfc9, #40a9ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.screen-time {
  color: #9fb3d1;
  font-family: monospace;
}
.screen-metrics {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
  margin: 18px 0;
}
.metric {
  background: rgba(20, 50, 100, 0.4);
  border: 1px solid rgba(59, 91, 140, 0.6);
  border-radius: 10px;
  padding: 16px;
  text-align: center;
}
.metric-num {
  font-size: 30px;
  font-weight: 800;
  color: #36cfc9;
}
.metric-label {
  color: #9fb3d1;
  margin-top: 6px;
  font-size: 13px;
}
.screen-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.panel {
  background: rgba(20, 50, 100, 0.3);
  border: 1px solid rgba(59, 91, 140, 0.5);
  border-radius: 10px;
  padding: 16px;
}
.panel.span-2 {
  grid-column: span 2;
}
.panel-title {
  font-weight: 700;
  margin-bottom: 10px;
  color: #cfe0f5;
}
@media (max-width: 1100px) {
  .screen-metrics {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>

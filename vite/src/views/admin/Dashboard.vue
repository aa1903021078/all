<template>
  <div>
    <div class="grid grid-cols-4 gap-4 mb-6">
      <div class="yq-card">
        <div class="text-sm text-gray-500">图书总数</div>
        <div class="text-3xl font-bold mt-2" style="color: var(--yq-primary)">{{ s.bookTotal || 0 }}</div>
      </div>
      <div class="yq-card">
        <div class="text-sm text-gray-500">章节总数</div>
        <div class="text-3xl font-bold mt-2">{{ s.chapterTotal || 0 }}</div>
      </div>
      <div class="yq-card">
        <div class="text-sm text-gray-500">用户总数</div>
        <div class="text-3xl font-bold mt-2">{{ userTotal }}</div>
      </div>
      <div class="yq-card">
        <div class="text-sm text-gray-500">订单总数</div>
        <div class="text-3xl font-bold mt-2">{{ orderTotal }}</div>
      </div>
    </div>
    <div class="yq-card">
      <div class="text-lg font-bold mb-4">🔥 热门图书 Top 8</div>
      <div ref="chartRef" style="height: 360px;"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { hotBooks, stats } from '@/api/book'
import { pageUsers, adminPageOrders } from '@/api/biz'

const s = ref({})
const userTotal = ref(0)
const orderTotal = ref(0)
const chartRef = ref()

onMounted(async () => {
  s.value = await stats()
  try { userTotal.value = (await pageUsers({ page: 1, size: 1 })).total } catch (e) {}
  try { orderTotal.value = (await adminPageOrders({ page: 1, size: 1 })).total } catch (e) {}
  const hot = await hotBooks(8)
  const chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: {},
    grid: { left: 120, right: 30 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: hot.map(b => b.name).reverse() },
    series: [{
      type: 'bar',
      data: hot.map(b => b.heat || 0).reverse(),
      itemStyle: { color: '#c20c0c' }
    }]
  })
})
</script>

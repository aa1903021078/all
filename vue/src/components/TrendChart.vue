<template>
  <div class="trend-chart">
    <div class="chart-header">
      <h3>{{ title }}</h3>
      <slot name="extra" />
    </div>
    <div ref="chartRef" class="chart-container"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  title: String,
  data: Array,
  xAxis: Array,
  type: {
    type: String,
    default: 'line'
  },
  color: {
    type: String,
    default: '#10b981'
  }
})

const chartRef = ref(null)
let chartInstance = null

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})

watch(() => props.data, () => {
  updateChart()
}, { deep: true })

const initChart = () => {
  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!chartInstance) return

  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: props.xAxis,
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { color: '#6b7280' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f3f4f6' } }
    },
    series: [{
      data: props.data,
      type: props.type,
      smooth: true,
      symbolSize: 8,
      itemStyle: { color: props.color },
      areaStyle: props.type === 'line' ? {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: props.color + '4D' }, // 30% opacity
          { offset: 1, color: props.color + '0D' }  // 5% opacity
        ])
      } : undefined
    }]
  }

  chartInstance.setOption(option)
}

const handleResize = () => {
  chartInstance?.resize()
}
</script>

<style scoped lang="scss">
.trend-chart {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);

  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      margin: 0;
      font-size: 16px;
      color: #111827;
    }
  }

  .chart-container {
    height: 300px;
  }
}
</style>
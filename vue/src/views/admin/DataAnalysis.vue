<template>
  <div class="data-analysis">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>📊 订单趋势（近30天）</span>
          </template>
          <div ref="orderTrendChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>♻️ 品类回收占比</span>
          </template>
          <div ref="categoryChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>👥 用户增长趋势</span>
          </template>
          <div ref="userChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>🌍 环保贡献统计</span>
          </template>
          <div ref="ecoChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>📈 回收员绩效排行</span>
      </template>
      <el-table :data="collectorRank">
        <el-table-column type="index" label="排名" width="80" />
        <el-table-column prop="name" label="回收员" />
        <el-table-column prop="orderCount" label="完成订单" />
        <el-table-column prop="totalWeight" label="回收重量(kg)" />
        <el-table-column prop="rating" label="评分" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const orderTrendChart = ref(null)
const categoryChart = ref(null)
const userChart = ref(null)
const ecoChart = ref(null)
const collectorRank = ref([])

onMounted(() => {
  initCharts()
  loadCollectorRank()
})

const initCharts = () => {
  // 订单趋势图
  const trendChart = echarts.init(orderTrendChart.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: Array.from({length: 30}, (_, i) => `${i+1}日`) },
    yAxis: { type: 'value' },
    series: [{
      data: Array.from({length: 30}, () => Math.floor(Math.random() * 50 + 20)),
      type: 'line',
      smooth: true,
      areaStyle: { color: 'rgba(82, 196, 26, 0.2)' },
      itemStyle: { color: '#52c41a' }
    }]
  })

  // 品类占比图
  const catChart = echarts.init(categoryChart.value)
  catChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie',
      radius: '50%',
      data: [
        { value: 1048, name: '冰箱' },
        { value: 735, name: '空调' },
        { value: 580, name: '洗衣机' },
        { value: 484, name: '电视' },
        { value: 300, name: '电脑' }
      ]
    }]
  })

  // 用户增长图
  const uChart = echarts.init(userChart.value)
  uChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月'] },
    yAxis: { type: 'value' },
    series: [{
      data: [120, 200, 350, 500, 680, 850],
      type: 'bar',
      itemStyle: { color: '#1890ff' }
    }]
  })

  // 环保贡献图
  const eChart = echarts.init(ecoChart.value)
  eChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['回收重量', '减碳量'] },
    xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
    yAxis: { type: 'value' },
    series: [
      { name: '回收重量', type: 'bar', data: [120, 132, 101, 134, 90, 230, 210], itemStyle: { color: '#52c41a' } },
      { name: '减碳量', type: 'line', data: [220, 182, 191, 234, 290, 330, 310], itemStyle: { color: '#722ed1' } }
    ]
  })
}

const loadCollectorRank = async () => {
  try {
    const res = await request.get('/statistics/collectorRanking', { params: { limit: 10 } })
    collectorRank.value = res
  } catch (e) {
    // 模拟数据
    collectorRank.value = [
      { name: '张师傅', orderCount: 156, totalWeight: 3200, rating: 4.9 },
      { name: '李师傅', orderCount: 142, totalWeight: 2850, rating: 4.8 },
      { name: '王师傅', orderCount: 128, totalWeight: 2600, rating: 4.9 },
    ]
  }
}
</script>

<style scoped lang="scss">
.data-analysis {
  .el-card {
    margin-bottom: 0;
  }
}
</style>
<template>
  <div class="dashboard">
    <!-- 数据概览 -->
    <div class="data-overview">
      <div class="data-card primary">
        <div class="icon"><el-icon><Document /></el-icon></div>
        <div class="content">
          <div class="number">{{ overview.totalOrders }}</div>
          <div class="label">总订单数</div>
        </div>
      </div>
      <div class="data-card success">
        <div class="icon"><el-icon><CircleCheck /></el-icon></div>
        <div class="content">
          <div class="number">{{ overview.completedOrders }}</div>
          <div class="label">已完成</div>
        </div>
      </div>
      <div class="data-card warning">
        <div class="icon"><el-icon><Timer /></el-icon></div>
        <div class="content">
          <div class="number">{{ overview.pendingOrders }}</div>
          <div class="label">待处理</div>
        </div>
      </div>
      <div class="data-card info">
        <div class="icon"><el-icon><Weight /></el-icon></div>
        <div class="content">
          <div class="number">{{ overview.totalWeight }}<span>kg</span></div>
          <div class="label">回收重量</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>📊 近7日订单趋势</span>
          </template>
          <div ref="orderChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>♻️ 品类回收占比</span>
          </template>
          <div ref="categoryChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待办事项 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>⏰ 待办事项</span>
          </template>
          <el-timeline>
            <el-timeline-item
                v-for="(item, index) in todos"
                :key="index"
                :type="item.type"
                :timestamp="item.time"
            >
              {{ item.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>🌍 环保贡献</span>
          </template>
          <div class="eco-display">
            <div class="eco-item">
              <div class="value">{{ overview.totalCarbonSaved }}</div>
              <div class="label">减少碳排放(kg)</div>
            </div>
            <div class="eco-item">
              <div class="value">{{ treeCount }}</div>
              <div class="label">相当于种树(棵)</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted,onUnmounted,nextTick,computed } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const overview = ref({
  totalOrders: 0,
  completedOrders: 0,
  pendingOrders: 0,
  totalWeight: 0,
  totalCarbonSaved: 0
})

const todos = ref([
  { content: '有5个订单待派单', time: '刚刚', type: 'warning' },
  { content: '3个回收员注册待审核', time: '10分钟前', type: 'primary' },
  { content: '今日回收目标已完成80%', time: '1小时前', type: 'success' }
])

const treeCount = computed(() => {
  return Math.floor((overview.value.totalCarbonSaved || 0) / 18)
})

// 定义 ref 绑定 DOM 元素
const orderChartRef = ref(null)
const categoryChartRef = ref(null)

// 存储图表实例以便销毁
let orderChartInstance = null
let categoryChartInstance = null

onMounted(() => {
  loadData()
  // 使用 nextTick 确保 DOM 完全渲染后再初始化图表
  nextTick(() => {
    initCharts()
  })
})

onUnmounted(() => {
  // 组件卸载时销毁图表，防止内存泄漏
  if (orderChartInstance) {
    orderChartInstance.dispose()
  }
  if (categoryChartInstance) {
    categoryChartInstance.dispose()
  }
})

const loadData = async () => {
  try {
    const res = await request.get('/statistics/overview')
    overview.value = res
  } catch (e) {
    console.error('加载数据失败:', e)
  }
}

const initCharts = () => {
  // 检查 DOM 是否存在
  if (!orderChartRef.value || !categoryChartRef.value) {
    console.error('图表容器未找到')
    return
  }

  // 订单趋势图 - 使用 ref.value 获取 DOM
  orderChartInstance = echarts.init(orderChartRef.value)
  orderChartInstance.setOption({
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: { type: 'value' },
    series: [{
      data: [120, 200, 150, 80, 70, 110, 130],
      type: 'line',
      smooth: true,
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0.05)' }
          ]
        }
      },
      itemStyle: { color: '#52c41a' }
    }]
  })

  // 品类占比图 - 使用 ref.value 获取 DOM
  categoryChartInstance = echarts.init(categoryChartRef.value)
  categoryChartInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '5%' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: false },
      data: [
        { value: 1048, name: '冰箱', itemStyle: { color: '#5470c6' } },
        { value: 735, name: '空调', itemStyle: { color: '#91cc75' } },
        { value: 580, name: '洗衣机', itemStyle: { color: '#fac858' } },
        { value: 484, name: '电视', itemStyle: { color: '#ee6666' } },
        { value: 300, name: '电脑', itemStyle: { color: '#73c0de' } }
      ]
    }]
  })
}
</script>

<style scoped lang="scss">
.dashboard {
  .data-overview {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    .data-card {
      background: white;
      padding: 20px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      box-shadow: 0 2px 12px rgba(0,0,0,0.05);

      &.primary { border-left: 4px solid #409eff; .icon { color: #409eff; } }
      &.success { border-left: 4px solid #67c23a; .icon { color: #67c23a; } }
      &.warning { border-left: 4px solid #e6a23c; .icon { color: #e6a23c; } }
      &.info { border-left: 4px solid #909399; .icon { color: #909399; } }

      .icon {
        font-size: 40px;
        margin-right: 15px;
      }

      .content {
        .number {
          font-size: 28px;
          font-weight: bold;
          color: #303133;

          span {
            font-size: 14px;
            margin-left: 5px;
          }
        }

        .label {
          color: #909399;
          margin-top: 5px;
        }
      }
    }
  }

  .eco-display {
    display: flex;
    justify-content: space-around;
    padding: 40px 0;

    .eco-item {
      text-align: center;

      .value {
        font-size: 48px;
        font-weight: bold;
        color: #52c41a;
      }

      .label {
        color: #666;
        margin-top: 10px;
      }
    }
  }
}
</style>
<template>
  <div class="income-page">
    <!-- 核心指标卡片 -->
    <div class="stats-cards">
      <div class="stat-card primary">
        <div class="icon">💰</div>
        <div class="info">
          <div class="value">¥{{ todayIncome }}</div>
          <div class="label">今日收入</div>
        </div>
      </div>
      <div class="stat-card success">
        <div class="icon">📦</div>
        <div class="info">
          <div class="value">{{ todayCount }}</div>
          <div class="label">今日单量</div>
        </div>
      </div>
      <div class="stat-card warning">
        <div class="icon">📊</div>
        <div class="info">
          <div class="value">{{ monthCount }}</div>
          <div class="label">本月单量</div>
        </div>
      </div>
      <div class="stat-card purple">
        <div class="icon">⭐</div>
        <div class="info">
          <div class="value">{{ rating }}%</div>
          <div class="label">好评率</div>
        </div>
      </div>
      <div class="stat-card info">
        <div class="icon">💵</div>
        <div class="info">
          <div class="value">¥{{ monthIncome }}</div>
          <div class="label">本月收入</div>
        </div>
      </div>
      <div class="stat-card danger">
        <div class="icon">🏆</div>
        <div class="info">
          <div class="value">¥{{ totalIncome }}</div>
          <div class="label">累计收入</div>
        </div>
      </div>
    </div>

    <!-- 收入趋势图 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>📈 近7日收入趋势</span>
          <el-radio-group v-model="chartType" size="small">
            <el-radio-button label="income">收入</el-radio-button>
            <el-radio-button label="count">单量</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="chartRef" style="height: 350px;"></div>
    </el-card>

    <!-- 收入明细 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>📋 收入明细（最近30天）</span>
      </template>
      <el-table :data="incomeList" stripe>
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="orderCount" label="订单数" width="100" />
        <el-table-column prop="totalWeight" label="总重量(kg)" width="120" />
        <el-table-column prop="avgRating" label="平均评分" width="100">
          <template #default="{ row }">
            <el-rate v-model="row.avgRating" disabled show-score />
          </template>
        </el-table-column>
        <el-table-column prop="income" label="收入(元)">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ row.income }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const user = JSON.parse(localStorage.getItem('user') || '{}')
const todayIncome = ref(0)
const todayCount = ref(0)
const monthCount = ref(0)
const monthIncome = ref(0)
const totalIncome = ref(0)
const rating = ref(98)
const chartType = ref('income')
const incomeList = ref([])
const chartRef = ref(null)
let chartInstance = null

onMounted(() => {
  loadIncome()
  initChart()
})

const loadIncome = async () => {
  try {
    // 获取今日统计
    const todayRes = await request.get(`/collector/todayStats/${user.id}`)
    todayIncome.value = todayRes.todayIncome || 0
    todayCount.value = todayRes.todayOrderCount || 0
    rating.value = ((todayRes.rating || 5) / 5 * 100).toFixed(1)

    // 获取月度统计（需要后端提供新接口）
    const monthRes = await request.get(`/recycleOrder/collectorMonthStats`, {
      params: { collectorId: user.id }
    })
    monthCount.value = monthRes.orderCount || 0
    monthIncome.value = monthRes.income || 0
    totalIncome.value = monthRes.totalIncome || 0

    // 获取明细
    const listRes = await request.get(`/recycleOrder/collectorIncomeList`, {
      params: { collectorId: user.id, days: 30 }
    })
    incomeList.value = listRes
  } catch (e) {
    // 模拟数据
    todayIncome.value = 258
    todayCount.value = 5
    monthCount.value = 68
    monthIncome.value = 6800
    totalIncome.value = 25600
    rating.value = 98.5

    incomeList.value = [
      { date: '2024-01-15', orderCount: 5, totalWeight: 120, avgRating: 5, income: 360 },
      { date: '2024-01-14', orderCount: 4, totalWeight: 95, avgRating: 4.5, income: 285 },
      { date: '2024-01-13', orderCount: 6, totalWeight: 150, avgRating: 5, income: 450 },
    ]
  }
}

const initChart = () => {
  if (chartInstance) {
    chartInstance.dispose()
  }
  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  const isIncome = chartType.value === 'income'
  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      axisLine: { lineStyle: { color: '#ccc' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#eee' } }
    },
    series: [{
      data: isIncome ? [120, 200, 150, 80, 70, 110, 130] : [5, 8, 6, 4, 3, 5, 7],
      type: 'bar',
      barWidth: '40%',
      itemStyle: {
        color: isIncome ? '#52c41a' : '#1890ff',
        borderRadius: [4, 4, 0, 0]
      },
      label: {
        show: true,
        position: 'top',
        formatter: isIncome ? '¥{c}' : '{c}单'
      }
    }]
  }
  chartInstance.setOption(option)
}

watch(chartType, updateChart)
</script>

<style scoped lang="scss">
.income-page {
  .stats-cards {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;

    .stat-card {
      background: white;
      padding: 24px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      box-shadow: 0 2px 12px rgba(0,0,0,0.05);
      transition: all 0.3s;

      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 8px 24px rgba(0,0,0,0.1);
      }

      &.primary { border-left: 4px solid #1890ff; .icon { background: #e6f7ff; color: #1890ff; } }
      &.success { border-left: 4px solid #52c41a; .icon { background: #f6ffed; color: #52c41a; } }
      &.warning { border-left: 4px solid #faad14; .icon { background: #fffbe6; color: #faad14; } }
      &.purple { border-left: 4px solid #722ed1; .icon { background: #f9f0ff; color: #722ed1; } }
      &.info { border-left: 4px solid #13c2c2; .icon { background: #e6fffb; color: #13c2c2; } }
      &.danger { border-left: 4px solid #f5222d; .icon { background: #fff1f0; color: #f5222d; } }

      .icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;
        margin-right: 16px;
      }

      .info {
        .value {
          font-size: 28px;
          font-weight: bold;
          color: #262626;
          margin-bottom: 4px;
        }

        .label {
          color: #8c8c8c;
          font-size: 14px;
        }
      }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr) !important;
  }
}
</style>
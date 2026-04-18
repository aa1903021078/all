<template>
  <div class="statistics-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-title">
        <h2>{{ pageTitle }}</h2>
        <p class="subtitle">{{ pageSubtitle }}</p>
      </div>
      <div class="header-actions">
        <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :shortcuts="dateShortcuts"
            @change="handleDateChange"
        />
        <el-button type="primary" @click="exportData">
          <el-icon><Download /></el-icon>导出数据
        </el-button>
      </div>
    </div>

    <!-- 统计概览卡片 -->
    <el-row :gutter="20" class="stats-overview">
      <el-col v-for="stat in overviewStats" :key="stat.key" :xs="24" :sm="12" :md="6">
        <div class="overview-card" :class="stat.type">
          <div class="card-icon">
            <el-icon><component :is="stat.icon" /></el-icon>
          </div>
          <div class="card-content">
            <p class="card-value">{{ stat.value }}</p>
            <p class="card-label">{{ stat.label }}</p>
            <p class="card-change" :class="stat.trend">
              <el-icon><component :is="stat.trend === 'up' ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
              {{ stat.change }}% 较上期
            </p>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 趋势图 -->
      <el-col :xs="24" :lg="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>{{ trendChartTitle }}</span>
              <el-radio-group v-model="trendType" size="small">
                <el-radio-button label="day">按日</el-radio-button>
                <el-radio-button label="week">按周</el-radio-button>
                <el-radio-button label="month">按月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChart" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 饼图 -->
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>{{ pieChartTitle }}</span>
            </div>
          </template>
          <div ref="pieChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-card class="data-table-card">
      <template #header>
        <div class="card-header">
          <span>详细数据</span>
          <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="total"
              layout="total, sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
          />
        </div>
      </template>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column
            v-for="col in tableColumns"
            :key="col.prop"
            :prop="col.prop"
            :label="col.label"
            :width="col.width"
            :formatter="col.formatter"
        />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  Document, User, Van, Money, Download, ArrowUp, ArrowDown
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()

// 页面类型
const statisticsType = computed(() => {
  const path = route.path
  if (path.includes('orders')) return 'orders'
  if (path.includes('users')) return 'users'
  if (path.includes('revenue')) return 'revenue'
  if (path.includes('environment')) return 'environment'
  return 'orders'
})

// 页面标题
const pageTitle = computed(() => {
  const titles = {
    orders: '订单统计',
    users: '用户统计',
    revenue: '收益统计',
    environment: '环保贡献统计'
  }
  return titles[statisticsType.value]
})

const pageSubtitle = computed(() => {
  const subtitles = {
    orders: '全面分析订单数据，优化回收效率',
    users: '深入了解用户行为，提升服务质量',
    revenue: '掌握收益趋势，优化经营策略',
    environment: '量化环保贡献，共建绿色家园'
  }
  return subtitles[statisticsType.value]
})

const trendChartTitle = computed(() => {
  const titles = {
    orders: '订单趋势',
    users: '用户增长趋势',
    revenue: '收益趋势',
    environment: '碳减排趋势'
  }
  return titles[statisticsType.value]
})

const pieChartTitle = computed(() => {
  const titles = {
    orders: '订单状态分布',
    users: '用户类型分布',
    revenue: '收益来源分布',
    environment: '回收品类分布'
  }
  return titles[statisticsType.value]
})

// 日期范围
const dateRange = ref([])
const dateShortcuts = [
  { text: '最近一周', value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }},
  { text: '最近一月', value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }},
  { text: '最近三月', value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }}
]

// 趋势图类型
const trendType = ref('day')

// 概览统计数据
const overviewStats = computed(() => {
  const configs = {
    orders: [
      { key: 'total', label: '总订单数', icon: 'Document', type: 'primary' },
      { key: 'completed', label: '已完成', icon: 'CircleCheck', type: 'success' },
      { key: 'pending', label: '待处理', icon: 'Timer', type: 'warning' },
      { key: 'cancelled', label: '已取消', icon: 'CircleClose', type: 'danger' }
    ],
    users: [
      { key: 'total', label: '总用户数', icon: 'User', type: 'primary' },
      { key: 'new', label: '新增用户', icon: 'UserFilled', type: 'success' },
      { key: 'active', label: '活跃用户', icon: 'TrendCharts', type: 'warning' },
      { key: 'collectors', label: '回收员数', icon: 'Van', type: 'info' }
    ],
    revenue: [
      { key: 'total', label: '总收益', icon: 'Money', type: 'primary' },
      { key: 'orders', label: '订单收益', icon: 'Document', type: 'success' },
      { key: 'points', label: '积分收益', icon: 'Coin', type: 'warning' },
      { key: 'profit', label: '净利润', icon: 'Wallet', type: 'info' }
    ],
    environment: [
      { key: 'carbon', label: '碳减排(kg)', icon: 'Leaf', type: 'success' },
      { key: 'recycled', label: '回收量(kg)', icon: 'Goods', type: 'primary' },
      { key: 'appliances', label: '回收件数', icon: 'Refrigerator', type: 'warning' },
      { key: 'trees', label: '等效植树', icon: 'Tree', type: 'info' }
    ]
  }

  return configs[statisticsType.value].map(item => ({
    ...item,
    value: getStatValue(item.key),
    change: Math.floor(Math.random() * 30) + 5,
    trend: Math.random() > 0.5 ? 'up' : 'down'
  }))
})

// 模拟统计数据
const statsData = {
  orders: { total: 1258, completed: 986, pending: 198, cancelled: 74 },
  users: { total: 5680, new: 328, active: 2156, collectors: 48 },
  revenue: { total: '¥128,560', orders: '¥98,320', points: '¥30,240', profit: '¥45,680' },
  environment: { carbon: '15,680', recycled: '45,230', appliances: 3256, trees: 856 }
}

const getStatValue = (key) => {
  return statsData[statisticsType.value]?.[key] || 0
}

// 表格数据
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)
const tableData = ref([])

const tableColumns = computed(() => {
  const columns = {
    orders: [
      { prop: 'date', label: '日期', width: 120 },
      { prop: 'orderNo', label: '订单号', width: 150 },
      { prop: 'userName', label: '用户', width: 120 },
      { prop: 'collectorName', label: '回收员', width: 120 },
      { prop: 'applianceType', label: '品类', width: 100 },
      { prop: 'weight', label: '重量(kg)', width: 100 },
      { prop: 'points', label: '积分', width: 100 },
      { prop: 'status', label: '状态', width: 100 },
      { prop: 'createTime', label: '创建时间', width: 160 }
    ],
    users: [
      { prop: 'date', label: '日期', width: 120 },
      { prop: 'newUsers', label: '新增用户', width: 100 },
      { prop: 'activeUsers', label: '活跃用户', width: 100 },
      { prop: 'ordersPerUser', label: '人均订单', width: 100 },
      { prop: 'totalPoints', label: '发放积分', width: 120 },
      { prop: 'recycledWeight', label: '回收重量(kg)', width: 120 }
    ],
    revenue: [
      { prop: 'date', label: '日期', width: 120 },
      { prop: 'orderRevenue', label: '订单收益', width: 120 },
      { prop: 'pointsRevenue', label: '积分收益', width: 120 },
      { prop: 'cost', label: '运营成本', width: 120 },
      { prop: 'profit', label: '净利润', width: 120 },
      { prop: 'profitRate', label: '利润率', width: 100 }
    ],
    environment: [
      { prop: 'date', label: '日期', width: 120 },
      { prop: 'carbonReduction', label: '碳减排(kg)', width: 120 },
      { prop: 'recycledWeight', label: '回收重量(kg)', width: 120 },
      { prop: 'applianceCount', label: '回收件数', width: 100 },
      { prop: 'treesEquivalent', label: '等效植树', width: 100 }
    ]
  }
  return columns[statisticsType.value]
})

// 图表引用
const trendChart = ref(null)
const pieChart = ref(null)
let trendChartInstance = null
let pieChartInstance = null

// 初始化趋势图
const initTrendChart = () => {
  if (!trendChart.value) return

  trendChartInstance = echarts.init(trendChart.value)

  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['本期', '上期'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '本期',
        type: 'line',
        smooth: true,
        data: [120, 132, 101, 134, 90, 230, 210],
        areaStyle: { opacity: 0.3 },
        itemStyle: { color: '#67c23a' }
      },
      {
        name: '上期',
        type: 'line',
        smooth: true,
        data: [220, 182, 191, 234, 290, 330, 310],
        itemStyle: { color: '#909399' }
      }
    ]
  }

  trendChartInstance.setOption(option)
}

// 初始化饼图
const initPieChart = () => {
  if (!pieChart.value) return

  pieChartInstance = echarts.init(pieChart.value)

  const pieData = {
    orders: [
      { value: 986, name: '已完成' },
      { value: 198, name: '待处理' },
      { value: 74, name: '已取消' }
    ],
    users: [
      { value: 5632, name: '普通用户' },
      { value: 48, name: '回收员' }
    ],
    revenue: [
      { value: 98320, name: '订单收益' },
      { value: 30240, name: '积分收益' }
    ],
    environment: [
      { value: 15230, name: '空调' },
      { value: 12850, name: '冰箱' },
      { value: 9870, name: '洗衣机' },
      { value: 7280, name: '电视' }
    ]
  }

  const option = {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', right: '5%', top: 'center' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 16, fontWeight: 'bold' }
        },
        data: pieData[statisticsType.value]
      }
    ]
  }

  pieChartInstance.setOption(option)
}

// 加载表格数据
const loadTableData = async () => {
  loading.value = true
  try {
    // 模拟数据
    tableData.value = Array.from({ length: pageSize.value }, (_, i) => ({
      date: '2024-03-' + String(i + 1).padStart(2, '0'),
      orderNo: 'R20240301' + String(i + 1).padStart(3, '0'),
      userName: '用户' + (i + 1),
      collectorName: '张师傅',
      applianceType: ['空调', '冰箱', '洗衣机', '电视'][i % 4],
      weight: Math.floor(Math.random() * 50) + 10,
      points: Math.floor(Math.random() * 500) + 100,
      status: ['已完成', '待处理', '进行中'][i % 3],
      createTime: '2024-03-01 10:' + String(i * 5).padStart(2, '0'),
      newUsers: Math.floor(Math.random() * 50) + 10,
      activeUsers: Math.floor(Math.random() * 200) + 50,
      ordersPerUser: (Math.random() * 3 + 1).toFixed(1),
      totalPoints: Math.floor(Math.random() * 5000) + 1000,
      recycledWeight: Math.floor(Math.random() * 500) + 100,
      orderRevenue: '¥' + (Math.floor(Math.random() * 5000) + 1000),
      pointsRevenue: '¥' + (Math.floor(Math.random() * 1000) + 200),
      cost: '¥' + (Math.floor(Math.random() * 2000) + 500),
      profit: '¥' + (Math.floor(Math.random() * 3000) + 500),
      profitRate: (Math.random() * 30 + 10).toFixed(1) + '%',
      carbonReduction: Math.floor(Math.random() * 500) + 100,
      applianceCount: Math.floor(Math.random() * 100) + 20,
      treesEquivalent: Math.floor(Math.random() * 50) + 10
    }))
  } finally {
    loading.value = false
  }
}

// 处理日期变化
const handleDateChange = () => {
  loadTableData()
  initTrendChart()
  initPieChart()
}

// 导出数据
const exportData = () => {
  ElMessage.success('数据导出成功')
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  loadTableData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadTableData()
}

// 监听类型变化
watch(statisticsType, () => {
  nextTick(() => {
    initTrendChart()
    initPieChart()
    loadTableData()
  })
})

// 监听趋势类型变化
watch(trendType, () => {
  initTrendChart()
})

onMounted(() => {
  // 设置默认日期范围（最近7天）
  const end = new Date()
  const start = new Date()
  start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
  dateRange.value = [start, end]

  nextTick(() => {
    initTrendChart()
    initPieChart()
    loadTableData()
  })

  // 响应式
  window.addEventListener('resize', () => {
    trendChartInstance?.resize()
    pieChartInstance?.resize()
  })
})
</script>

<style scoped>
.statistics-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-title h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 概览卡片 */
.stats-overview {
  margin-bottom: 20px;
}

.overview-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  transition: transform 0.3s;
}

.overview-card:hover {
  transform: translateY(-4px);
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.card-icon .el-icon {
  font-size: 28px;
  color: #fff;
}

.overview-card.primary .card-icon { background: linear-gradient(135deg, #409eff, #79bbff); }
.overview-card.success .card-icon { background: linear-gradient(135deg, #67c23a, #95d475); }
.overview-card.warning .card-icon { background: linear-gradient(135deg, #e6a23c, #f3d19e); }
.overview-card.danger .card-icon { background: linear-gradient(135deg, #f56c6c, #fab6b6); }
.overview-card.info .card-icon { background: linear-gradient(135deg, #909399, #c0c4cc); }

.card-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 4px 0;
}

.card-label {
  font-size: 14px;
  color: #909399;
  margin: 0 0 8px 0;
}

.card-change {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  margin: 0;
}

.card-change.up {
  color: #67c23a;
}

.card-change.down {
  color: #f56c6c;
}

/* 图表 */
.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 350px;
}

/* 数据表格 */
.data-table-card {
  margin-bottom: 20px;
}

</style>
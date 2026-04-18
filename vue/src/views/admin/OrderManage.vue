<template>
  <div class="order-manage-page">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="title-section">
        <h2>订单管理中心</h2>
        <p class="subtitle">管理全平台回收订单，进行派单和订单监控</p>
      </div>
      <div class="action-section">
        <el-radio-group v-model="viewMode" size="default">
          <el-radio-button label="list">
            <el-icon><List /></el-icon>
            列表
          </el-radio-button>
          <el-radio-button label="card">
            <el-icon><Grid /></el-icon>
            卡片
          </el-radio-button>
        </el-radio-group>
        <el-button type="primary" :loading="refreshing" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card blue">
        <div class="icon-wrap">
          <el-icon><Document /></el-icon>
        </div>
        <div class="content">
          <div class="number">{{ stats.total }}</div>
          <div class="label">总订单数</div>
        </div>
      </div>
      <div class="stat-card orange">
        <div class="icon-wrap">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="content">
          <div class="number">{{ stats.pendingDispatch }}</div>
          <div class="label">待分配</div>
        </div>
      </div>
      <div class="stat-card green">
        <div class="icon-wrap">
          <el-icon><Van /></el-icon>
        </div>
        <div class="content">
          <div class="number">{{ stats.processing }}</div>
          <div class="label">进行中</div>
        </div>
      </div>
      <div class="stat-card purple">
        <div class="icon-wrap">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="content">
          <div class="number">{{ stats.completed }}</div>
          <div class="label">今日完成</div>
        </div>
      </div>
    </div>

    <!-- 标签页 -->
    <div class="tabs-container">
      <div class="custom-tabs">
        <div
            v-for="tab in tabs"
            :key="tab.key"
            :class="['tab-item', { active: activeTab === tab.key }]"
            @click="handleTabChange(tab.key)"
        >
          <el-icon><component :is="tab.icon" /></el-icon>
          <span>{{ tab.label }}</span>
          <el-tag v-if="tab.count > 0" size="small" type="danger" effect="dark" class="tab-badge">
            {{ tab.count }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 筛选栏 - 添加智能派单入口 -->
    <div class="filter-bar">
      <el-input
          v-model="searchForm.orderNo"
          placeholder="请输入订单号"
          style="width: 220px"
          clearable
          @keyup.enter="loadData"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-date-picker
          v-model="searchForm.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 240px"
      />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetFilter">重置</el-button>
      <!-- 智能派单入口 -->
      <el-button type="warning" plain @click="goToSmartDispatch">
        <el-icon><Position /></el-icon>
        智能派单
      </el-button>
    </div>

    <!-- 内容区 -->
    <div class="content-area" v-loading="loading">
      <!-- 列表视图 -->
      <div v-if="viewMode === 'list'" class="data-table">
        <el-table :data="filteredOrders" style="width: 100%" row-key="id">
          <el-table-column type="index" width="50" />
          <el-table-column label="订单号" width="180">
            <template #default="{ row }">
              <span class="order-no">{{ row.orderNo }}</span>
            </template>
          </el-table-column>
          <el-table-column label="物品信息" min-width="180">
            <template #default="{ row }">
              <div class="product-info">
                <div class="icon">{{ getApplianceIcon(row.applianceTypeName) }}</div>
                <div class="detail">
                  <div class="name">{{ row.applianceTypeName }}</div>
                  <div class="weight">{{ row.estimatedWeight }}kg × {{ row.quantity || 1 }}件</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="用户信息" min-width="150">
            <template #default="{ row }">
              <div class="user-info-cell">
                <div>{{ row.contactName || row.userName }}</div>
                <div class="phone">{{ row.contactPhone }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="回收员" min-width="140">
            <template #default="{ row }">
              <div v-if="getCollectorName(row)" class="collector-cell">
                <div class="name">{{ getCollectorName(row) }}</div>
                <div class="phone">{{ getCollectorPhone(row) }}</div>
              </div>
              <el-tag v-else type="info" size="small" effect="plain">未分配</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="地址" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="address-cell">{{ row.addressDetail }}</div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" effect="dark" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="预约时间" width="180">
            <template #default="{ row }">
              <div class="time-info">
                <div>{{ row.expectDate }}</div>
                <div class="time">{{ row.expectTimeStart }}-{{ row.expectTimeEnd }}</div>
              </div>
            </template>
          </el-table-column>
          <!-- 操作列 - 根据标签页显示不同按钮 -->
          <el-table-column label="操作" width="280" fixed="right" align="center">
            <template #default="{ row }">
              <el-button size="small" @click="viewDetail(row)">详情</el-button>

              <!-- 待分配标签页：显示主要派单按钮（实心） -->
              <el-button
                  v-if="row.status === 0 && activeTab === 'pending'"
                  type="success"
                  size="small"
                  @click="openDispatch(row)"
              >
                派单
              </el-button>

              <!-- 全部订单标签页：显示弱化派单按钮（文字链接） -->
              <el-button
                  v-if="row.status === 0 && activeTab === 'all'"
                  type="success"
                  link
                  size="small"
                  @click="openDispatch(row)"
              >
                派单
              </el-button>

              <!-- 取消按钮（待分配状态显示） -->
              <el-button
                  v-if="row.status === 0"
                  type="danger"
                  link
                  size="small"
                  @click="cancelOrder(row)"
              >
                取消
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 卡片视图 -->
      <div v-else class="card-grid">
        <div v-for="order in filteredOrders" :key="order.id" class="order-card" :class="'status-' + order.status">
          <div class="status-bar"></div>
          <div class="card-header">
            <span class="order-id">{{ order.orderNo }}</span>
            <el-tag :type="getStatusType(order.status)" size="small" effect="dark">
              {{ getStatusText(order.status) }}
            </el-tag>
          </div>

          <!-- 进度条（进行中的订单） -->
          <div v-if="order.status >= 1 && order.status <= 3" class="mini-progress">
            <div class="step" :class="{ active: order.status >= 1 }">待接单</div>
            <div class="line" :class="{ active: order.status >= 2 }"></div>
            <div class="step" :class="{ active: order.status >= 2 }">已接单</div>
            <div class="line" :class="{ active: order.status >= 3 }"></div>
            <div class="step" :class="{ active: order.status >= 3 }">上门中</div>
          </div>

          <div class="card-body">
            <div class="product">
              <span class="icon">{{ getApplianceIcon(order.applianceTypeName) }}</span>
              <div class="info">
                <h4>{{ order.applianceTypeName }}</h4>
                <p>{{ order.estimatedWeight }}kg × {{ order.quantity || 1 }}件</p>
              </div>
            </div>

            <div class="contact-section">
              <div class="contact-item">
                <span class="label">用户：</span>
                <span class="value">{{ order.contactName || order.userName }} {{ order.contactPhone }}</span>
              </div>
              <div class="contact-item">
                <span class="label">回收员：</span>
                <span class="value" v-if="getCollectorName(order)">{{ getCollectorName(order) }}</span>
                <el-tag v-else type="warning" size="small">待分配</el-tag>
              </div>
              <div class="contact-item">
                <el-icon><Location /></el-icon>
                <span class="text-truncate">{{ order.addressDetail }}</span>
              </div>
            </div>
          </div>

          <!-- 卡片操作按钮 - 根据标签页显示 -->
          <div class="card-footer">
            <el-button size="small" @click="viewDetail(order)">查看详情</el-button>

            <template v-if="order.status === 0 && activeTab === 'pending'">
              <el-button type="success" size="small" @click="openDispatch(order)">立即派单</el-button>
            </template>

            <template v-if="order.status === 0 && activeTab === 'all'">
              <el-button type="success" link size="small" @click="openDispatch(order)">派单</el-button>
            </template>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            @change="loadData"
        />
      </div>

      <el-empty v-if="filteredOrders.length === 0" :description="emptyText" />
    </div>

    <!-- 订单详情抽屉 -->
    <el-drawer
        v-model="drawerVisible"
        title="订单详情"
        size="600px"
        destroy-on-close
    >
      <div v-if="currentOrder.id" class="order-detail">
        <!-- 状态时间线 -->
        <div class="detail-section" v-if="currentOrder.status !== 5">
          <div class="section-title">订单进度</div>
          <div class="progress-timeline">
            <div class="timeline-item" :class="{ active: currentOrder.status >= 0, current: currentOrder.status === 0 }">
              <div class="dot">1</div>
              <div class="label">待分配</div>
            </div>
            <div class="timeline-line" :class="{ active: currentOrder.status >= 1 }"></div>
            <div class="timeline-item" :class="{ active: currentOrder.status >= 1, current: currentOrder.status === 1 }">
              <div class="dot">2</div>
              <div class="label">待接单</div>
            </div>
            <div class="timeline-line" :class="{ active: currentOrder.status >= 2 }"></div>
            <div class="timeline-item" :class="{ active: currentOrder.status >= 2, current: currentOrder.status === 2 }">
              <div class="dot">3</div>
              <div class="label">已接单</div>
            </div>
            <div class="timeline-line" :class="{ active: currentOrder.status >= 3 }"></div>
            <div class="timeline-item" :class="{ active: currentOrder.status >= 3, current: currentOrder.status === 3 }">
              <div class="dot">4</div>
              <div class="label">已到达</div>
            </div>
            <div class="timeline-line" :class="{ active: currentOrder.status >= 4 }"></div>
            <div class="timeline-item" :class="{ active: currentOrder.status >= 4 }">
              <div class="dot">5</div>
              <div class="label">已完成</div>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">基本信息</div>
          <div class="info-list">
            <div class="info-item">
              <span class="label">订单编号</span>
              <span class="value">{{ currentOrder.orderNo }}</span>
            </div>
            <div class="info-item">
              <span class="label">创建时间</span>
              <span class="value">{{ formatDateTime(currentOrder.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">订单状态</span>
              <el-tag :type="getStatusType(currentOrder.status)" effect="dark" size="small">
                {{ getStatusText(currentOrder.status) }}
              </el-tag>
            </div>
            <div class="info-item" v-if="currentOrder.cancelReason">
              <span class="label">取消原因</span>
              <span class="value text-danger">{{ currentOrder.cancelReason }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">物品信息</div>
          <div class="product-display">
            <div class="big-icon">{{ getApplianceIcon(currentOrder.applianceTypeName) }}</div>
            <div class="product-detail">
              <h3>{{ currentOrder.applianceTypeName }}</h3>
              <p>预估重量：{{ currentOrder.estimatedWeight }}kg × {{ currentOrder.quantity || 1 }}件</p>
              <p v-if="currentOrder.weightActual">实际重量：{{ currentOrder.weightActual }}kg</p>
              <p v-if="currentOrder.priceEstimate" class="price">预估价格：¥{{ currentOrder.priceEstimate }}</p>
              <p v-if="currentOrder.priceActual" class="price">实际金额：¥{{ currentOrder.priceActual }}</p>
            </div>
          </div>
          <div class="desc-box" v-if="currentOrder.applianceDesc">
            <div class="label">物品描述</div>
            <div class="content">{{ currentOrder.applianceDesc }}</div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">用户信息</div>
          <div class="info-list">
            <div class="info-item">
              <span class="label">联系人</span>
              <span class="value">{{ currentOrder.contactName || currentOrder.userName }}</span>
            </div>
            <div class="info-item">
              <span class="label">联系电话</span>
              <span class="value">{{ currentOrder.contactPhone }}</span>
            </div>
            <div class="info-item block">
              <span class="label">服务地址</span>
              <span class="value">{{ currentOrder.addressDetail }}</span>
            </div>
            <div class="info-item" v-if="currentOrder.buildingType">
              <span class="label">房屋类型</span>
              <span class="value">{{ currentOrder.buildingType === 1 ? '电梯房' : `楼梯房${currentOrder.floor}层` }}</span>
            </div>
            <div class="info-item">
              <span class="label">预约时间</span>
              <span class="value">{{ currentOrder.expectDate }} {{ currentOrder.expectTimeStart }}-{{ currentOrder.expectTimeEnd }}</span>
            </div>
          </div>
        </div>

        <!-- 已分配回收员显示 -->
        <div class="detail-section" v-if="hasCollector">
          <div class="section-title">回收员信息</div>
          <div class="info-list">
            <div class="info-item">
              <span class="label">回收员</span>
              <span class="value">
                {{ currentOrder.collectorName || currentOrder.collector?.name || currentOrder.collector?.username || '未知' }}
              </span>
            </div>
            <div class="info-item">
              <span class="label">联系电话</span>
              <span class="value">
                {{ currentOrder.collectorPhone || currentOrder.collector?.phone || currentOrder.collector?.tel || '-' }}
              </span>
            </div>
            <div class="info-item" v-if="currentOrder.acceptTime">
              <span class="label">接单时间</span>
              <span class="value">{{ formatDateTime(currentOrder.acceptTime) }}</span>
            </div>
            <div class="info-item" v-if="currentOrder.arriveTime">
              <span class="label">到达时间</span>
              <span class="value">{{ formatDateTime(currentOrder.arriveTime) }}</span>
            </div>
            <div class="info-item" v-if="currentOrder.completeTime">
              <span class="label">完成时间</span>
              <span class="value">{{ formatDateTime(currentOrder.completeTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 未分配提示 -->
        <div class="detail-section alert-section" v-else-if="currentOrder.status === 0">
          <el-alert
              title="该订单待分配回收员"
              type="warning"
              :closable="false"
              show-icon
          >
            <template #default>
              <div style="margin-top: 8px;">
                <el-button type="success" size="small" @click="openDispatch(currentOrder)">
                  立即派单
                </el-button>
              </div>
            </template>
          </el-alert>
        </div>

        <div class="detail-section eco-section" v-if="currentOrder.status === 4">
          <div class="section-title">🌱 环保贡献</div>
          <div class="eco-stats">
            <div class="eco-item">
              <div class="eco-value">{{ currentOrder.pointsEarned || 0 }}</div>
              <div class="eco-label">获得积分</div>
            </div>
            <div class="eco-item">
              <div class="eco-value">{{ currentOrder.carbonSaved || 0 }}kg</div>
              <div class="eco-label">碳减排量</div>
            </div>
          </div>
        </div>

        <div class="detail-actions" v-if="currentOrder.status === 0">
          <el-button type="success" size="large" style="width: 100%" @click="openDispatch(currentOrder)">
            立即派单
          </el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 派单弹窗 -->
    <el-dialog
        v-model="dispatchVisible"
        title="订单派单"
        width="500px"
        :close-on-click-modal="false"
    >
      <div class="dispatch-info" v-if="currentOrder.id">
        <div class="order-brief">
          <div class="label">订单号：</div>
          <div class="value">{{ currentOrder.orderNo }}</div>
        </div>
        <div class="order-brief">
          <div class="label">回收物品：</div>
          <div class="value">{{ currentOrder.applianceTypeName }} {{ currentOrder.estimatedWeight }}kg</div>
        </div>
        <div class="order-brief">
          <div class="label">服务地址：</div>
          <div class="value">{{ currentOrder.addressDetail }}</div>
        </div>
        <div class="order-brief">
          <div class="label">预约时间：</div>
          <div class="value">{{ currentOrder.expectDate }} {{ currentOrder.expectTimeStart }}</div>
        </div>
      </div>

      <el-divider />

      <el-form :model="dispatchForm" label-position="top">
        <el-form-item label="选择回收员" required>
          <el-select
              v-model="dispatchForm.collectorId"
              placeholder="请选择回收员"
              style="width: 100%"
              filterable
          >
            <el-option
                v-for="item in collectors"
                :key="item.id"
                :label="`${item.name} (${item.phone}) - 距用户${item.distance || '--'}km`"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dispatchVisible = false">取消</el-button>
          <el-button type="primary" @click="submitDispatch" :loading="submitting">确认派单</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, reactive, computed, onMounted, watch} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  List,
  Grid,
  Refresh,
  Location,
  Clock,
  Document,
  Bell,
  CircleCheck,
  Van,
  Search,
  User,
  Position
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

// 视图状态
const activeTab = ref('all')
const viewMode = ref('list')
const loading = ref(false)
const refreshing = ref(false)

// 标签页配置
const tabs = ref([
  {key: 'all', label: '全部订单', icon: 'Document', count: 0},
  {key: 'pending', label: '待分配', icon: 'Bell', count: 0},
  {key: 'processing', label: '进行中', icon: 'Van', count: 0},
  {key: 'completed', label: '已完成', icon: 'CircleCheck', count: 0},
  {key: 'cancelled', label: '已取消', icon: 'CircleClose', count: 0}
])

// 统计数据
const stats = reactive({
  total: 0,
  pendingDispatch: 0,
  processing: 0,
  completed: 0
})

// 数据列表
const allOrders = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  dateRange: null
})

// 弹窗控制
const drawerVisible = ref(false)
const dispatchVisible = ref(false)
const currentOrder = ref({})
const submitting = ref(false)

// 派单表单
const dispatchForm = reactive({
  orderId: null,
  collectorId: null
})

// 回收员列表
const collectors = ref([])

// 计算属性：是否有回收员（用于详情页显示判断）
const hasCollector = computed(() => {
  return !!(currentOrder.value.collectorName ||
      currentOrder.value.collector?.name ||
      currentOrder.value.collector?.username ||
      currentOrder.value.collectorId)
})

// 计算属性：筛选后的订单
const filteredOrders = computed(() => {
  let result = allOrders.value

  // 根据标签页筛选
  if (activeTab.value === 'pending') {
    result = result.filter(o => o.status === 0)
  } else if (activeTab.value === 'processing') {
    result = result.filter(o => o.status === 1 || o.status === 2 || o.status === 3)
  } else if (activeTab.value === 'completed') {
    result = result.filter(o => o.status === 4)
  } else if (activeTab.value === 'cancelled') {
    result = result.filter(o => o.status === 5)
  }

  return result
})

const emptyText = computed(() => {
  const texts = {
    'all': '暂无订单数据',
    'pending': '暂无待分配订单',
    'processing': '暂无进行中的订单',
    'completed': '今日暂无已完成订单',
    'cancelled': '暂无已取消订单'
  }
  return texts[activeTab.value] || '暂无数据'
})

// 生命周期
onMounted(() => {
  loadData()
  loadCollectors()
})

// 加载订单数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      orderNo: searchForm.orderNo
    }

    // 如果有日期范围，添加参数
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }

    const res = await request.get('/recycleOrder/selectPage', {params})

    if (res.code === '200' || res.code === 200) {
      allOrders.value = res.list || res.data?.list || []
      total.value = res.total || res.data?.total || 0

      // 更新统计
      updateStats()
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error('网络错误，请检查连接')
  } finally {
    loading.value = false
  }
}

// 更新统计数据
const updateStats = () => {
  stats.total = allOrders.value.length
  stats.pendingDispatch = allOrders.value.filter(o => o.status === 0).length
  stats.processing = allOrders.value.filter(o => o.status === 1 || o.status === 2 || o.status === 3).length

  // 今日完成
  const today = new Date().toDateString()
  stats.completed = allOrders.value.filter(o => {
    return o.status === 4 && o.completeTime && new Date(o.completeTime).toDateString() === today
  }).length

  // 更新标签计数
  tabs.value[0].count = stats.total
  tabs.value[1].count = stats.pendingDispatch
  tabs.value[2].count = stats.processing
  tabs.value[3].count = stats.completed
  tabs.value[4].count = allOrders.value.filter(o => o.status === 5).length
}

// 加载回收员列表
const loadCollectors = async () => {
  try {
    const res = await request.get('/collector/selectAll')
    if (res.code === '200' || res.code === 200) {
      collectors.value = res.data || []
    }
  } catch (error) {
    console.error('加载回收员失败:', error)
  }
}

// 刷新数据
const refreshData = async () => {
  refreshing.value = true
  try {
    await loadData()
    ElMessage.success('刷新成功')
  } catch (e) {
    ElMessage.error('刷新失败')
  } finally {
    refreshing.value = false
  }
}

// 标签切换
const handleTabChange = (tab) => {
  activeTab.value = tab
  pageNum.value = 1
  loadData()
}

// 重置筛选
const resetFilter = () => {
  searchForm.orderNo = ''
  searchForm.dateRange = null
  loadData()
}

// 跳转到智能派单页面
const goToSmartDispatch = () => {
  router.push('/admin/dispatch')
}

// 查看详情 - 调用详情接口补全数据
const viewDetail = async (order) => {
  // 初始化并显示基础信息
  currentOrder.value = {}
  drawerVisible.value = true

  // 构建新对象
  let orderData = {...order}

  // 调用详情接口补全数据
  try {
    const res = await request.get(`/recycleOrder/selectById/${order.id}`)

    if ((res.code === '200' || res.code === 200) && res.data) {
      orderData = {...orderData, ...res.data}
    }

    // 如果有 collectorId 但没有名字，查询回收员信息
    const collectorId = orderData.collectorId || orderData.collector?.id

    if (collectorId && !orderData.collectorName) {
      try {
        const collectorRes = await request.get(`/collector/selectById/${collectorId}`)

        if ((collectorRes.code === '200' || collectorRes.code === 200) && collectorRes.data) {
          const collector = collectorRes.data
          orderData.collectorName = collector.name || collector.username || '未知'
          orderData.collectorPhone = collector.phone || collector.tel || '-'
          orderData.collector = collector
        }
      } catch (e) {
        console.error('获取回收员信息失败:', e)
      }
    }

    // 一次性赋值，确保响应式更新
    currentOrder.value = orderData
  } catch (error) {
    console.error('获取详情失败:', error)
    currentOrder.value = orderData
  }
}

// 打开派单弹窗
const openDispatch = (order) => {
  currentOrder.value = order
  dispatchForm.orderId = order.id
  dispatchForm.collectorId = null
  dispatchVisible.value = true
}

// 提交派单
const submitDispatch = async () => {
  if (!dispatchForm.collectorId) {
    ElMessage.warning('请选择回收员')
    return
  }

  submitting.value = true
  try {
    const res = await request.post('/recycleOrder/dispatch', null, {
      params: {
        orderId: dispatchForm.orderId,
        collectorId: dispatchForm.collectorId
      }
    })

    if (res.code === '200' || res.code === 200) {
      ElMessage.success('派单成功')
      dispatchVisible.value = false
      await loadData()
    } else {
      ElMessage.error(res.msg || '派单失败')
    }
  } catch (error) {
    ElMessage.error('派单失败')
  } finally {
    submitting.value = false
  }
}

// 取消订单
const cancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '确认取消', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await request.post('/recycleOrder/cancel', null, {
      params: {
        orderId: order.id,
        reason: '管理员取消'
      }
    })

    if (res.code === '200' || res.code === 200) {
      ElMessage.success('订单已取消')
      await loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

// 兼容性方法：获取回收员姓名
const getCollectorName = (row) => {
  if (!row) return null
  return row.collectorName ||
      row.collector?.name ||
      row.collector?.username ||
      row.realName ||
      null
}

// 兼容性方法：获取回收员电话
const getCollectorPhone = (row) => {
  if (!row) return '-'
  return row.collectorPhone ||
      row.collector?.phone ||
      row.collector?.tel ||
      '-'
}

// 工具函数
const getApplianceIcon = (name) => {
  const icons = {
    '冰箱': '❄️',
    '洗衣机': '🌀',
    '空调': '🌡️',
    '电视': '📺',
    '电脑': '💻',
    '手机': '📱',
    '热水器': '🔥',
    '微波炉': '🍲'
  }
  for (let key in icons) if (name?.includes(key)) return icons[key]
  return '📦'
}

const getStatusText = (status) => {
  const map = {0: '待分配', 1: '待接单', 2: '已接单', 3: '已到达', 4: '已完成', 5: '已取消'}
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = {0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'success', 5: 'danger'}
  return map[status] || 'info'
}

const formatDateTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 监听标签切换
watch(activeTab, () => {
  loadData()
})
</script>

<style scoped lang="scss">
.order-manage-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    background: white;
    padding: 24px;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

    .title-section {
      h2 {
        margin: 0 0 8px 0;
        font-size: 24px;
        color: #1f2937;
      }

      .subtitle {
        margin: 0;
        color: #6b7280;
        font-size: 14px;
      }
    }

    .action-section {
      display: flex;
      gap: 12px;
    }
  }

  .stats-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 24px;

    .stat-card {
      background: white;
      border-radius: 12px;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

      &.blue {
        border-left: 4px solid #3b82f6;

        .icon-wrap {
          background: #eff6ff;
          color: #3b82f6;
        }
      }

      &.orange {
        border-left: 4px solid #f59e0b;

        .icon-wrap {
          background: #fffbeb;
          color: #f59e0b;
        }
      }

      &.green {
        border-left: 4px solid #10b981;

        .icon-wrap {
          background: #f0fdf4;
          color: #10b981;
        }
      }

      &.purple {
        border-left: 4px solid #8b5cf6;

        .icon-wrap {
          background: #faf5ff;
          color: #8b5cf6;
        }
      }

      .icon-wrap {
        width: 48px;
        height: 48px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
      }

      .content {
        .number {
          font-size: 24px;
          font-weight: 700;
          color: #1f2937;
          line-height: 1;
        }

        .label {
          font-size: 14px;
          color: #6b7280;
          margin-top: 4px;
        }
      }
    }
  }

  .tabs-container {
    background: white;
    border-radius: 12px 12px 0 0;
    padding: 0 20px;
    border-bottom: 1px solid #e5e7eb;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

    .custom-tabs {
      display: flex;
      gap: 8px;

      .tab-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 16px 24px;
        cursor: pointer;
        color: #6b7280;
        font-weight: 500;
        border-bottom: 2px solid transparent;
        transition: all 0.2s;
        position: relative;

        &:hover {
          color: #1f2937;
        }

        &.active {
          color: #409eff;
          border-bottom-color: #409eff;
        }

        .tab-badge {
          position: absolute;
          right: 0;
          top: 50%;
          transform: translateY(-50%);
        }
      }
    }
  }

  .filter-bar {
    background: white;
    padding: 16px 20px;
    display: flex;
    gap: 12px;
    align-items: center;
    border-bottom: 1px solid #e5e7eb;
  }

  .content-area {
    background: white;
    border-radius: 0 0 12px 12px;
    padding: 24px;
    min-height: 500px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

    .data-table {
      .product-info {
        display: flex;
        align-items: center;
        gap: 12px;

        .icon {
          font-size: 24px;
          width: 40px;
          height: 40px;
          background: #f3f4f6;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .detail {
          .name {
            font-weight: 600;
            color: #1f2937;
            margin-bottom: 2px;
          }

          .weight {
            font-size: 13px;
            color: #6b7280;
          }
        }
      }

      .order-no {
        font-family: monospace;
        color: #6b7280;
        font-weight: 600;
      }

      .user-info-cell {
        .phone {
          font-size: 13px;
          color: #6b7280;
        }
      }

      .collector-cell {
        .name {
          font-weight: 500;
          color: #1f2937;
        }

        .phone {
          font-size: 13px;
          color: #6b7280;
        }
      }

      .address-cell {
        color: #6b7280;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .time-info {
        .time {
          font-size: 13px;
          color: #6b7280;
        }
      }
    }

    .card-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
      gap: 20px;
      margin-bottom: 20px;

      .order-card {
        background: white;
        border: 1px solid #e5e7eb;
        border-radius: 12px;
        overflow: hidden;
        transition: all 0.3s;
        position: relative;

        &:hover {
          box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
          transform: translateY(-2px);
        }

        .status-bar {
          height: 4px;
          background: #e5e7eb;

          &.status-0 {
            background: #909399;
          }

          &.status-1 {
            background: #f59e0b;
          }

          &.status-2 {
            background: #3b82f6;
          }

          &.status-3 {
            background: #10b981;
          }

          &.status-4 {
            background: #67c23a;
          }

          &.status-5 {
            background: #f56c6c;
          }
        }

        .card-header {
          padding: 16px;
          display: flex;
          justify-content: space-between;
          align-items: center;
          background: #f9fafb;
          border-bottom: 1px solid #e5e7eb;

          .order-id {
            font-family: monospace;
            color: #6b7280;
            font-weight: 600;
            font-size: 14px;
          }
        }

        .mini-progress {
          display: flex;
          align-items: center;
          padding: 12px 16px;
          background: #fafafa;
          border-bottom: 1px solid #e5e7eb;

          .step {
            font-size: 12px;
            color: #9ca3af;
            font-weight: 500;
            white-space: nowrap;

            &.active {
              color: #10b981;
            }
          }

          .line {
            flex: 1;
            height: 2px;
            background: #e5e7eb;
            margin: 0 8px;

            &.active {
              background: #10b981;
            }
          }
        }

        .card-body {
          padding: 16px;

          .product {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 16px;

            .icon {
              font-size: 32px;
              width: 56px;
              height: 56px;
              background: #f3f4f6;
              border-radius: 12px;
              display: flex;
              align-items: center;
              justify-content: center;
            }

            .info {
              h4 {
                margin: 0 0 4px 0;
                color: #1f2937;
              }

              p {
                margin: 0;
                color: #6b7280;
                font-size: 14px;
              }
            }
          }

          .contact-section {
            margin: 12px 0;
            padding: 12px;
            background: #f9fafb;
            border-radius: 8px;
            font-size: 13px;
            color: #4b5563;

            .contact-item {
              display: flex;
              align-items: center;
              gap: 6px;
              margin-bottom: 6px;

              &:last-child {
                margin-bottom: 0;
              }

              .label {
                color: #9ca3af;
                flex-shrink: 0;
              }

              .value {
                flex: 1;
              }

              .el-icon {
                color: #9ca3af;
              }

              .text-truncate {
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }
            }
          }
        }

        .card-footer {
          padding: 16px;
          border-top: 1px solid #e5e7eb;
          display: flex;
          gap: 8px;
          justify-content: flex-end;
        }
      }
    }

    .pagination-wrap {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .order-detail {
    padding: 20px;

    .detail-section {
      margin-bottom: 24px;

      .section-title {
        font-size: 16px;
        font-weight: 700;
        color: #1f2937;
        margin-bottom: 16px;
        padding-bottom: 8px;
        border-bottom: 2px solid #f3f4f6;
      }

      .info-list {
        .info-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 12px 0;
          border-bottom: 1px solid #f3f4f6;

          &.block {
            flex-direction: column;
            align-items: flex-start;
            gap: 8px;

            .value {
              color: #4b5563;
              line-height: 1.5;
            }
          }

          .label {
            color: #6b7280;
            font-size: 14px;
          }

          .value {
            color: #1f2937;
            font-weight: 500;

            &.text-danger {
              color: #ef4444;
            }
          }
        }
      }

      .product-display {
        display: flex;
        gap: 16px;
        align-items: center;
        margin-bottom: 16px;

        .big-icon {
          font-size: 48px;
          width: 80px;
          height: 80px;
          background: #f3f4f6;
          border-radius: 16px;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .product-detail {
          h3 {
            margin: 0 0 8px 0;
            color: #1f2937;
          }

          p {
            margin: 0 0 4px 0;
            color: #6b7280;
          }

          .price {
            color: #ef4444;
            font-weight: 700;
            font-size: 18px;
          }
        }
      }

      .desc-box {
        background: #f9fafb;
        padding: 16px;
        border-radius: 8px;

        .label {
          font-size: 12px;
          color: #6b7280;
          margin-bottom: 8px;
        }

        .content {
          color: #4b5563;
          line-height: 1.6;
        }
      }

      .progress-timeline {
        display: flex;
        align-items: center;
        padding: 20px 0;

        .timeline-item {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 6px;

          .dot {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background: #e5e7eb;
            color: #9ca3af;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 14px;
            font-weight: 600;
            transition: all 0.3s;
          }

          .label {
            font-size: 12px;
            color: #9ca3af;
          }

          &.active {
            .dot {
              background: #10b981;
              color: white;
            }

            .label {
              color: #10b981;
              font-weight: 500;
            }
          }

          &.current {
            .dot {
              box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2);
              animation: pulse 2s infinite;
            }
          }
        }

        .timeline-line {
          flex: 1;
          height: 2px;
          background: #e5e7eb;
          margin: 0 8px;
          margin-bottom: 20px;

          &.active {
            background: #10b981;
          }
        }

        @keyframes pulse {
          0% {
            box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4);
          }
          70% {
            box-shadow: 0 0 0 6px rgba(16, 185, 129, 0);
          }
          100% {
            box-shadow: 0 0 0 0 rgba(16, 185, 129, 0);
          }
        }
      }

      &.alert-section {
        .el-alert {
          padding: 20px;
        }
      }

      &.eco-section {
        background: #f6ffed;
        padding: 20px;
        border-radius: 12px;
        border: 1px solid #b7eb8f;

        .section-title {
          border-bottom: none;
          margin-bottom: 16px;
          color: #52c41a;
        }

        .eco-stats {
          display: flex;
          gap: 40px;

          .eco-item {
            text-align: center;

            .eco-value {
              font-size: 28px;
              font-weight: 600;
              color: #52c41a;
              line-height: 1.2;
            }

            .eco-label {
              font-size: 13px;
              color: #73d13d;
              margin-top: 4px;
            }
          }
        }
      }
    }

    .detail-actions {
      position: fixed;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 20px 24px;
      background: white;
      border-top: 1px solid #e5e7eb;
      box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.05);
    }
  }

  .dispatch-info {
    background: #f5f7fa;
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 16px;

    .order-brief {
      display: flex;
      margin-bottom: 8px;
      font-size: 14px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        color: #909399;
        width: 80px;
        flex-shrink: 0;
      }

      .value {
        color: #303133;
        flex: 1;
        font-weight: 500;
      }
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}

@media (max-width: 1200px) {
  .order-manage-page {
    .stats-row {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}
</style>
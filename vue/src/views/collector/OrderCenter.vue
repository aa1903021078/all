<template>
  <div class="order-center-page">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="title-section">
        <h2>订单中心</h2>
        <p class="subtitle">管理回收订单，查看待抢订单和进行中的任务</p>
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
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card blue">
        <div class="icon-wrap">
          <el-icon><Grid /></el-icon>
        </div>
        <div class="content">
          <div class="number">{{ stats.hallCount }}</div>
          <div class="label">抢单大厅</div>
        </div>
      </div>
      <div class="stat-card orange">
        <div class="icon-wrap">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="content">
          <div class="number">{{ stats.pendingCount }}</div>
          <div class="label">待接单</div>
        </div>
      </div>
      <div class="stat-card green">
        <div class="icon-wrap">
          <el-icon><Van /></el-icon>
        </div>
        <div class="content">
          <div class="number">{{ stats.processingCount }}</div>
          <div class="label">进行中</div>
        </div>
      </div>
      <div class="stat-card purple">
        <div class="icon-wrap">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="content">
          <div class="number">{{ stats.completedCount }}</div>
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

    <!-- 内容区 -->
    <div class="content-area" v-loading="loading">
      <!-- 抢单大厅 -->
      <template v-if="activeTab === 'hall'">
        <div class="filter-bar">
          <el-radio-group v-model="hallFilter" size="small" @change="applyHallFilter">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="nearby">附近优先</el-radio-button>
            <el-radio-button label="price">高价优先</el-radio-button>
            <el-radio-button label="urgent">紧急</el-radio-button>
          </el-radio-group>
        </div>

        <div v-if="viewMode === 'list'" class="data-table">
          <el-table :data="hallList" style="width: 100%" row-key="id">
            <el-table-column type="index" width="50" />
            <el-table-column label="物品信息" min-width="200">
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
            <el-table-column label="预估价格" width="120" align="center">
              <template #default="{ row }">
                <span class="price-text">¥{{ row.priceEstimate || '--' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="距离/地址" min-width="220">
              <template #default="{ row }">
                <div class="location-info">
                  <div class="distance">
                    <el-icon><Location /></el-icon>
                    <span>{{ row.distance ? row.distance + 'km' : '未知' }}</span>
                  </div>
                  <div class="address">{{ row.addressDetail }}</div>
                </div>
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
            <el-table-column label="紧急度" width="100" align="center">
              <template #default="{ row }">
                <el-tag
                    :type="row.urgencyLevel === 3 ? 'danger' : row.urgencyLevel === 2 ? 'warning' : 'info'"
                    size="small"
                    effect="dark"
                >
                  {{ row.urgencyLevel === 3 ? '紧急' : row.urgencyLevel === 2 ? '较急' : '普通' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="viewDetail(row)">查看</el-button>
                <el-button
                    type="success"
                    size="small"
                    :loading="row._grabbing"
                    @click="handleGrab(row)"
                >
                  抢单
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-else class="card-grid">
          <div v-for="order in hallList" :key="order.id" class="order-card">
            <div class="card-header">
              <div class="urgency-badge" v-if="order.urgencyLevel === 3">紧急</div>
              <div class="distance">{{ order.distance ? order.distance + 'km' : '距离未知' }}</div>
            </div>
            <div class="card-body">
              <div class="product">
                <span class="icon">{{ getApplianceIcon(order.applianceTypeName) }}</span>
                <div class="info">
                  <h4>{{ order.applianceTypeName }}</h4>
                  <p>{{ order.estimatedWeight }}kg × {{ order.quantity || 1 }}件</p>
                </div>
              </div>
              <div class="price-row">
                <span class="label">预估价格</span>
                <span class="value">¥{{ order.priceEstimate || '--' }}</span>
              </div>
              <div class="address-row">
                <el-icon><Location /></el-icon>
                <span>{{ truncateAddress(order.addressDetail, 30) }}</span>
              </div>
              <div class="time-row">
                <el-icon><Clock /></el-icon>
                <span>{{ order.expectDate }} {{ order.expectTimeStart }}</span>
              </div>
            </div>
            <div class="card-footer">
              <el-button type="primary" plain @click="viewDetail(order)">查看详情</el-button>
              <el-button type="success" :loading="order._grabbing" @click="handleGrab(order)">立即抢单</el-button>
            </div>
          </div>
        </div>

        <el-empty v-if="hallList.length === 0" description="暂无待分配订单" />
      </template>

      <!-- 我的订单（待接单/进行中/已完成） -->
      <template v-else>
        <div v-if="viewMode === 'list'" class="data-table">
          <el-table :data="filteredMyOrders" style="width: 100%">
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
                    <div class="weight">{{ row.estimatedWeight }}kg</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="用户信息" min-width="180">
              <template #default="{ row }">
                <div class="user-info-cell">
                  <div>{{ row.contactName }}</div>
                  <div class="phone">{{ row.contactPhone }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="地址" min-width="200">
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
            <el-table-column label="操作" width="250" fixed="right" align="center">
              <template #default="{ row }">
                <el-button size="small" @click="viewDetail(row)">详情</el-button>
                <template v-if="row.status === 1">
                  <el-button type="success" size="small" @click="handleAccept(row)">接单</el-button>
                </template>
                <template v-else-if="row.status === 2">
                  <el-button type="primary" size="small" @click="handleArrive(row)">到达</el-button>
                  <el-button size="small" @click="callUser(row.contactPhone)">电话</el-button>
                </template>
                <template v-else-if="row.status === 3">
                  <el-button type="warning" size="small" @click="handleComplete(row)">完成</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-else class="card-grid">
          <div v-for="order in filteredMyOrders" :key="order.id" class="order-card my-order" :class="'status-' + order.status">
            <div class="status-bar"></div>
            <div class="card-header">
              <span class="order-id">{{ order.orderNo }}</span>
              <el-tag :type="getStatusType(order.status)" size="small" effect="dark">
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>
            <div class="card-body">
              <div class="product">
                <span class="icon">{{ getApplianceIcon(order.applianceTypeName) }}</span>
                <div class="info">
                  <h4>{{ order.applianceTypeName }}</h4>
                  <p>{{ order.estimatedWeight }}kg</p>
                </div>
              </div>
              <div class="contact-info">
                <div><el-icon><User /></el-icon> {{ order.contactName }}</div>
                <div><el-icon><Phone /></el-icon> {{ order.contactPhone }}</div>
                <div><el-icon><Location /></el-icon> {{ truncateAddress(order.addressDetail, 25) }}</div>
              </div>

              <!-- 进度条（仅进行中的订单） -->
              <div v-if="order.status === 2 || order.status === 3" class="mini-progress">
                <div class="step" :class="{ active: order.status >= 2 }">接单</div>
                <div class="line" :class="{ active: order.status >= 3 }"></div>
                <div class="step" :class="{ active: order.status >= 3 }">到达</div>
                <div class="line"></div>
                <div class="step">完成</div>
              </div>
            </div>
            <div class="card-footer">
              <el-button size="small" @click="viewDetail(order)">详情</el-button>
              <template v-if="order.status === 1">
                <el-button type="success" size="small" @click="handleAccept(order)">接单</el-button>
              </template>
              <template v-else-if="order.status === 2">
                <el-button type="primary" size="small" @click="handleArrive(order)">到达</el-button>
                <el-button size="small" @click="callUser(order.contactPhone)">电话</el-button>
              </template>
              <template v-else-if="order.status === 3">
                <el-button type="warning" size="small" @click="handleComplete(order)">完成</el-button>
              </template>
            </div>
          </div>
        </div>

        <el-empty v-if="filteredMyOrders.length === 0" :description="emptyText" />
      </template>
    </div>

    <!-- 订单详情抽屉 - 添加自定义类名 -->
    <el-drawer
        v-model="drawerVisible"
        title="订单详情"
        size="500px"
        destroy-on-close
        class="order-detail-drawer"
    >
      <div v-if="currentOrder.id" class="order-detail-wrapper">
        <div class="order-detail-content">
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
                <el-tag :type="getStatusType(currentOrder.status)" effect="dark">
                  {{ getStatusText(currentOrder.status) }}
                </el-tag>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-title">物品信息</div>
            <div class="product-display">
              <div class="big-icon">{{ getApplianceIcon(currentOrder.applianceTypeName) }}</div>
              <div class="product-detail">
                <h3>{{ currentOrder.applianceTypeName }}</h3>
                <p>预估重量：{{ currentOrder.estimatedWeight }}kg</p>
                <p v-if="currentOrder.priceEstimate" class="price">预估价格：¥{{ currentOrder.priceEstimate }}</p>
              </div>
            </div>
            <div class="desc-box" v-if="currentOrder.applianceDesc">
              <div class="label">物品描述</div>
              <div class="content">{{ currentOrder.applianceDesc }}</div>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-title">服务信息</div>
            <div class="info-list">
              <div class="info-item">
                <span class="label">联系人</span>
                <span class="value">{{ currentOrder.contactName }}</span>
              </div>
              <div class="info-item">
                <span class="label">联系电话</span>
                <span class="value">{{ currentOrder.contactPhone }}</span>
                <el-button type="primary" link size="small" @click="callUser(currentOrder.contactPhone)">拨打</el-button>
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
        </div>

        <!-- 底部操作按钮 - 使用 flex 布局而非 fixed -->
        <div class="detail-actions-wrapper" v-if="[0, 1, 2, 3].includes(currentOrder.status)">
          <div class="detail-actions-inner">
            <template v-if="currentOrder.status === 0">
              <el-button
                  type="success"
                  size="default"
                  class="action-btn"
                  @click="handleGrab(currentOrder); drawerVisible = false;"
              >
                <el-icon><Pointer /></el-icon>
                <span>立即抢单</span>
              </el-button>
            </template>
            <template v-else-if="currentOrder.status === 1">
              <el-button
                  type="success"
                  size="default"
                  class="action-btn"
                  @click="handleAccept(currentOrder); drawerVisible = false;"
              >
                <el-icon><Check /></el-icon>
                <span>确认接单</span>
              </el-button>
            </template>
            <template v-else-if="currentOrder.status === 2">
              <el-button
                  type="primary"
                  size="default"
                  class="action-btn"
                  @click="handleArrive(currentOrder); drawerVisible = false;"
              >
                <el-icon><MapLocation /></el-icon>
                <span>确认到达</span>
              </el-button>
            </template>
            <template v-else-if="currentOrder.status === 3">
              <el-button
                  type="warning"
                  size="default"
                  class="action-btn"
                  @click="handleComplete(currentOrder); drawerVisible = false;"
              >
                <el-icon><CircleCheck /></el-icon>
                <span>完成结算</span>
              </el-button>
            </template>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 完成结算弹窗 -->
    <el-dialog
        v-model="completeVisible"
        title="订单结算"
        width="450px"
        :close-on-click-modal="false"
    >
      <el-alert
          title="请根据实际称重填写结算信息"
          type="info"
          :closable="false"
          style="margin-bottom: 20px"
      />
      <el-form ref="formRef" :model="completeForm" :rules="completeRules" label-position="top">
        <el-form-item label="实际重量 (kg)" prop="weightActual">
          <el-input-number
              v-model="completeForm.weightActual"
              :min="0.1"
              :precision="2"
              :step="0.5"
              style="width: 100%"
              size="large"
          />
        </el-form-item>
        <el-form-item label="实际金额 (元)" prop="priceActual">
          <el-input-number
              v-model="completeForm.priceActual"
              :min="0"
              :precision="2"
              :step="10"
              style="width: 100%"
              size="large"
          />
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input
              v-model="completeForm.remark"
              type="textarea"
              :rows="2"
              placeholder="如有特殊情况请备注（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="success" @click="submitComplete" :loading="submitting">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  List, Grid, Refresh, Location, Clock, User, Phone,
  CircleCheck, Van, Bell, Monitor, Pointer, Check, MapLocation
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 用户信息
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))

// 视图状态
const activeTab = ref('hall')
const viewMode = ref('list')
const loading = ref(false)
const refreshing = ref(false)

// 标签页配置
const tabs = ref([
  { key: 'hall', label: '抢单大厅', icon: 'Grid', count: 0 },
  { key: 'pending', label: '待接单', icon: 'Bell', count: 0 },
  { key: 'processing', label: '进行中', icon: 'Van', count: 0 },
  { key: 'completed', label: '已完成', icon: 'CircleCheck', count: 0 }
])

// 统计数据
const stats = reactive({
  hallCount: 0,
  pendingCount: 0,
  processingCount: 0,
  completedCount: 0
})

// 数据列表
const hallList = ref([])
const myOrders = ref([])
const hallFilter = ref('all')

// 弹窗控制
const drawerVisible = ref(false)
const completeVisible = ref(false)
const currentOrder = ref({})
const submitting = ref(false)

// 完成表单
const completeForm = reactive({
  orderId: null,
  weightActual: 0,
  priceActual: 0,
  remark: ''
})

const formRef = ref()
const completeRules = {
  weightActual: [{ required: true, message: '请输入实际重量', trigger: 'blur' }],
  priceActual: [{ required: true, message: '请输入实际金额', trigger: 'blur' }]
}

// 计算属性
const filteredMyOrders = computed(() => {
  if (activeTab.value === 'pending') {
    return myOrders.value.filter(o => o.status === 1)
  } else if (activeTab.value === 'processing') {
    return myOrders.value.filter(o => o.status === 2 || o.status === 3)
  } else if (activeTab.value === 'completed') {
    return myOrders.value.filter(o => o.status === 4)
  }
  return myOrders.value
})

const emptyText = computed(() => {
  const texts = {
    'pending': '暂无待接单，去抢单大厅看看吧',
    'processing': '暂无进行中的订单',
    'completed': '今日暂无已完成订单'
  }
  return texts[activeTab.value] || '暂无数据'
})

// 生命周期
onMounted(() => {
  loadAllData()
})

// 方法
const loadAllData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadHallData(),
      loadMyOrders()
    ])
  } catch (e) {
    console.error('加载数据失败:', e)
  } finally {
    loading.value = false
  }
}

const refreshData = async () => {
  refreshing.value = true
  ElMessage.info('正在刷新...')
  try {
    if (activeTab.value === 'hall') {
      await loadHallData()
    } else {
      await loadMyOrders()
    }
    ElMessage.success('刷新成功')
  } catch (e) {
    ElMessage.error('刷新失败')
  } finally {
    refreshing.value = false
  }
}

// 加载抢单大厅（status=0）
const loadHallData = async () => {
  try {
    const res = await request.get('/recycleOrder/selectByStatus/0')
    if (res.code === '200' || res.code === 200) {
      hallList.value = (res.data || []).map(item => ({ ...item, _grabbing: false }))
      stats.hallCount = hallList.value.length
      tabs.value[0].count = stats.hallCount
      applyHallFilter()
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (error) {
    console.error('加载抢单大厅失败:', error)
    ElMessage.error('网络错误，请检查连接')
  }
}

// 加载我的订单
const loadMyOrders = async () => {
  if (!user.value.id) return
  try {
    const res = await request.get(`/recycleOrder/selectByCollector/${user.value.id}`)
    if (res.code === '200' || res.code === 200) {
      myOrders.value = res.data || []

      // 更新统计
      stats.pendingCount = myOrders.value.filter(o => o.status === 1).length
      stats.processingCount = myOrders.value.filter(o => o.status === 2 || o.status === 3).length
      stats.completedCount = myOrders.value.filter(o => {
        if (o.status !== 4) return false
        // 检查是否是今天完成的
        const today = new Date().toDateString()
        return o.completeTime ? new Date(o.completeTime).toDateString() === today : false
      }).length

      // 更新标签计数
      tabs.value[1].count = stats.pendingCount
      tabs.value[2].count = stats.processingCount
      tabs.value[3].count = stats.completedCount
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (error) {
    console.error('加载订单失败:', error)
  }
}

// 抢单
const handleGrab = async (order) => {
  try {
    await ElMessageBox.confirm(
        `确认抢单？<br>
      <strong>物品：</strong>${order.applianceTypeName} ${order.estimatedWeight}kg<br>
      <strong>地址：</strong>${order.addressDetail}`,
        '抢单确认',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '立即抢单',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    order._grabbing = true
    const res = await request.post('/recycleOrder/grab', null, {
      params: { orderId: order.id, collectorId: user.value.id }
    })

    if (res.code === '200' || res.code === 200) {
      ElMessage.success('抢单成功！')
      await loadAllData()
      activeTab.value = 'pending'
    } else {
      ElMessage.error(res.msg || '抢单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '抢单失败')
    }
  } finally {
    order._grabbing = false
  }
}

// 接单（status 1->2）
const handleAccept = async (order) => {
  try {
    await ElMessageBox.confirm('确认接单？接单后请尽快前往服务地点', '确认接单')
    const res = await request.post('/recycleOrder/accept', null, {
      params: { orderId: order.id, collectorId: user.value.id }
    })
    if (res.code === '200' || res.code === 200) {
      ElMessage.success('接单成功')
      await loadMyOrders()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

// 到达（status 2->3）
const handleArrive = async (order) => {
  try {
    await ElMessageBox.confirm('已到达用户指定地点？', '确认到达')
    const res = await request.post('/recycleOrder/arrive', null, {
      params: { orderId: order.id }
    })
    if (res.code === '200' || res.code === 200) {
      ElMessage.success('已确认到达')
      await loadMyOrders()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

// 完成（打开结算）
const handleComplete = (order) => {
  currentOrder.value = order
  completeForm.orderId = order.id
  completeForm.weightActual = order.weightActual || order.estimatedWeight || 0
  completeForm.priceActual = order.priceActual || order.priceEstimate || 0
  completeForm.remark = ''
  completeVisible.value = true
}

// 提交完成
const submitComplete = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  submitting.value = true
  try {
    const res = await request.post('/recycleOrder/complete', {
      id: completeForm.orderId,
      weightActual: completeForm.weightActual,
      priceActual: completeForm.priceActual,
      remark: completeForm.remark
    })
    if (res.code === '200' || res.code === 200) {
      ElMessage.success('订单完成！')
      completeVisible.value = false
      await loadMyOrders()
      activeTab.value = 'completed'
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

// 辅助功能
const handleTabChange = (tab) => {
  activeTab.value = tab
  if (tab === 'hall') {
    loadHallData()
  } else {
    loadMyOrders()
  }
}

const applyHallFilter = () => {
  // 这里可以实现本地排序逻辑
  if (hallFilter.value === 'nearby') {
    hallList.value.sort((a, b) => (a.distance || 999) - (b.distance || 999))
  } else if (hallFilter.value === 'price') {
    hallList.value.sort((a, b) => (b.priceEstimate || 0) - (a.priceEstimate || 0))
  } else if (hallFilter.value === 'urgent') {
    hallList.value.sort((a, b) => (b.urgencyLevel || 0) - (a.urgencyLevel || 0))
  }
}

const viewDetail = (order) => {
  currentOrder.value = order
  drawerVisible.value = true
}

const callUser = (phone) => {
  if (!phone) return
  window.location.href = `tel:${phone}`
}

// 工具函数
const getApplianceIcon = (name) => {
  const icons = { '冰箱': '❄️', '洗衣机': '🌀', '空调': '🌡️', '电视': '📺', '电脑': '💻', '手机': '📱', '热水器': '🔥', '微波炉': '🍲' }
  for (let key in icons) if (name?.includes(key)) return icons[key]
  return '📦'
}

const getStatusText = (status) => {
  const map = { 0: '待分配', 1: '待接单', 2: '前往中', 3: '已到达', 4: '已完成', 5: '已取消' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'info', 5: 'danger' }
  return map[status] || 'info'
}

const truncateAddress = (addr, len) => {
  return addr?.length > len ? addr.substring(0, len) + '...' : addr
}

const formatDateTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')} ${String(date.getHours()).padStart(2,'0')}:${String(date.getMinutes()).padStart(2,'0')}`
}

// 监听标签切换
watch(activeTab, (newVal) => {
  if (newVal === 'hall') {
    loadHallData()
  } else {
    loadMyOrders()
  }
})
</script>

<style scoped lang="scss">
.order-center-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

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
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);

      &.blue { border-left: 4px solid #3b82f6; .icon-wrap { background: #eff6ff; color: #3b82f6; } }
      &.orange { border-left: 4px solid #f59e0b; .icon-wrap { background: #fffbeb; color: #f59e0b; } }
      &.green { border-left: 4px solid #10b981; .icon-wrap { background: #f0fdf4; color: #10b981; } }
      &.purple { border-left: 4px solid #8b5cf6; .icon-wrap { background: #faf5ff; color: #8b5cf6; } }

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
          color: #10b981;
          border-bottom-color: #10b981;
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

  .content-area {
    background: white;
    border-radius: 0 0 12px 12px;
    padding: 24px;
    min-height: 500px;

    .filter-bar {
      margin-bottom: 20px;
    }

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

      .price-text {
        color: #ef4444;
        font-weight: 700;
        font-size: 16px;
      }

      .location-info {
        .distance {
          display: flex;
          align-items: center;
          gap: 4px;
          color: #10b981;
          font-weight: 600;
          font-size: 13px;
          margin-bottom: 4px;

          .el-icon {
            font-size: 14px;
          }
        }
        .address {
          font-size: 13px;
          color: #6b7280;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .time-info {
        .time {
          font-size: 13px;
          color: #6b7280;
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

      .address-cell {
        color: #6b7280;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .card-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;

      .order-card {
        background: white;
        border: 1px solid #e5e7eb;
        border-radius: 12px;
        overflow: hidden;
        transition: all 0.3s;

        &:hover {
          box-shadow: 0 10px 25px rgba(0,0,0,0.1);
          transform: translateY(-2px);
        }

        .card-header {
          padding: 16px;
          display: flex;
          justify-content: space-between;
          align-items: center;
          background: #f9fafb;
          border-bottom: 1px solid #e5e7eb;
          position: relative;

          .urgency-badge {
            position: absolute;
            right: 0;
            top: 0;
            background: #ef4444;
            color: white;
            padding: 4px 12px;
            border-radius: 0 0 0 12px;
            font-size: 12px;
            font-weight: 600;
          }

          .distance {
            color: #10b981;
            font-weight: 600;
          }

          .order-id {
            font-family: monospace;
            color: #6b7280;
            font-weight: 600;
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

          .price-row, .address-row, .time-row {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 8px;
            font-size: 14px;

            .label {
              color: #6b7280;
            }
            .value {
              color: #ef4444;
              font-weight: 700;
              font-size: 18px;
            }

            .el-icon {
              color: #9ca3af;
            }
          }

          .address-row, .time-row {
            color: #6b7280;
            font-size: 13px;
          }

          .contact-info {
            margin: 12px 0;
            padding: 12px;
            background: #f9fafb;
            border-radius: 8px;
            font-size: 13px;
            color: #4b5563;

            > div {
              display: flex;
              align-items: center;
              gap: 6px;
              margin-bottom: 6px;

              &:last-child {
                margin-bottom: 0;
              }

              .el-icon {
                color: #9ca3af;
              }
            }
          }

          .mini-progress {
            display: flex;
            align-items: center;
            margin-top: 16px;
            padding-top: 16px;
            border-top: 1px solid #e5e7eb;

            .step {
              font-size: 12px;
              color: #9ca3af;
              font-weight: 500;

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
        }

        .card-footer {
          padding: 16px;
          border-top: 1px solid #e5e7eb;
          display: flex;
          gap: 8px;
        }

        &.my-order {
          .status-bar {
            height: 4px;
            background: #e5e7eb;

            &.status-1 { background: #f59e0b; }
            &.status-2 { background: #3b82f6; }
            &.status-3 { background: #10b981; }
            &.status-4 { background: #6b7280; }
          }
        }
      }
    }
  }

  // 订单详情抽屉样式
  .order-detail-wrapper {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 100%;
  }

  .order-detail-content {
    flex: 1;
    padding: 20px;
    padding-bottom: 80px; // 为底部按钮留出空间
    overflow-y: auto;

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
    }
  }

  // 底部操作按钮 - 使用 sticky 定位而非 fixed
  .detail-actions-wrapper {
    position: sticky;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 12px 20px;
    background: white;
    border-top: 1px solid #e5e7eb;
    box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
    z-index: 10;
    margin-top: auto;
  }

  .detail-actions-inner {
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .action-btn {
    min-width: 140px;
    height: 36px;
    padding: 0 24px;
    font-size: 14px;
    border-radius: 6px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 6px;

    .el-icon {
      font-size: 14px;
    }
  }
}

@media (max-width: 1200px) {
  .order-center-page {
    .stats-row {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}
</style>
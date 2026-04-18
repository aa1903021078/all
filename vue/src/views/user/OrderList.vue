<template>
  <div class="order-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>我的回收订单</h2>
      <p>管理您的回收预约，查看订单进度和环保贡献</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-bar">
      <div class="stat-item">
        <div class="stat-icon total">📦</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.total || 0 }}</div>
          <div class="stat-label">累计订单</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon completed">✅</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.completed || 0 }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon points">🌟</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalPoints || 0 }}</div>
          <div class="stat-label">获得积分</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon carbon">🌱</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalCarbon?.toFixed(2) || 0 }} kg</div>
          <div class="stat-label">碳减排</div>
        </div>
      </div>
    </div>

    <!-- 筛选标签 -->
    <div class="filter-tabs">
      <div
          v-for="tab in tabs"
          :key="tab.value"
          class="tab-item"
          :class="{ active: activeTab === tab.value }"
          @click="handleTabChange(tab.value)"
      >
        {{ tab.label }}
        <span v-if="tab.count > 0" class="badge">{{ tab.count }}</span>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="order-container" v-loading="loading">
      <div v-if="orders.length > 0" class="order-list">
        <div
            v-for="order in orders"
            :key="order.id"
            class="order-card"
            :class="`status-${order.status}`"
        >
          <!-- 订单头部 -->
          <div class="order-header">
            <div class="order-meta">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span class="order-time">{{ formatDate(order.createTime) }}</span>
            </div>
            <div class="status-tag" :class="statusClass(order.status)">
              <el-icon v-if="statusIcon(order.status)">
                <component :is="statusIcon(order.status)" />
              </el-icon>
              {{ statusText(order.status) }}
            </div>
          </div>

          <!-- 进度条（仅进行中的订单显示） -->
          <div v-if="[1,2,3].includes(order.status)" class="progress-bar">
            <div class="progress-step" :class="{ active: order.status >= 1, current: order.status === 1 }">
              <div class="step-dot">1</div>
              <div class="step-label">待接单</div>
            </div>
            <div class="progress-line" :class="{ active: order.status >= 2 }"></div>
            <div class="progress-step" :class="{ active: order.status >= 2, current: order.status === 2 }">
              <div class="step-dot">2</div>
              <div class="step-label">已接单</div>
            </div>
            <div class="progress-line" :class="{ active: order.status >= 3 }"></div>
            <div class="progress-step" :class="{ active: order.status >= 3, current: order.status === 3 }">
              <div class="step-dot">3</div>
              <div class="step-label">上门中</div>
            </div>
            <div class="progress-line" :class="{ active: order.status >= 4 }"></div>
            <div class="progress-step" :class="{ active: order.status >= 4 }">
              <div class="step-dot">4</div>
              <div class="step-label">已完成</div>
            </div>
          </div>

          <!-- 订单内容 -->
          <div class="order-content">
            <div class="appliance-info">
              <div class="appliance-icon">{{ getApplianceIcon(order.applianceTypeName) }}</div>
              <div class="appliance-detail">
                <h4>{{ order.applianceTypeName || '未知品类' }}</h4>
                <p>预估重量：{{ order.estimatedWeight }}kg × {{ order.quantity || 1 }}件</p>
                <p v-if="order.weightActual" class="actual-weight">
                  实际重量：{{ order.weightActual }}kg
                </p>
              </div>
              <div v-if="order.status === 4" class="reward-badge">
                <div class="points">+{{ order.pointsEarned }} 积分</div>
                <div class="carbon">减碳 {{ order.carbonSaved }}kg</div>
              </div>
            </div>

            <div class="divider"></div>

            <div class="address-info">
              <div class="info-row">
                <el-icon><Location /></el-icon>
                <span class="label">上门地址：</span>
                <span class="value">{{ order.addressDetail }}</span>
              </div>
              <div class="info-row">
                <el-icon><Clock /></el-icon>
                <span class="label">预约时间：</span>
                <span class="value">{{ order.expectDate }} {{ order.expectTimeStart }}-{{ order.expectTimeEnd }}</span>
              </div>
              <div class="info-row" v-if="order.collectorName">
                <el-icon><User /></el-icon>
                <span class="label">回收员：</span>
                <span class="value">
                  {{ order.collectorName }}
                  <a :href="`tel:${order.collectorPhone}`" class="phone-link">{{ order.collectorPhone }}</a>
                </span>
              </div>
              <div class="info-row" v-if="order.cancelReason">
                <el-icon><Warning /></el-icon>
                <span class="label">取消原因：</span>
                <span class="value text-danger">{{ order.cancelReason }}</span>
              </div>
            </div>

            <!-- 评价展示 -->
            <div v-if="order.status === 4 && order.userRating" class="rating-display">
              <el-rate v-model="order.userRating" disabled show-score text-color="#ff9900" />
              <p v-if="order.userComment" class="comment-text">{{ order.userComment }}</p>
            </div>
          </div>

          <!-- 订单操作 -->
          <div class="order-actions">
            <div class="action-left">
              <el-button
                  v-if="order.status === 0"
                  type="danger"
                  plain
                  size="small"
                  @click="cancelOrder(order)"
              >
                取消订单
              </el-button>
              <el-button
                  v-if="order.status === 5"
                  type="primary"
                  plain
                  size="small"
                  @click="rebook(order)"
              >
                重新预约
              </el-button>
            </div>
            <div class="action-right">
              <el-button
                  v-if="order.status === 4 && !order.userRating"
                  type="warning"
                  size="small"
                  @click="rateOrder(order)"
              >
                评价得积分
              </el-button>
              <el-button
                  type="default"
                  size="small"
                  @click="viewDetail(order)"
              >
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">📭</div>
        <h3>暂无{{ activeTab === 'all' ? '' : tabs.find(t => t.value === activeTab)?.label }}订单</h3>
        <p v-if="activeTab === 'all' || activeTab === '5'">您还没有预约过回收服务，快来体验环保回收吧！</p>
        <el-button v-if="activeTab === 'all' || activeTab === '5'" type="primary" @click="goToRecycle">
          立即预约
        </el-button>
      </div>
    </div>

    <!-- 订单详情抽屉 -->
    <el-drawer
        v-model="detailVisible"
        title="订单详情"
        size="500px"
        :with-header="true"
    >
      <div v-if="currentOrder.id" class="order-detail">
        <!-- 订单信息 -->
        <div class="detail-section">
          <h4>订单信息</h4>
          <div class="detail-row">
            <span class="label">订单编号：</span>
            <span class="value">{{ currentOrder.orderNo }}</span>
          </div>
          <div class="detail-row">
            <span class="label">创建时间：</span>
            <span class="value">{{ formatDateTime(currentOrder.createTime) }}</span>
          </div>
          <div class="detail-row">
            <span class="label">当前状态：</span>
            <el-tag :type="statusType(currentOrder.status)">{{ statusText(currentOrder.status) }}</el-tag>
          </div>
        </div>

        <!-- 回收员信息（新增） -->
        <div class="detail-section collector-section" v-if="currentOrder.collectorName || currentOrder.status >= 1">
          <h4>🚚 回收员信息</h4>
          <div class="collector-card" v-if="currentOrder.collectorName">
            <div class="collector-avatar">
              <img src="https://cdn-icons-png.flaticon.com/512/3135/3135715.png" alt="collector">
            </div>
            <div class="collector-info">
              <div class="collector-name">{{ currentOrder.collectorName }}</div>
              <div class="collector-phone">
                <el-icon><Phone /></el-icon>
                <a :href="`tel:${currentOrder.collectorPhone}`">{{ currentOrder.collectorPhone }}</a>
              </div>
              <div class="collector-location" v-if="currentOrder.collectorLocation">
                <el-icon><Location /></el-icon>
                <span>实时位置：{{ currentOrder.collectorLocation }}</span>
                <el-button type="primary" link size="small" @click="viewCollectorLocation">
                  查看地图
                </el-button>
              </div>
            </div>
          </div>
          <div class="collector-waiting" v-else>
            <el-icon class="waiting-icon"><Van /></el-icon>
            <span>正在为您分配回收员，请稍候...</span>
          </div>
        </div>

        <!-- 订单进度时间轴（新增） -->
        <div class="detail-section timeline-section">
          <h4>📋 订单进度</h4>
          <el-timeline>
            <el-timeline-item
                v-for="(activity, index) in getOrderTimeline(currentOrder)"
                :key="index"
                :type="activity.type"
                :color="activity.color"
                :icon="activity.icon"
                :timestamp="activity.timestamp"
            >
              <div class="timeline-content">
                <div class="timeline-title">{{ activity.title }}</div>
                <div class="timeline-desc" v-if="activity.description">{{ activity.description }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>

        <div class="detail-section">
          <h4>回收物品</h4>
          <div class="detail-row">
            <span class="label">品类：</span>
            <span class="value">{{ currentOrder.applianceTypeName }}</span>
          </div>
          <div class="detail-row">
            <span class="label">预估重量：</span>
            <span class="value">{{ currentOrder.estimatedWeight }}kg</span>
          </div>
          <div class="detail-row" v-if="currentOrder.weightActual">
            <span class="label">实际重量：</span>
            <span class="value">{{ currentOrder.weightActual }}kg</span>
          </div>
          <div class="detail-row" v-if="currentOrder.priceActual">
            <span class="label">结算金额：</span>
            <span class="value text-price">¥{{ currentOrder.priceActual }}</span>
          </div>
        </div>

        <div class="detail-section">
          <h4>服务地址</h4>
          <div class="detail-row">
            <span class="label">联系人：</span>
            <span class="value">{{ currentOrder.contactName }} {{ currentOrder.contactPhone }}</span>
          </div>
          <div class="detail-row">
            <span class="label">详细地址：</span>
            <span class="value">{{ currentOrder.addressDetail }}</span>
          </div>
          <div class="detail-row" v-if="currentOrder.buildingType">
            <span class="label">房屋类型：</span>
            <span class="value">{{ currentOrder.buildingType === 1 ? '电梯房' : `楼梯房${currentOrder.floor}层` }}</span>
          </div>
        </div>

        <div v-if="currentOrder.status === 4" class="detail-section eco-section">
          <h4>🌱 环保贡献</h4>
          <div class="eco-stats">
            <div class="eco-item">
              <div class="eco-value">{{ currentOrder.pointsEarned }}</div>
              <div class="eco-label">获得积分</div>
            </div>
            <div class="eco-item">
              <div class="eco-value">{{ currentOrder.carbonSaved }}kg</div>
              <div class="eco-label">碳减排量</div>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 评价弹窗 -->
    <el-dialog
        v-model="rateVisible"
        title="服务评价"
        width="450px"
        :close-on-click-modal="false"
        class="rate-dialog"
    >
      <div class="rate-content">
        <div class="rate-header">
          <img src="https://cdn-icons-png.flaticon.com/512/3135/3135715.png" alt="collector" class="collector-avatar">
          <div class="collector-info">
            <h4>{{ currentOrder.collectorName }}</h4>
            <p>感谢您的评价，将帮助回收员提升服务质量</p>
          </div>
        </div>

        <div class="rate-stars">
          <div class="stars-label">服务态度</div>
          <el-rate
              v-model="rateForm.rating"
              :colors="['#ff9900', '#ff9900', '#ff9900']"
              :max="5"
              size="large"
          />
          <span class="rate-score">{{ rateForm.rating }}分</span>
        </div>

        <el-input
            v-model="rateForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入您的评价内容（选填），分享您的回收体验..."
            maxlength="200"
            show-word-limit
        />

        <div class="rate-tags">
          <span
              v-for="tag in rateTags"
              :key="tag"
              class="tag"
              :class="{ active: selectedTags.includes(tag) }"
              @click="toggleTag(tag)"
          >
            {{ tag }}
          </span>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="rateVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRate" :loading="submitting">
            提交评价
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Clock, User, Warning, CircleCheck, Van, Phone, MapLocation } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('all')
const orders = ref([])
const detailVisible = ref(false)
const rateVisible = ref(false)
const currentOrder = ref({})
const submitting = ref(false)

const rateForm = reactive({
  rating: 5,
  comment: ''
})

const rateTags = ['上门准时', '态度很好', '称重准确', '价格公道', '服务专业', '清理现场']
const selectedTags = ref([])

const tabs = [
  { label: '全部', value: 'all', count: 0 },
  { label: '待分配', value: '0', count: 0 },
  { label: '待接单', value: '1', count: 0 },
  { label: '进行中', value: 'processing', count: 0 },
  { label: '已完成', value: '4', count: 0 },
  { label: '已取消', value: '5', count: 0 }
]

const stats = reactive({
  total: 0,
  completed: 0,
  totalPoints: 0,
  totalCarbon: 0
})

const user = computed(() => {
  const userStr = localStorage.getItem('user')
  return userStr ? JSON.parse(userStr) : {}
})

onMounted(() => {
  loadOrders()
  loadStats()
})

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await request.get(`/recycleOrder/selectByUser/${user.value.id}`)
    let list = res.data || res || []

    // 更新各状态数量
    tabs.forEach(tab => {
      if (tab.value === 'all') {
        tab.count = list.length
      } else if (tab.value === 'processing') {
        tab.count = list.filter(o => o.status === 2 || o.status === 3).length
      } else {
        tab.count = list.filter(o => o.status === parseInt(tab.value)).length
      }
    })

    // 根据当前标签筛选
    if (activeTab.value === 'all') {
      orders.value = list.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
    } else if (activeTab.value === 'processing') {
      orders.value = list.filter(o => o.status === 2 || o.status === 3)
    } else {
      orders.value = list.filter(o => o.status === parseInt(activeTab.value))
    }
  } catch (e) {
    console.error('加载订单失败:', e)
    ElMessage.error('加载订单失败')
  }
  loading.value = false
}

// ==========================================
// 【修改部分】修复统计逻辑：只统计已完成订单的积分和碳减排
// ==========================================
const loadStats = async () => {
  try {
    const res = await request.get(`/recycleOrder/selectByUser/${user.value.id}`)
    const list = res.data || res || []

    // 筛选出已完成的订单（status === 4）
    const completedOrders = list.filter(o => o.status === 4)

    stats.total = list.length
    stats.completed = completedOrders.length
    // 关键修改：只累加已完成订单的实际获得积分和减碳量
    stats.totalPoints = completedOrders.reduce((sum, o) => sum + (o.pointsEarned || 0), 0)
    stats.totalCarbon = completedOrders.reduce((sum, o) => sum + (o.carbonSaved || 0), 0)
  } catch (e) {
    console.error('加载统计失败:', e)
  }
}
// ==========================================

// ==========================================
// 【新增方法】生成订单进度时间轴数据
// ==========================================
const getOrderTimeline = (order) => {
  const timeline = []

  // 1. 创建时间
  timeline.push({
    title: '订单创建',
    timestamp: formatDateTime(order.createTime),
    description: '用户提交回收预约',
    type: 'primary',
    color: '#409eff',
    icon: CircleCheck
  })

  // 2. 派单时间（待分配->待接单）
  if (order.assignTime || order.status >= 1) {
    timeline.push({
      title: '系统派单',
      timestamp: formatDateTime(order.assignTime) || '处理中...',
      description: order.collectorName ? `已分配回收员：${order.collectorName}` : '正在分配回收员',
      type: order.status >= 1 ? 'success' : 'info',
      color: order.status >= 1 ? '#67c23a' : '#909399',
      icon: Van
    })
  }

  // 3. 接单时间
  if (order.acceptTime || order.status >= 2) {
    timeline.push({
      title: '回收员接单',
      timestamp: formatDateTime(order.acceptTime) || '等待接单...',
      description: order.collectorName ? `${order.collectorName} 已接单` : '回收员接单中',
      type: order.status >= 2 ? 'success' : 'info',
      color: order.status >= 2 ? '#67c23a' : '#909399',
      icon: CircleCheck
    })
  }

  // 4. 到达时间
  if (order.arriveTime || order.status >= 3) {
    timeline.push({
      title: '上门回收',
      timestamp: formatDateTime(order.arriveTime) || '前往中...',
      description: order.collectorName ? `${order.collectorName} 已到达回收地址` : '回收员前往中',
      type: order.status >= 3 ? 'success' : 'info',
      color: order.status >= 3 ? '#67c23a' : '#909399',
      icon: Location
    })
  }

  // 5. 完成时间
  if (order.completeTime || order.status === 4) {
    timeline.push({
      title: '订单完成',
      timestamp: formatDateTime(order.completeTime) || '处理中...',
      description: order.pointsEarned ? `获得 ${order.pointsEarned} 积分，减碳 ${order.carbonSaved}kg` : '回收完成',
      type: order.status === 4 ? 'success' : 'info',
      color: order.status === 4 ? '#67c23a' : '#909399',
      icon: CircleCheck
    })
  }

  // 6. 取消时间（如果已取消）
  if (order.cancelTime || order.status === 5) {
    timeline.push({
      title: '订单取消',
      timestamp: formatDateTime(order.cancelTime) || '-',
      description: order.cancelReason || '订单已取消',
      type: 'danger',
      color: '#f56c6c',
      icon: Warning
    })
  }

  return timeline
}

// 【新增方法】查看回收员位置
const viewCollectorLocation = () => {
  if (currentOrder.value.collectorLat && currentOrder.value.collectorLng) {
    // 打开地图查看实时位置
    const url = `https://map.baidu.com/search/${currentOrder.value.collectorLocation || '当前位置'}/@${currentOrder.value.collectorLng},${currentOrder.value.collectorLat},16z`
    window.open(url, '_blank')
  } else {
    ElMessage.info('暂无实时位置信息')
  }
}
// ==========================================

const handleTabChange = (tab) => {
  activeTab.value = tab
  loadOrders()
}

const statusText = (status) => {
  const map = {
    0: '待分配',
    1: '待接单',
    2: '已接单',
    3: '已到达',
    4: '已完成',
    5: '已取消'
  }
  return map[status] || '未知'
}

const statusType = (status) => {
  const map = {
    0: 'info',
    1: 'warning',
    2: 'primary',
    3: 'success',
    4: 'success',
    5: 'danger'
  }
  return map[status] || 'info'
}

const statusClass = (status) => {
  const map = {
    0: 'status-pending',
    1: 'status-assigned',
    2: 'status-accepted',
    3: 'status-arrived',
    4: 'status-completed',
    5: 'status-cancelled'
  }
  return map[status] || ''
}

const statusIcon = (status) => {
  const map = {
    0: null,
    1: Van,
    2: CircleCheck,
    3: Location,
    4: CircleCheck,
    5: Warning
  }
  return map[status]
}

const getApplianceIcon = (typeName) => {
  const iconMap = {
    '冰箱': '❄️',
    '洗衣机': '🌀',
    '空调': '❄️',
    '电视': '📺',
    '电脑': '💻',
    '手机': '📱',
    '热水器': '🔥',
    '微波炉': '♨️'
  }
  for (let key in iconMap) {
    if (typeName?.includes(key)) return iconMap[key]
  }
  return '📦'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const cancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？取消后无法恢复', '确认取消', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      type: 'warning'
    })

    await request.post('/recycleOrder/cancel', null, {
      params: {
        orderId: order.id,
        reason: '用户主动取消'
      }
    })
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('取消失败:', e)
    }
  }
}

const rateOrder = (order) => {
  currentOrder.value = order
  rateForm.rating = 5
  rateForm.comment = ''
  selectedTags.value = []
  rateVisible.value = true
}

const toggleTag = (tag) => {
  const index = selectedTags.value.indexOf(tag)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(tag)
  }
  // 将标签同步到评论中
  rateForm.comment = selectedTags.value.join('，')
}

const submitRate = async () => {
  if (!rateForm.rating) {
    ElMessage.warning('请选择评分')
    return
  }

  submitting.value = true
  try {
    await request.post('/recycleOrder/rate', null, {
      params: {
        orderId: currentOrder.value.id,
        rating: rateForm.rating,
        comment: rateForm.comment
      }
    })
    ElMessage.success('评价成功，感谢您的反馈！')
    rateVisible.value = false
    loadOrders()
  } catch (error) {
    ElMessage.error(error.message || '评价失败')
  } finally {
    submitting.value = false
  }
}

const viewDetail = (order) => {
  currentOrder.value = order
  detailVisible.value = true
}

const rebook = (order) => {
  // 将取消的订单信息带到预约页面
  router.push({
    path: '/recycle',
    query: {
      typeId: order.applianceTypeId,
      weight: order.estimatedWeight
    }
  })
}

const goToRecycle = () => {
  router.push('/recycle')
}
</script>

<style scoped lang="scss">
.order-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;

  .page-header {
    text-align: center;
    margin-bottom: 30px;

    h2 {
      font-size: 28px;
      color: #303133;
      margin-bottom: 8px;
      font-weight: 600;
    }

    p {
      color: #909399;
      font-size: 14px;
    }
  }

  .stats-bar {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 30px;

    .stat-item {
      background: white;
      border-radius: 12px;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 15px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

      .stat-icon {
        width: 50px;
        height: 50px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;

        &.total {
          background: #ecf5ff;
        }

        &.completed {
          background: #f0f9ff;
        }

        &.points {
          background: #fff8e6;
        }

        &.carbon {
          background: #f0fff4;
        }
      }

      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        line-height: 1.2;
      }

      .stat-label {
        font-size: 13px;
        color: #909399;
        margin-top: 4px;
      }
    }

    @media (max-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  .filter-tabs {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    flex-wrap: wrap;

    .tab-item {
      padding: 8px 20px;
      background: white;
      border-radius: 20px;
      cursor: pointer;
      font-size: 14px;
      color: #606266;
      transition: all 0.3s;
      border: 1px solid #e4e7ed;

      &:hover {
        color: #409eff;
        border-color: #409eff;
      }

      &.active {
        background: #409eff;
        color: white;
        border-color: #409eff;
      }

      .badge {
        background: #f56c6c;
        color: white;
        font-size: 12px;
        padding: 2px 6px;
        border-radius: 10px;
        margin-left: 4px;
      }
    }
  }

  .order-container {
    min-height: 400px;

    .order-list {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .order-card {
      background: white;
      border-radius: 12px;
      padding: 24px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
      border: 1px solid transparent;
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
        transform: translateY(-2px);
      }

      &.status-4 {
        border-left: 4px solid #67c23a;
      }

      &.status-5 {
        border-left: 4px solid #f56c6c;
        opacity: 0.9;
      }

      .order-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;

        .order-meta {
          display: flex;
          flex-direction: column;
          gap: 4px;

          .order-no {
            font-size: 14px;
            color: #606266;
            font-weight: 500;
          }

          .order-time {
            font-size: 12px;
            color: #909399;
          }
        }

        .status-tag {
          display: flex;
          align-items: center;
          gap: 4px;
          padding: 6px 12px;
          border-radius: 16px;
          font-size: 13px;
          font-weight: 500;

          &.status-pending {
            background: #ecf5ff;
            color: #409eff;
          }

          &.status-assigned {
            background: #fdf6ec;
            color: #e6a23c;
          }

          &.status-accepted {
            background: #f0f9ff;
            color: #1890ff;
          }

          &.status-arrived {
            background: #f6ffed;
            color: #52c41a;
          }

          &.status-completed {
            background: #f6ffed;
            color: #52c41a;
          }

          &.status-cancelled {
            background: #fef0f0;
            color: #f56c6c;
          }
        }
      }

      .progress-bar {
        display: flex;
        align-items: center;
        margin-bottom: 24px;
        padding: 0 10px;

        .progress-step {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 6px;
          flex-shrink: 0;

          .step-dot {
            width: 28px;
            height: 28px;
            border-radius: 50%;
            background: #dcdfe6;
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 12px;
            font-weight: 600;
            transition: all 0.3s;
          }

          .step-label {
            font-size: 12px;
            color: #909399;
          }

          &.active {
            .step-dot {
              background: #52c41a;
            }

            .step-label {
              color: #52c41a;
              font-weight: 500;
            }
          }

          &.current {
            .step-dot {
              box-shadow: 0 0 0 3px rgba(82, 196, 26, 0.2);
              animation: pulse 2s infinite;
            }
          }
        }

        .progress-line {
          flex: 1;
          height: 2px;
          background: #dcdfe6;
          margin: 0 8px;
          margin-bottom: 20px;

          &.active {
            background: #52c41a;
          }
        }

        @keyframes pulse {
          0% {
            box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.4);
          }
          70% {
            box-shadow: 0 0 0 6px rgba(82, 196, 26, 0);
          }
          100% {
            box-shadow: 0 0 0 0 rgba(82, 196, 26, 0);
          }
        }
      }

      .order-content {
        .appliance-info {
          display: flex;
          align-items: center;
          gap: 16px;
          margin-bottom: 16px;

          .appliance-icon {
            width: 60px;
            height: 60px;
            background: #f5f7fa;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 28px;
          }

          .appliance-detail {
            flex: 1;

            h4 {
              font-size: 16px;
              color: #303133;
              margin-bottom: 4px;
              font-weight: 600;
            }

            p {
              font-size: 13px;
              color: #606266;

              &.actual-weight {
                color: #67c23a;
                font-weight: 500;
                margin-top: 2px;
              }
            }
          }

          .reward-badge {
            text-align: right;

            .points {
              color: #fa8c16;
              font-size: 18px;
              font-weight: 600;
              margin-bottom: 4px;
            }

            .carbon {
              color: #52c41a;
              font-size: 12px;
              background: #f6ffed;
              padding: 2px 8px;
              border-radius: 10px;
            }
          }
        }

        .divider {
          height: 1px;
          background: #ebeef5;
          margin: 16px 0;
        }

        .address-info {
          .info-row {
            display: flex;
            align-items: flex-start;
            gap: 8px;
            margin-bottom: 10px;
            font-size: 14px;

            .el-icon {
              color: #909399;
              margin-top: 2px;
            }

            .label {
              color: #909399;
              flex-shrink: 0;
            }

            .value {
              color: #303133;
              flex: 1;
              line-height: 1.4;

              &.text-danger {
                color: #f56c6c;
              }
            }

            .phone-link {
              color: #409eff;
              text-decoration: none;
              margin-left: 8px;

              &:hover {
                text-decoration: underline;
              }
            }
          }
        }

        .rating-display {
          margin-top: 16px;
          padding: 16px;
          background: #f5f7fa;
          border-radius: 8px;

          .comment-text {
            margin-top: 8px;
            color: #606266;
            font-size: 14px;
            line-height: 1.6;
          }
        }
      }

      .order-actions {
        display: flex;
        justify-content: space-between;
        margin-top: 20px;
        padding-top: 16px;
        border-top: 1px solid #ebeef5;

        .action-left, .action-right {
          display: flex;
          gap: 10px;
        }
      }
    }

    .empty-state {
      text-align: center;
      padding: 60px 20px;
      background: white;
      border-radius: 12px;

      .empty-icon {
        font-size: 64px;
        margin-bottom: 16px;
        opacity: 0.6;
      }

      h3 {
        color: #303133;
        margin-bottom: 8px;
        font-size: 18px;
      }

      p {
        color: #909399;
        margin-bottom: 24px;
        font-size: 14px;
      }
    }
  }

  .order-detail {
    .detail-section {
      margin-bottom: 24px;

      h4 {
        font-size: 16px;
        color: #303133;
        margin-bottom: 16px;
        font-weight: 600;
        padding-bottom: 8px;
        border-bottom: 1px solid #ebeef5;
      }

      .detail-row {
        display: flex;
        margin-bottom: 12px;
        font-size: 14px;

        .label {
          color: #909399;
          width: 80px;
          flex-shrink: 0;
        }

        .value {
          color: #303133;
          flex: 1;

          &.text-price {
            color: #f56c6c;
            font-weight: 600;
            font-size: 16px;
          }
        }
      }

      // ==========================================
      // 【新增样式】回收员信息区域
      // ==========================================
      &.collector-section {
        .collector-card {
          display: flex;
          gap: 16px;
          padding: 16px;
          background: #f5f7fa;
          border-radius: 12px;
          align-items: center;

          .collector-avatar {
            width: 60px;
            height: 60px;
            border-radius: 50%;
            overflow: hidden;
            flex-shrink: 0;
            background: white;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

            img {
              width: 100%;
              height: 100%;
              object-fit: cover;
            }
          }

          .collector-info {
            flex: 1;

            .collector-name {
              font-size: 16px;
              font-weight: 600;
              color: #303133;
              margin-bottom: 6px;
            }

            .collector-phone {
              display: flex;
              align-items: center;
              gap: 6px;
              color: #409eff;
              font-size: 14px;
              margin-bottom: 6px;

              a {
                color: #409eff;
                text-decoration: none;

                &:hover {
                  text-decoration: underline;
                }
              }

              .el-icon {
                font-size: 14px;
              }
            }

            .collector-location {
              display: flex;
              align-items: center;
              gap: 6px;
              color: #67c23a;
              font-size: 13px;

              .el-icon {
                font-size: 14px;
              }

              .el-button {
                margin-left: 8px;
              }
            }
          }
        }

        .collector-waiting {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 20px;
          background: #fdf6ec;
          border-radius: 12px;
          color: #e6a23c;
          font-size: 14px;

          .waiting-icon {
            font-size: 24px;
            animation: rotate 2s linear infinite;
          }

          @keyframes rotate {
            from { transform: rotate(0deg); }
            to { transform: rotate(360deg); }
          }
        }
      }

      // ==========================================
      // 【新增样式】订单进度时间轴区域
      // ==========================================
      &.timeline-section {
        .el-timeline {
          padding-left: 8px;

          .timeline-content {
            .timeline-title {
              font-size: 14px;
              font-weight: 600;
              color: #303133;
              margin-bottom: 4px;
            }

            .timeline-desc {
              font-size: 13px;
              color: #909399;
              line-height: 1.4;
            }
          }
        }
      }

      &.eco-section {
        background: #f6ffed;
        padding: 20px;
        border-radius: 12px;
        border: 1px solid #b7eb8f;

        h4 {
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
  }

  .rate-dialog {
    .rate-content {
      .rate-header {
        display: flex;
        gap: 16px;
        margin-bottom: 24px;
        align-items: center;

        .collector-avatar {
          width: 50px;
          height: 50px;
          border-radius: 50%;
          object-fit: cover;
        }

        .collector-info {
          h4 {
            font-size: 16px;
            color: #303133;
            margin-bottom: 4px;
          }

          p {
            font-size: 13px;
            color: #909399;
          }
        }
      }

      .rate-stars {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 20px;

        .stars-label {
          font-size: 14px;
          color: #606266;
          font-weight: 500;
        }

        .rate-score {
          color: #ff9900;
          font-weight: 600;
          font-size: 16px;
        }
      }

      .rate-tags {
        margin-top: 16px;
        display: flex;
        flex-wrap: wrap;
        gap: 8px;

        .tag {
          padding: 6px 12px;
          background: #f5f7fa;
          border: 1px solid #dcdfe6;
          border-radius: 16px;
          font-size: 13px;
          color: #606266;
          cursor: pointer;
          transition: all 0.3s;

          &:hover, &.active {
            background: #fff8e6;
            border-color: #ffc53d;
            color: #fa8c16;
          }
        }
      }
    }

    .dialog-footer {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
    }
  }
}
</style>
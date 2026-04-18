<template>
  <div class="dispatch-container">
    <!-- 顶部统计栏 -->
    <el-row :gutter="20" class="stats-bar">
      <el-col :span="6">
        <el-statistic title="待派单" :value="stats.pendingCount" value-style="color: #f56c6c">
          <template #prefix><el-icon><Document /></el-icon></template>
        </el-statistic>
      </el-col>
      <el-col :span="6">
        <el-statistic title="在线回收员" :value="stats.onlineCollectors" value-style="color: #67c23a">
          <template #prefix><el-icon><User /></el-icon></template>
        </el-statistic>
      </el-col>
      <el-col :span="6">
        <el-statistic title="今日已派单" :value="stats.todayDispatched">
          <template #prefix><el-icon><CircleCheck /></el-icon></template>
        </el-statistic>
      </el-col>
      <el-col :span="6">
        <el-statistic title="平均响应" :value="stats.avgResponseTime" suffix="分钟">
          <template #prefix><el-icon><Timer /></el-icon></template>
        </el-statistic>
      </el-col>
    </el-row>

    <!-- 主内容区 -->
    <el-row :gutter="20" class="main-content">
      <!-- 左侧：待派单列表 -->
      <el-col :span="10">
        <el-card class="order-list-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="title">📋 待派单列表</span>
              <el-radio-group v-model="viewMode" size="small">
                <el-radio-button value="card">卡片</el-radio-button>
                <el-radio-button value="table">表格</el-radio-button>
              </el-radio-group>
            </div>
          </template>

          <!-- 卡片视图 -->
          <div v-if="viewMode === 'card'" class="order-cards">
            <el-empty v-if="pendingOrders.length === 0" description="暂无待派单订单" />

            <el-card
                v-for="order in pendingOrders"
                :key="order.id"
                class="order-item"
                :class="{ 'active': selectedOrder?.id === order.id }"
                shadow="hover"
                @click="selectOrder(order)"
            >
              <div class="order-header">
                <span class="order-no">{{ order.orderNo }}</span>
                <el-tag size="small" type="warning">待派单</el-tag>
              </div>

              <div class="order-body">
                <div class="info-row">
                  <el-icon><Location /></el-icon>
                  <span class="address" :title="order.address">{{ order.address }}</span>
                </div>
                <div class="info-row">
                  <el-icon><Refrigerator /></el-icon>
                  <span>{{ order.applianceType }} · {{ order.estimatedWeight }}kg</span>
                  <el-tag size="small" type="info" class="time-tag">{{ order.createTime }}</el-tag>
                </div>
              </div>

              <div class="order-actions">
                <el-button
                    type="primary"
                    size="small"
                    :icon="MagicStick"
                    :loading="loadingOrderId === order.id && dispatchType === 'auto'"
                    @click.stop="handleAutoDispatch(order)"
                >
                  智能派单
                </el-button>
                <el-button
                    type="warning"
                    size="small"
                    plain
                    :icon="User"
                    @click.stop="openManualDispatch(order)"
                >
                  手动派单
                </el-button>
              </div>
            </el-card>
          </div>

          <!-- 表格视图 -->
          <el-table v-else :data="pendingOrders" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNo" label="订单号" width="140" />
            <el-table-column prop="address" label="地址" show-overflow-tooltip />
            <el-table-column prop="applianceType" label="类型" width="100" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button
                    type="primary"
                    size="small"
                    @click="handleAutoDispatch(row)"
                    :loading="loadingOrderId === row.id && dispatchType === 'auto'"
                >
                  智能
                </el-button>
                <el-button
                    type="warning"
                    size="small"
                    plain
                    @click="openManualDispatch(row)"
                >
                  手动
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 中间：地图展示（简化版，实际可接入高德/百度地图） -->
      <el-col :span="9">
        <el-card class="map-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="title">🗺️ 位置分布</span>
              <el-button v-if="selectedOrder" type="primary" size="small" text @click="centerMap">
                定位选中订单
              </el-button>
            </div>
          </template>

          <div class="map-container" ref="mapContainer">
            <!-- 实际项目中这里接入高德地图 -->
            <div v-if="!selectedOrder" class="map-placeholder">
              <el-icon :size="48" color="#dcdfe6"><MapLocation /></el-icon>
              <p>点击左侧订单查看位置分布</p>
              <p class="sub-text">红色标记：订单位置 | 绿色标记：回收员位置</p>
            </div>

            <div v-else class="map-content">
              <div class="selected-order-info">
                <h4>当前选中订单</h4>
                <p><strong>地址：</strong>{{ selectedOrder.address }}</p>
                <p><strong>坐标：</strong>{{ selectedOrder.lat }}, {{ selectedOrder.lng }}</p>
                <div class="nearby-collectors" v-if="nearbyCollectors.length > 0">
                  <h5>附近可用回收员（{{ nearbyCollectors.length }}人）</h5>
                  <div v-for="c in nearbyCollectors" :key="c.id" class="collector-marker">
                    <el-avatar :size="24" :src="c.avatar" />
                    <span>{{ c.realName }}（{{ c.distance }}km）</span>
                    <el-tag size="small" :type="c.status === 'idle' ? 'success' : 'warning'">
                      {{ c.status === 'idle' ? '空闲' : '忙碌' }}
                    </el-tag>
                  </div>
                </div>
                <el-empty v-else description="5公里内暂无可用回收员" :image-size="60" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：最近派单记录 -->
      <el-col :span="5">
        <el-card class="recent-dispatch" shadow="hover">
          <template #header>
            <span class="title">📈 最近派单</span>
          </template>
          <el-timeline>
            <el-timeline-item
                v-for="(record, index) in recentRecords"
                :key="index"
                :type="record.status === 'success' ? 'success' : 'danger'"
                :timestamp="record.time"
                placement="top"
            >
              <p class="record-title">{{ record.orderNo }}</p>
              <p class="record-desc">
                {{ record.type === 'auto' ? '🤖 智能派单' : '👤 手动派单' }} 给 {{ record.collectorName }}
              </p>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-if="recentRecords.length === 0" description="暂无记录" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 手动派单抽屉 -->
    <el-drawer
        v-model="manualDrawerVisible"
        title="手动选择回收员"
        size="400px"
        :destroy-on-close="true"
    >
      <div v-if="currentOrder" class="manual-drawer-content">
        <el-alert
            :title="`为订单 ${currentOrder.orderNo} 选择回收员`"
            type="info"
            :closable="false"
            class="order-alert"
        />

        <div class="order-detail">
          <p><strong>地址：</strong>{{ currentOrder.address }}</p>
          <p><strong>家电：</strong>{{ currentOrder.applianceType }}（{{ currentOrder.estimatedWeight }}kg）</p>
        </div>

        <el-divider content-position="left">可用回收员列表</el-divider>

        <el-radio-group v-model="selectedCollectorId" class="collector-list">
          <el-radio
              v-for="collector in availableCollectors"
              :key="collector.id"
              :value="collector.id"
              class="collector-radio"
          >
            <div class="collector-info">
              <div class="collector-main">
                <el-avatar :size="40" :src="collector.avatar" />
                <div class="collector-text">
                  <div class="name">{{ collector.realName }}</div>
                  <div class="stats">
                    <span>评分 {{ collector.rating }}</span>
                    <el-divider direction="vertical" />
                    <span>今日 {{ collector.todayOrders }}单</span>
                  </div>
                </div>
                <el-tag
                    :type="collector.status === 'idle' ? 'success' : 'warning'"
                    size="small"
                >
                  {{ collector.status === 'idle' ? '空闲' : '忙碌' }}
                </el-tag>
              </div>
              <div class="distance" v-if="collector.distance">
                <el-icon><Location /></el-icon>
                距离订单 {{ collector.distance }}km
              </div>
            </div>
          </el-radio>
        </el-radio-group>

        <div class="drawer-footer">
          <el-button @click="manualDrawerVisible = false">取消</el-button>
          <el-button
              type="warning"
              @click="confirmManualDispatch"
              :loading="loadingOrderId === currentOrder?.id && dispatchType === 'manual'"
              :disabled="!selectedCollectorId"
          >
            确认派单
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document, User, CircleCheck, Timer,
  MagicStick, Location, Refrigerator, MapLocation
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 统计数据
const stats = reactive({
  pendingCount: 0,
  onlineCollectors: 8,
  todayDispatched: 12,
  avgResponseTime: 5.2
})

// 视图模式
const viewMode = ref('card')

// 数据列表
const pendingOrders = ref([])
const loading = ref(false)
const loadingOrderId = ref(null)
const dispatchType = ref('') // 'auto' | 'manual'

// 选中订单（用于地图展示）
const selectedOrder = ref(null)
const nearbyCollectors = ref([])

// 手动派单相关
const manualDrawerVisible = ref(false)
const currentOrder = ref(null)
const availableCollectors = ref([])
const selectedCollectorId = ref(null)

// 最近派单记录
const recentRecords = ref([])

// 获取待派单列表
const loadPendingOrders = async () => {
  loading.value = true
  try {
    const res = await request.get('/recycleOrder/selectPendingDispatch')
    pendingOrders.value = res.data || []
    stats.pendingCount = pendingOrders.value.length
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 选中订单查看详情/地图
const selectOrder = async (order) => {
  selectedOrder.value = order
  // 修复：改为调用 collector 接口获取附近回收员
  try {
    const res = await request.get('/collector/selectNearby', {
      params: {
        lat: order.lat,
        lng: order.lng,
        radius: 5000
      }
    })
    nearbyCollectors.value = res.data || []
  } catch (error) {
    console.error('获取附近回收员失败:', error)
    nearbyCollectors.value = []
  }
}

// 智能派单（自动）
const handleAutoDispatch = async (order) => {
  try {
    await ElMessageBox.confirm(
        `确定对订单 ${order.orderNo} 执行智能派单吗？<br>系统将自动选择5公里内最近的可用回收员`,
        '确认智能派单',
        {
          confirmButtonText: '确认派单',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: true
        }
    )

    loadingOrderId.value = order.id
    dispatchType.value = 'auto'

    // 修复：使用正确的路径参数格式
    await request.post(`/recycleOrder/dispatch/${order.id}`)

    ElMessage.success(`订单 ${order.orderNo} 智能派单成功！`)
    addRecentRecord(order, 'auto', '系统自动分配')

    await loadPendingOrders()
    if (selectedOrder.value?.id === order.id) {
      selectedOrder.value = null
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '派单失败')
    }
  } finally {
    loadingOrderId.value = null
    dispatchType.value = ''
  }
}

// 打开手动派单抽屉
const openManualDispatch = async (order) => {
  currentOrder.value = order
  selectedCollectorId.value = null
  manualDrawerVisible.value = true

  // 修复：改为调用 collector 接口获取可用回收员列表
  try {
    const res = await request.get('/collector/selectNearby', {
      params: {
        lat: order.lat,
        lng: order.lng,
        radius: 10000  // 手动派单范围扩大到10公里
      }
    })
    availableCollectors.value = res.data || []
  } catch (error) {
    console.error('获取可用回收员失败:', error)
    availableCollectors.value = []
    ElMessage.warning('获取回收员列表失败，请稍后重试')
  }
}

// 确认手动派单
const confirmManualDispatch = async () => {
  if (!selectedCollectorId.value) {
    ElMessage.warning('请选择回收员')
    return
  }

  const collector = availableCollectors.value.find(c => c.id === selectedCollectorId.value)

  try {
    loadingOrderId.value = currentOrder.value.id
    dispatchType.value = 'manual'

    await request.post('/recycleOrder/manualDispatch', {
      orderId: currentOrder.value.id,
      collectorId: selectedCollectorId.value
    })

    ElMessage.success(`已手动派单给 ${collector?.realName || '回收员'}`)
    addRecentRecord(currentOrder.value, 'manual', collector?.realName)

    manualDrawerVisible.value = false
    await loadPendingOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '手动派单失败')
  } finally {
    loadingOrderId.value = null
    dispatchType.value = ''
  }
}

// 添加最近记录
const addRecentRecord = (order, type, collectorName) => {
  recentRecords.value.unshift({
    orderNo: order.orderNo,
    type,
    collectorName,
    time: new Date().toLocaleTimeString(),
    status: 'success'
  })
  if (recentRecords.value.length > 10) {
    recentRecords.value.pop()
  }
}

// 地图居中（模拟）
const centerMap = () => {
  ElMessage.info('地图已定位到选中订单位置')
}

onMounted(() => {
  loadPendingOrders()
  setInterval(loadPendingOrders, 30000)
})
</script>

<style scoped lang="scss">
.dispatch-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);

  .stats-bar {
    margin-bottom: 20px;

    :global(.el-statistic__content) {
      font-size: 24px;
      font-weight: 600;
    }
  }

  .main-content {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .title {
        font-size: 16px;
        font-weight: 600;
      }
    }

    .order-list-card {
      height: calc(100vh - 220px);

      .order-cards {
        height: calc(100% - 40px);
        overflow-y: auto;

        .order-item {
          margin-bottom: 12px;
          cursor: pointer;
          transition: all 0.3s;

          &:hover, &.active {
            border-color: #409eff;
            transform: translateY(-2px);
          }

          .order-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;

            .order-no {
              font-weight: 600;
              color: #303133;
            }
          }

          .order-body {
            margin-bottom: 12px;

            .info-row {
              display: flex;
              align-items: center;
              margin-bottom: 8px;
              color: #606266;
              font-size: 13px;

              .el-icon {
                margin-right: 6px;
              }

              .address {
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                flex: 1;
              }

              .time-tag {
                margin-left: auto;
              }
            }
          }

          .order-actions {
            display: flex;
            gap: 8px;

            .el-button {
              flex: 1;
            }
          }
        }
      }
    }

    .map-card {
      height: calc(100vh - 220px);

      .map-container {
        height: calc(100% - 40px);
        background: #f5f7fa;
        border-radius: 4px;
        position: relative;

        .map-placeholder {
          height: 100%;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          color: #909399;

          .sub-text {
            font-size: 12px;
            margin-top: 8px;
          }
        }

        .map-content {
          padding: 16px;

          .selected-order-info {
            h4 {
              margin: 0 0 12px 0;
              color: #303133;
            }

            p {
              margin: 8px 0;
              color: #606266;
              font-size: 14px;
            }

            .nearby-collectors {
              margin-top: 20px;

              h5 {
                margin: 0 0 12px 0;
                color: #303133;
              }

              .collector-marker {
                display: flex;
                align-items: center;
                gap: 8px;
                padding: 8px;
                background: #fff;
                border-radius: 4px;
                margin-bottom: 8px;
                border: 1px solid #e4e7ed;
              }
            }
          }
        }
      }
    }

    .recent-dispatch {
      height: calc(100vh - 220px);

      .record-title {
        font-weight: 600;
        margin: 0 0 4px 0;
      }

      .record-desc {
        font-size: 12px;
        color: #909399;
        margin: 0;
      }
    }
  }

  .manual-drawer-content {
    .order-alert {
      margin-bottom: 16px;
    }

    .order-detail {
      background: #f5f7fa;
      padding: 12px;
      border-radius: 4px;
      margin-bottom: 16px;

      p {
        margin: 4px 0;
        color: #606266;
      }
    }

    .collector-list {
      width: 100%;

      .collector-radio {
        width: 100%;
        margin-bottom: 12px;
        margin-right: 0;

        :global(.el-radio__input) {
          margin-top: 12px;
        }

        :global(.el-radio__label) {
          width: calc(100% - 24px);
          padding-left: 8px;
        }
      }

      .collector-info {
        width: 100%;

        .collector-main {
          display: flex;
          align-items: center;
          gap: 12px;

          .collector-text {
            flex: 1;

            .name {
              font-weight: 600;
              color: #303133;
              margin-bottom: 4px;
            }

            .stats {
              font-size: 12px;
              color: #909399;
            }
          }
        }

        .distance {
          margin-top: 8px;
          color: #409eff;
          font-size: 13px;
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }

    .drawer-footer {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 16px;
      background: #fff;
      border-top: 1px solid #e4e7ed;
      display: flex;
      justify-content: flex-end;
      gap: 12px;
    }
  }
}
</style>
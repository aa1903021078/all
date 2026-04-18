<template>
  <div class="workbench">
    <!-- 今日数据卡片 -->
    <div class="stats-cards">
      <div class="stat-card blue">
        <div class="icon">📦</div>
        <div class="info">
          <div class="value">{{ stats.todayOrderCount }}</div>
          <div class="label">今日接单</div>
        </div>
      </div>
      <div class="stat-card green">
        <div class="icon">✅</div>
        <div class="info">
          <div class="value">{{ stats.completedCount }}</div>
          <div class="label">已完成</div>
        </div>
      </div>
      <div class="stat-card orange">
        <div class="icon">💰</div>
        <div class="info">
          <div class="value">¥{{ stats.todayIncome }}</div>
          <div class="label">今日收入</div>
        </div>
      </div>
      <div class="stat-card purple">
        <div class="icon">⭐</div>
        <div class="info">
          <div class="value">{{ stats.rating }}</div>
          <div class="label">我的评分</div>
        </div>
      </div>
    </div>

    <!-- 新订单提醒 -->
    <el-card class="new-orders" v-if="newOrders.length > 0">
      <template #header>
        <div class="card-header">
          <span>🆕 新订单待接单</span>
          <el-button type="primary" size="small" @click="refreshOrders">刷新</el-button>
        </div>
      </template>

      <div class="order-list">
        <div v-for="order in newOrders" :key="order.id" class="order-item">
          <div class="order-info">
            <div class="header">
              <span class="order-no">{{ order.orderNo }}</span>
              <el-tag :type="order.urgencyLevel === 3 ? 'danger' : order.urgencyLevel === 2 ? 'warning' : 'info'">
                {{ urgencyText(order.urgencyLevel) }}
              </el-tag>
            </div>
            <div class="detail">
              <p><strong>品类：</strong>{{ order.applianceTypeName }}</p>
              <p><strong>预估重量：</strong>{{ order.estimatedWeight }}kg</p>
              <p><strong>地址：</strong>{{ order.addressDetail }}</p>
              <p><strong>期望时间：</strong>{{ order.expectDate }} {{ order.expectTimeStart }}-{{ order.expectTimeEnd }}</p>
            </div>
          </div>
          <div class="order-actions">
            <el-button type="success" size="large" @click="acceptOrder(order.id)">
              立即接单
            </el-button>
            <el-button type="danger" size="large" @click="rejectOrder(order.id)">
              拒绝
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 进行中的订单 -->
    <el-card class="active-orders" v-if="activeOrders.length > 0">
      <template #header>
        <span>🚚 进行中的订单</span>
      </template>

      <el-timeline>
        <el-timeline-item
            v-for="order in activeOrders"
            :key="order.id"
            :type="order.status === 2 ? 'primary' : 'warning'"
        >
          <div class="active-order-item">
            <div class="info">
              <h4>{{ order.applianceTypeName }} - {{ order.orderNo }}</h4>
              <p>{{ order.addressDetail }}</p>
              <p>联系人：{{ order.contactName }} {{ order.contactPhone }}</p>
            </div>
            <div class="actions">
              <el-button
                  v-if="order.status === 2"
                  type="primary"
                  @click="arrive(order.id)"
              >
                确认到达
              </el-button>
              <el-button
                  v-if="order.status === 3"
                  type="success"
                  @click="complete(order)"
              >
                完成订单
              </el-button>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <!-- 完成订单弹窗 -->
    <el-dialog v-model="completeVisible" title="完成订单" width="500px">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="实际重量(kg)">
          <el-input-number v-model="completeForm.weightActual" :min="0.1" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="实际金额(元)">
          <el-input-number v-model="completeForm.priceActual" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmComplete" :loading="completing">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const user = JSON.parse(localStorage.getItem('user') || '{}')
const stats = ref({
  todayOrderCount: 0,
  completedCount: 0,
  todayIncome: 0,
  rating: 5.0
})
const newOrders = ref([])
const activeOrders = ref([])
const timer = ref(null)
const completeVisible = ref(false)
const completing = ref(false)
const completeForm = ref({
  orderId: null,
  weightActual: 0,
  priceActual: 0
})

onMounted(() => {
  loadStats()
  loadOrders()
  timer.value = setInterval(loadOrders, 10000) // 每10秒刷新
})

onUnmounted(() => {
  clearInterval(timer.value)
})

const loadStats = async () => {
  try {
    const res = await request.get(`/collector/todayStats/${user.id}`)
    stats.value = res
  } catch (e) {}
}

const loadOrders = async () => {
  try {
    const res = await request.get(`/recycleOrder/selectByCollector/${user.id}`)
    // 待接单（status=1）
    newOrders.value = res.filter(o => o.status === 1)
    // 进行中（status=2已接单 或 status=3已到达）
    activeOrders.value = res.filter(o => o.status === 2 || o.status === 3)
  } catch (e) {}
}

const urgencyText = (level) => {
  const map = { 1: '普通', 2: '较急', 3: '紧急' }
  return map[level] || '普通'
}

const refreshOrders = () => {
  loadOrders()
  ElMessage.success('已刷新')
}

const acceptOrder = async (orderId) => {
  try {
    await request.post('/order/accept', null, { params: { orderId, collectorId: user.id } })
    ElMessage.success('接单成功')
    loadOrders()
    loadStats()
  } catch (error) {
    ElMessage.error(error.message || '接单失败')
  }
}

const rejectOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm('确认拒绝此订单？', '提示')
    await request.post('/order/cancel', null, { params: { orderId, reason: '回收员拒绝' } })
    ElMessage.success('已拒绝')
    loadOrders()
  } catch (e) {}
}

const arrive = async (orderId) => {
  try {
    await request.post('/order/arrive', null, { params: { orderId } })
    ElMessage.success('已确认到达')
    loadOrders()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const complete = (order) => {
  completeForm.value = {
    orderId: order.id,
    weightActual: order.estimatedWeight,
    priceActual: order.priceEstimate || 0
  }
  completeVisible.value = true
}

const confirmComplete = async () => {
  completing.value = true
  try {
    await request.post('/order/complete', {
      id: completeForm.value.orderId,
      weightActual: completeForm.value.weightActual,
      priceActual: completeForm.value.priceActual
    })
    ElMessage.success('订单完成')
    completeVisible.value = false
    loadOrders()
    loadStats()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    completing.value = false
  }
}
</script>

<style scoped lang="scss">
.workbench {
  .stats-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 24px;

    .stat-card {
      background: white;
      padding: 20px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      box-shadow: 0 2px 8px rgba(0,0,0,0.05);

      &.blue { border-left: 4px solid #1890ff; }
      &.green { border-left: 4px solid #52c41a; }
      &.orange { border-left: 4px solid #fa8c16; }
      &.purple { border-left: 4px solid #722ed1; }

      .icon {
        font-size: 40px;
        margin-right: 15px;
      }

      .info {
        .value {
          font-size: 28px;
          font-weight: bold;
          color: #333;
        }

        .label {
          color: #666;
          margin-top: 5px;
        }
      }
    }
  }

  .new-orders {
    margin-bottom: 24px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .order-list {
      .order-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20px;
        border: 1px solid #e8e8e8;
        border-radius: 8px;
        margin-bottom: 15px;
        background: #fafafa;

        .order-info {
          flex: 1;

          .header {
            display: flex;
            align-items: center;
            gap: 15px;
            margin-bottom: 10px;

            .order-no {
              font-weight: bold;
              font-size: 16px;
            }
          }

          .detail {
            p {
              margin: 5px 0;
              color: #666;
            }
          }
        }

        .order-actions {
          display: flex;
          flex-direction: column;
          gap: 10px;
        }
      }
    }
  }

  .active-orders {
    .active-order-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px;
      background: #f6ffed;
      border-radius: 8px;
      margin-bottom: 10px;

      .info {
        h4 {
          margin-bottom: 8px;
        }

        p {
          color: #666;
          font-size: 13px;
          margin: 3px 0;
        }
      }
    }
  }
}
</style>
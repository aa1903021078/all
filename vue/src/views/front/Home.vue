<template>
  <div class="home-container">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <el-card shadow="hover" class="welcome-card">
        <div class="welcome-content">
          <div class="avatar">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="user-info">
            <h3>欢迎回来，{{ userInfo.name || userInfo.username }}</h3>
            <p>角色：{{ userInfo.role }}</p>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 快捷操作区域 -->
    <div class="quick-actions">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="hover" class="action-card" @click="goToAppointment">
            <div class="action-content">
              <el-icon class="action-icon" color="#409eff"><DocumentAdd /></el-icon>
              <h4>预约回收</h4>
              <p>提交家电回收申请</p>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="action-card" @click="goToOrders">
            <div class="action-content">
              <el-icon class="action-icon" color="#67c23a"><List /></el-icon>
              <h4>查看订单</h4>
              <p>跟踪回收进度</p>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="action-card" @click="goToAddress">
            <div class="action-content">
              <el-icon class="action-icon" color="#e6a23c"><Location /></el-icon>
              <h4>地址管理</h4>
              <p>管理回收地址</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 最新订单 -->
    <div class="latest-orders">
      <el-card shadow="hover" class="order-card">
        <template #header>
          <div class="card-header">
            <span>最新订单</span>
            <el-button type="primary" text @click="goToOrders">查看全部</el-button>
          </div>
        </template>

        <el-table :data="latestOrders" style="width: 100%" v-loading="loading">
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column prop="applianceTypeName" label="家电类型" />
          <el-table-column prop="createTime" label="下单时间" />
          <el-table-column label="状态">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, DocumentAdd, List, Location } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const userInfo = ref(JSON.parse(localStorage.getItem('system-user') || '{}'))
const latestOrders = ref([])
const loading = ref(false)

const goToAppointment = () => router.push('/front/appointment')
const goToOrders = () => router.push('/front/recycleOrder')
const goToAddress = () => router.push('/front/userAddress')

const getStatusType = (status) => {
  const types = ['info', 'warning', 'success', 'danger']
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = ['待接单', '已接单', '已完成', '已取消']
  return texts[status] || '未知'
}

const loadLatestOrders = async () => {
  loading.value = true
  try {
    const res = await request.get('/recycle/orders')
    if (res.code === '200') {
      latestOrders.value = res.data.slice(0, 5) // 只显示最新5条
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadLatestOrders()
})
</script>

<style scoped>
.home-container {
  padding: 20px;
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
}

.welcome-section {
  margin-bottom: 30px;
}

.welcome-card {
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
}

.welcome-content {
  display: flex;
  align-items: center;
  gap: 20px;
  color: white;
}

.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
}

.user-info h3 {
  margin: 0 0 5px 0;
  font-size: 24px;
}

.user-info p {
  margin: 0;
  opacity: 0.9;
}

.quick-actions {
  margin-bottom: 30px;
}

.action-card {
  cursor: pointer;
  transition: all 0.3s;
}

.action-card:hover {
  transform: translateY(-5px);
}

.action-content {
  text-align: center;
  padding: 20px 0;
}

.action-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.action-content h4 {
  margin: 10px 0 5px 0;
  font-size: 18px;
}

.action-content p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
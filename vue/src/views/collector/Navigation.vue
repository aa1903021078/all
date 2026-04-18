<template>
  <div class="navigation-page">
    <div class="map-container" id="mapContainer"></div>

    <div class="route-panel" v-if="currentOrder">
      <h4>当前订单导航</h4>
      <div class="route-info">
        <p><strong>目的地：</strong>{{ currentOrder.addressDetail }}</p>
        <p><strong>距离：</strong>约 {{ distance }} 公里</p>
        <p><strong>预计时间：</strong>{{ duration }} 分钟</p>
      </div>
      <el-button type="primary" @click="startNavi">开始导航</el-button>
    </div>

    <div class="order-selector" v-else>
      <h4>选择订单开始导航</h4>
      <el-select v-model="selectedOrderId" placeholder="选择进行中的订单" style="width: 100%">
        <el-option
            v-for="order in activeOrders"
            :key="order.id"
            :label="order.applianceTypeName + ' - ' + order.addressDetail"
            :value="order.id"
        />
      </el-select>
      <el-button type="primary" style="margin-top: 10px; width: 100%" @click="loadRoute">规划路线</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const map = ref(null)
const currentOrder = ref(null)
const activeOrders = ref([])
const selectedOrderId = ref(null)
const distance = ref(0)
const duration = ref(0)
const user = JSON.parse(localStorage.getItem('user') || '{}')

onMounted(() => {
  initMap()
  loadActiveOrders()
})

const initMap = () => {
  // 实际项目中引入高德地图API
  // const AMap = window.AMap
  // map.value = new AMap.Map('mapContainer', {
  //   zoom: 12,
  //   center: [116.397428, 39.90923]
  // })
}

const loadActiveOrders = async () => {
  try {
    const res = await request.get(`/order/selectByCollector/${user.id}`)
    activeOrders.value = res.filter(o => o.status === 2)
  } catch (e) {}
}

const loadRoute = () => {
  const order = activeOrders.value.find(o => o.id === selectedOrderId.value)
  if (!order) {
    ElMessage.warning('请选择订单')
    return
  }
  currentOrder.value = order
  distance.value = (Math.random() * 5 + 1).toFixed(1)
  duration.value = Math.floor(Math.random() * 20 + 10)
}

const startNavi = () => {
  // 调用地图导航API
  window.open(`https://map.qq.com/nav/drive#routes/page?end=${currentOrder.value.addressLat},${currentOrder.value.addressLng}`, '_blank')
}
</script>

<style scoped lang="scss">
.navigation-page {
  height: calc(100vh - 140px);
  position: relative;

  .map-container {
    width: 100%;
    height: 100%;
    background: #e4e7ed;
    display: flex;
    align-items: center;
    justify-content: center;

    &::after {
      content: '地图区域（实际引入高德/腾讯地图SDK）';
      color: #909399;
    }
  }

  .route-panel, .order-selector {
    position: absolute;
    bottom: 20px;
    left: 20px;
    right: 20px;
    background: white;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 4px 20px rgba(0,0,0,0.15);

    h4 {
      margin-bottom: 15px;
    }

    .route-info {
      margin-bottom: 15px;

      p {
        margin: 5px 0;
        color: #606266;
      }
    }
  }
}
</style>
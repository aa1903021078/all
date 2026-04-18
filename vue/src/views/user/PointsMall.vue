<template>
  <div class="points-mall-page">
    <!-- 头部积分信息 -->
    <div class="mall-header">
      <div class="user-points-card">
        <div class="points-icon">
          <el-icon><Coin /></el-icon>
        </div>
        <div class="points-info">
          <p class="points-label">我的积分</p>
          <p class="points-value">{{ userPoints }}</p>
        </div>
      </div>
      <div class="header-title">
        <h2>积分兑换中心</h2>
        <p>回收得积分，好礼兑不停</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showExchangeRecords">
          <el-icon><List /></el-icon>我的兑换
        </el-button>
      </div>
    </div>

    <!-- 积分商城标题区域 -->
    <div class="section-header">
      <div class="section-title">
        <el-icon class="title-icon"><ShoppingBag /></el-icon>
        <div class="title-text">
          <h3>积分商城</h3>
          <span class="subtitle">挑选心仪商品，积分当钱花</span>
        </div>
      </div>
      <div class="goods-stats">
        <el-tag type="success" effect="plain" size="large">
          <el-icon><Goods /></el-icon>
          共 {{ goodsList.length }} 件商品可兑换
        </el-tag>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="products-grid">
      <div
          v-for="goods in goodsList"
          :key="goods.id"
          class="product-card"
          :class="{ 'out-of-stock': goods.stock <= 0 }"
      >
        <div class="product-image">
          <img :src="goods.image" style="width: 100%; height: 100%; object-fit: cover;" />
          <div v-if="goods.stock <= 0" class="stock-overlay">已兑完</div>
          <div v-else-if="goods.stock <= 10" class="stock-tag">仅剩{{ goods.stock }}件</div>
        </div>
        <div class="product-info">
          <h4 class="product-name">{{ goods.name }}</h4>
          <p class="product-desc">{{ goods.description }}</p>
          <div class="product-footer">
            <div class="product-points">
              <el-icon><Coin /></el-icon>
              <span>{{ goods.pointsRequired }}</span>
            </div>
            <el-button
                type="primary"
                size="small"
                :disabled="goods.stock <= 0 || userPoints < goods.pointsRequired"
                @click="exchangeGoods(goods)"
            >
              {{ goods.stock <= 0 ? '已兑完' : '立即兑换' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态提示 -->
    <div v-if="goodsList.length === 0" class="empty-state">
      <el-icon :size="60" color="#dcdfe6"><ShoppingBag /></el-icon>
      <p>暂无可用商品</p>
    </div>

    <!-- 兑换确认抽屉（右侧滑出） -->
    <el-drawer
        v-model="exchangeVisible"
        title="确认兑换"
        size="480px"
        direction="rtl"
        destroy-on-close
        class="exchange-drawer"
    >
      <div class="drawer-content" v-if="selectedGoods">
        <!-- 商品预览 -->
        <div class="goods-preview-card">
          <img :src="selectedGoods.image" class="preview-img" />
          <div class="preview-info">
            <h4>{{ selectedGoods.name }}</h4>
            <p class="preview-desc">{{ selectedGoods.description }}</p>
            <div class="preview-price">
              <el-icon><Coin /></el-icon>
              <span>{{ selectedGoods.pointsRequired }} 积分</span>
            </div>
          </div>
        </div>

        <!-- 收货地址区域 -->
        <div class="address-section">
          <div class="section-label">
            <span>收货地址</span>
            <el-button
                v-if="userAddress.id"
                type="primary"
                link
                size="small"
                @click="showAddressSelector = true"
            >
              更换地址
            </el-button>
            <el-button
                v-else
                type="primary"
                link
                size="small"
                @click="goToAddAddress"
            >
              添加地址
            </el-button>
          </div>

          <!-- 有地址的情况 -->
          <div v-if="userAddress.id" class="address-card">
            <div class="address-header">
              <span class="contact-name">{{ userAddress.name || userAddress.contactName }}</span>
              <span class="contact-phone">{{ userAddress.phone || userAddress.contactPhone }}</span>
              <el-tag v-if="userAddress.isDefault" type="success" size="small" effect="plain">默认</el-tag>
            </div>
            <div class="address-detail">
              <el-icon><Location /></el-icon>
              <span>{{ userAddress.address }}</span>
            </div>
          </div>

          <!-- 无地址的情况 -->
          <div v-else class="no-address">
            <el-icon :size="40" color="#dcdfe6"><Location /></el-icon>
            <p>暂无收货地址，请先添加</p>
            <el-button type="primary" @click="goToAddAddress">去添加地址</el-button>
          </div>
        </div>

        <!-- 积分信息 -->
        <div class="points-summary">
          <div class="summary-row">
            <span>我的积分</span>
            <span class="my-points">{{ userPoints }} 分</span>
          </div>
          <div class="summary-row deduction">
            <span>兑换所需</span>
            <span class="need-points">- {{ selectedGoods.pointsRequired }} 分</span>
          </div>
          <el-divider />
          <div class="summary-row total">
            <span>兑换后剩余</span>
            <span :class="remainingPoints < 0 ? 'insufficient' : 'remaining'">
              {{ remainingPoints }} 分
            </span>
          </div>
        </div>

        <!-- 备注 -->
        <div class="remark-section">
          <el-input
              v-model="exchangeForm.remark"
              placeholder="备注信息（选填）：如配送时间要求等"
              type="textarea"
              :rows="2"
          />
        </div>
      </div>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="exchangeVisible = false" size="large">再想想</el-button>
          <el-button
              type="primary"
              size="large"
              :loading="exchanging"
              :disabled="!userAddress.id || remainingPoints < 0"
              @click="confirmExchange"
              class="confirm-btn"
          >
            确认兑换
          </el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 地址选择器弹窗 -->
    <el-dialog v-model="showAddressSelector" title="选择收货地址" width="600px">
      <div v-if="addressList.length > 0" class="address-list">
        <div
            v-for="addr in addressList"
            :key="addr.id"
            class="address-item"
            :class="{ 'active': selectedAddressId === addr.id }"
            @click="selectAddress(addr)"
        >
          <div class="addr-info">
            <div class="addr-header">
              <span class="name">{{ addr.name || addr.contactName }}</span>
              <span class="phone">{{ addr.phone || addr.contactPhone }}</span>
              <el-tag v-if="addr.isDefault" type="success" size="small">默认</el-tag>
            </div>
            <div class="addr-detail">{{ addr.address }}</div>
          </div>
          <el-icon v-if="selectedAddressId === addr.id" class="check-icon"><Check /></el-icon>
        </div>
      </div>
      <div v-else class="no-address-dialog">
        <p>暂无其他地址</p>
      </div>
      <template #footer>
        <el-button @click="showAddressSelector = false">取消</el-button>
        <el-button type="primary" @click="confirmAddressChange">确定</el-button>
      </template>
    </el-dialog>

    <!-- 我的兑换记录对话框 -->
    <el-dialog v-model="recordsVisible" title="我的兑换记录" width="900px">
      <el-table :data="exchangeRecords" stripe>
        <el-table-column prop="id" label="兑换单号" width="100" />
        <el-table-column label="商品信息" min-width="200">
          <template #default="{ row }">
            <div class="record-goods">
              <span>{{ row.goodsName }}</span>
              <span class="points">-{{ row.pointsSpent }}积分</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="兑换时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Coin, List, ShoppingBag, Goods, Location, Check } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'

const router = useRouter()

// 用户信息
const currentUser = ref({})
const userPoints = ref(0)
const goodsList = ref([])
const exchangeRecords = ref([])

// 兑换相关
const exchangeVisible = ref(false)
const exchanging = ref(false)
const selectedGoods = ref(null)
const recordsVisible = ref(false)
const showAddressSelector = ref(false)

// 地址相关数据
const userAddress = ref({})
const addressList = ref([])
const selectedAddressId = ref(null)
const tempSelectedAddress = ref(null)

const exchangeForm = ref({
  remark: ''
})

const remainingPoints = computed(() => {
  if (!selectedGoods.value) return 0
  return userPoints.value - selectedGoods.value.pointsRequired
})

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'info' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待发放', 1: '已发放', 2: '已取消' }
  return texts[status] || '未知'
}

// ==================== 获取用户最新积分 ====================
const loadUserPoints = async () => {
  try {
    // 兼容多种可能的用户ID字段名
    const userId = currentUser.value.id || currentUser.value.userId || currentUser.value.user_id

    console.log('尝试获取积分，用户ID:', userId)
    console.log('currentUser对象:', currentUser.value)

    if (!userId) {
      console.error('无法获取用户ID，currentUser内容:', currentUser.value)
      ElMessage.warning('获取用户信息失败，请重新登录')
      return
    }

    const res = await request.get(`/user/selectById/${userId}`)
    console.log('后端返回:', res)

    if (res?.data) {
      const newPoints = res.data.points || 0
      userPoints.value = newPoints
      console.log('最新积分:', newPoints)

      // 关键修改：使用 'user' 而不是 'userInfo'
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      user.points = newPoints
      user.id = userId // 确保id被保存
      localStorage.setItem('user', JSON.stringify(user))

      console.log('积分已同步:', newPoints)
    } else {
      console.error('后端返回数据为空:', res)
    }
  } catch (error) {
    console.error('获取积分失败:', error)
    // 关键修改：使用 'user' 而不是 'userInfo'
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    userPoints.value = user.points || 0
  }
}

// ==================== 商品管理 ====================
const loadAvailableGoods = async () => {
  try {
    const res = await request.get('/pointsGoods/selectAll')
    const list = res?.data || []
    goodsList.value = list.filter(item => item.status === 1 && item.stock > 0)
  } catch (error) {
    console.error('加载商品失败', error)
    goodsList.value = []
  }
}

// ==================== 地址管理 ====================
const loadUserDefaultAddress = async () => {
  try {
    const userId = currentUser.value.id || currentUser.value.userId || currentUser.value.user_id
    if (!userId) return

    const res = await request.get(`/userAddress/selectDefault/${userId}`)
    userAddress.value = res?.data || {}
  } catch (error) {
    console.log('获取默认地址失败', error)
    userAddress.value = {}
  }
}

const loadUserAddressList = async () => {
  try {
    const userId = currentUser.value.id || currentUser.value.userId || currentUser.value.user_id
    if (!userId) return

    const res = await request.get(`/userAddress/selectByUser/${userId}`)
    addressList.value = res?.data || []
  } catch (error) {
    console.error('获取地址列表失败', error)
    addressList.value = []
  }
}

// ==================== 兑换逻辑 ====================
const exchangeGoods = async (goods) => {
  if (userPoints.value < goods.pointsRequired) {
    ElMessage.warning('积分不足')
    return
  }

  selectedGoods.value = goods
  exchangeVisible.value = true

  await loadUserDefaultAddress()
  await loadUserAddressList()
}

const selectAddress = (addr) => {
  selectedAddressId.value = addr.id
  tempSelectedAddress.value = addr
}

const confirmAddressChange = () => {
  if (tempSelectedAddress.value) {
    userAddress.value = tempSelectedAddress.value
  }
  showAddressSelector.value = false
}

const goToAddAddress = () => {
  router.push('/userAddress')
}

const confirmExchange = async () => {
  if (!userAddress.value.id) {
    ElMessage.warning('请选择收货地址')
    return
  }

  exchanging.value = true
  try {
    const userId = currentUser.value.id || currentUser.value.userId || currentUser.value.user_id

    await request.post('/pointsExchange/exchange', null, {
      params: {
        userId: userId,
        goodsId: selectedGoods.value.id
      }
    })

    ElMessage.success('兑换成功，请等待发货')

    // 兑换成功后刷新积分
    await loadUserPoints()

    exchangeVisible.value = false
    exchangeForm.value.remark = ''
    await loadAvailableGoods()
  } catch (error) {
    console.error('兑换失败', error)
    ElMessage.error(error.response?.data?.message || '兑换失败')
  } finally {
    exchanging.value = false
  }
}

const showExchangeRecords = async () => {
  recordsVisible.value = true
  try {
    const res = await request.get('/pointsExchange/selectPage', {
      params: { pageNum: 1, pageSize: 50 }
    })
    exchangeRecords.value = res?.data?.list || []
  } catch (error) {
    console.error('加载记录失败', error)
    exchangeRecords.value = []
  }
}

// ==================== 页面加载时正确处理 ====================
onMounted(() => {
  // 关键修改：使用 'user' 而不是 'userInfo'
  const user = JSON.parse(localStorage.getItem('user') || '{}')

  console.log('onMounted - 从localStorage读取:', user)

  // 关键修复：如果没有id，尝试从其他字段获取或提示登录
  const userId = user.id || user.userId || user.user_id

  if (!userId) {
    console.error('localStorage中没有用户ID，请检查登录逻辑')
    ElMessage.error('请先登录')
    return
  }

  // 确保currentUser包含id
  currentUser.value = { ...user, id: userId }

  // 现在可以安全地加载积分
  loadUserPoints()
  loadAvailableGoods()
})
</script>

<style scoped>
.points-mall-page {
  padding-bottom: 40px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.mall-header {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  padding: 40px 20px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 20px;
}

.user-points-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255,255,255,0.2);
  padding: 16px 24px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.points-icon {
  width: 50px; height: 50px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #67c23a;
  font-size: 24px;
}

.points-value { font-size: 32px; font-weight: 700; color: #fff; margin: 0; }
.points-label { color: rgba(255,255,255,0.9); margin: 0 0 4px 0; }
.header-title { text-align: center; color: #fff; flex: 1; }
.header-title h2 { margin: 0 0 8px 0; font-size: 28px; }

.section-header {
  max-width: 1200px;
  margin: 0 auto 20px;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 32px;
  color: #67c23a;
  background: #f0f9eb;
  padding: 8px;
  border-radius: 8px;
}

.title-text h3 {
  margin: 0;
  font-size: 20px;
  color: #303133;
  font-weight: 600;
}

.title-text .subtitle {
  font-size: 13px;
  color: #909399;
  margin-left: 8px;
  font-weight: normal;
}

.products-grid {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  transition: all 0.3s;
}

.product-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }
.product-card.out-of-stock { opacity: 0.6; }
.product-image { position: relative; height: 200px; }
.stock-overlay {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 18px; font-weight: 600;
}
.stock-tag {
  position: absolute; top: 10px; right: 10px;
  background: #f56c6c; color: #fff;
  padding: 4px 10px; border-radius: 12px; font-size: 12px;
}
.product-info { padding: 16px; }
.product-name { margin: 0 0 8px 0; font-size: 16px; color: #303133; }
.product-desc { margin: 0 0 16px 0; font-size: 13px; color: #909399; }
.product-footer { display: flex; align-items: center; justify-content: space-between; }
.product-points { display: flex; align-items: center; gap: 4px; color: #f56c6c; font-weight: 600; font-size: 18px; }

.empty-state {
  max-width: 1200px;
  margin: 40px auto;
  text-align: center;
  color: #909399;
  padding: 60px 20px;
  background: #fff;
  border-radius: 12px;
}
.empty-state p { margin-top: 16px; font-size: 14px; }

/* 抽屉内样式 */
.drawer-content { padding: 10px; }
.goods-preview-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #f0f9eb 0%, #e6f7d6 100%);
  border-radius: 12px;
  margin-bottom: 24px;
}
.preview-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  border: 3px solid #fff;
}
.preview-info h4 { margin: 0 0 8px 0; font-size: 18px; color: #303133; }
.preview-desc { margin: 0 0 12px 0; font-size: 13px; color: #606266; }
.preview-price {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #f56c6c;
  font-size: 20px;
  font-weight: 600;
}

.address-section { margin-bottom: 24px; }
.section-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
  font-weight: 600;
}
.address-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  position: relative;
}
.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.contact-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.contact-phone {
  font-size: 14px;
  color: #606266;
}
.address-detail {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}
.address-detail .el-icon {
  margin-top: 2px;
  flex-shrink: 0;
}

.no-address {
  text-align: center;
  padding: 40px 20px;
  background: #f5f7fa;
  border-radius: 8px;
  border: 2px dashed #dcdfe6;
}
.no-address p {
  margin: 12px 0;
  color: #909399;
  font-size: 14px;
}

.points-summary {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}
.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}
.summary-row:last-child { margin-bottom: 0; }
.my-points { color: #67c23a; font-weight: 600; }
.need-points { color: #f56c6c; font-weight: 600; }
.remaining { color: #409eff; font-weight: 600; }
.insufficient { color: #f56c6c; font-weight: 600; }

.remark-section { margin-bottom: 20px; }

.drawer-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
.confirm-btn {
  min-width: 120px;
}

/* 地址选择器样式 */
.address-list {
  max-height: 400px;
  overflow-y: auto;
}
.address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s;
}
.address-item:hover, .address-item.active {
  border-color: #67c23a;
  background: #f0f9eb;
}
.addr-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.addr-header .name {
  font-weight: 600;
  font-size: 15px;
}
.addr-header .phone {
  color: #606266;
  font-size: 14px;
}
.addr-detail {
  color: #909399;
  font-size: 13px;
  line-height: 1.4;
}
.check-icon {
  color: #67c23a;
  font-size: 20px;
}
.no-address-dialog {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.record-goods { display: flex; flex-direction: column; }
.record-goods .points { color: #f56c6c; font-size: 12px; }
</style>
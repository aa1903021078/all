<template>
  <div class="app-container">
    <!-- Featured Packages -->
    <div class="section-title">
      <h2>精选套餐</h2>
      <p class="section-subtitle">为您和宝宝提供最专业的月子护理服务</p>
    </div>

    <el-row v-loading="loading" :gutter="24" class="package-grid">
      <el-col v-for="item in packageList" :key="item.id" :xs="24" :sm="12" :md="8" style="margin-bottom: 24px">
        <el-card shadow="hover" class="package-card" :class="getCardClass(item.name)">
          <div class="package-badge" v-if="item.name && item.name.includes('豪华')">推荐</div>
          <div class="package-image">
            <el-image v-if="item.image" :src="item.image" fit="cover" class="card-img" />
            <div v-else class="card-img-placeholder" :class="getPlaceholderClass(item.name)">
              <el-icon :size="48" color="#fff"><Present /></el-icon>
            </div>
          </div>
          <div class="package-info">
            <div class="package-name">{{ item.name }}</div>
            <div class="package-price">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ Number(item.price).toFixed(0) }}</span>
              <span class="price-unit">/{{ item.duration || 28 }}天</span>
            </div>
            <div class="package-duration">
              <el-icon><Timer /></el-icon>
              <span>服务周期：{{ item.duration || 28 }}天</span>
            </div>
            <div class="package-desc">{{ item.description || '暂无描述' }}</div>
            <div v-if="item.services" class="package-services">
              <div class="services-title">包含服务：</div>
              <div class="services-content">{{ item.services }}</div>
            </div>
            <el-button type="primary" class="book-btn" @click="handleBook(item)">
              <el-icon style="margin-right: 4px"><ShoppingCart /></el-icon>立即预约
            </el-button>
          </div>
        </el-card>
      </el-col>
      <el-col v-if="!loading && packageList.length === 0" :span="24">
        <el-empty description="暂无可用套餐" />
      </el-col>
    </el-row>

    <div class="pagination-container">
      <el-pagination v-model:current-page="queryParams.current" v-model:page-size="queryParams.size"
        :page-sizes="[6, 9, 12]" :total="total" layout="total, prev, pager, next"
        @size-change="getList" @current-change="getList" />
    </div>

    <!-- Order Dialog -->
    <el-dialog v-model="orderDialogVisible" title="预约下单" width="560px" destroy-on-close>
      <div class="order-package-info">
        <div class="order-pkg-name">{{ selectedPackage.name }}</div>
        <div class="order-pkg-price">¥{{ Number(selectedPackage.price || 0).toFixed(2) }}</div>
        <div class="order-pkg-duration">服务周期：{{ selectedPackage.duration || 28 }}天</div>
      </div>
      <el-form ref="orderFormRef" :model="orderForm" :rules="orderRules" label-width="100px" style="margin-top: 20px">
        <el-form-item label="预期入住日期" prop="expectedDate">
          <el-date-picker v-model="orderForm.expectedDate" type="date" placeholder="请选择预期入住日期"
            value-format="YYYY-MM-DD" style="width: 100%" :disabled-date="disablePastDate" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="orderForm.remark" type="textarea" :rows="3" placeholder="请输入备注信息（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitOrder">确认预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Timer, Present, ShoppingCart } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import { getPackageList, addOrder } from '@/api/index.js'

const store = useStore()
const user = computed(() => store.getters.user)

const loading = ref(false)
const packageList = ref([])
const total = ref(0)
const queryParams = reactive({ current: 1, size: 9, status: 1 })

const orderDialogVisible = ref(false)
const selectedPackage = ref({})
const submitLoading = ref(false)
const orderFormRef = ref(null)
const orderForm = reactive({ expectedDate: '', remark: '' })

const orderRules = {
  expectedDate: [{ required: true, message: '请选择预期入住日期', trigger: 'change' }]
}

const disablePastDate = (date) => date.getTime() < Date.now() - 86400000

const getCardClass = (name) => {
  if (!name) return ''
  if (name.includes('豪华')) return 'card-premium'
  if (name.includes('标准')) return 'card-standard'
  return ''
}

const getPlaceholderClass = (name) => {
  if (!name) return 'gradient-basic'
  if (name.includes('豪华')) return 'gradient-premium'
  if (name.includes('标准')) return 'gradient-standard'
  return 'gradient-basic'
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getPackageList(queryParams)
    packageList.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleBook = (pkg) => {
  selectedPackage.value = { ...pkg }
  orderForm.expectedDate = ''
  orderForm.remark = ''
  orderDialogVisible.value = true
}

const handleSubmitOrder = async () => {
  await orderFormRef.value.validate()
  submitLoading.value = true
  try {
    await addOrder({
      customerId: user.value.id,
      packageId: selectedPackage.value.id,
      packageName: selectedPackage.value.name,
      totalPrice: selectedPackage.value.price,
      expectedDate: orderForm.expectedDate,
      remark: orderForm.remark
    })
    ElMessage.success('预约成功！订单已提交，请等待审核。')
    orderDialogVisible.value = false
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => getList())
</script>

<style scoped>
.app-container { padding: 20px; }
.section-title { text-align: center; margin-bottom: 32px; }
.section-title h2 { font-size: 28px; font-weight: 700; color: #303133; margin: 0 0 8px 0; }
.section-subtitle { font-size: 14px; color: #909399; margin: 0; }
.pagination-container { display: flex; justify-content: center; margin-top: 16px; }

.package-card { border-radius: 12px; overflow: hidden; position: relative; transition: transform 0.3s, box-shadow 0.3s; }
.package-card:hover { transform: translateY(-6px); box-shadow: 0 12px 32px rgba(0,0,0,0.15); }
.package-card :deep(.el-card__body) { padding: 0; }
.card-premium { border: 2px solid #e6a23c; }
.card-standard { border: 2px solid #409eff; }

.package-badge {
  position: absolute; top: 12px; right: -30px; z-index: 2;
  background: linear-gradient(135deg, #e6a23c, #f5a623); color: #fff;
  padding: 4px 40px; font-size: 12px; font-weight: 600;
  transform: rotate(45deg); box-shadow: 0 2px 8px rgba(230,162,60,0.4);
}

.package-image { width: 100%; height: 200px; overflow: hidden; }
.card-img { width: 100%; height: 200px; }
.card-img-placeholder { width: 100%; height: 200px; display: flex; align-items: center; justify-content: center; }
.gradient-basic { background: linear-gradient(135deg, #a8e6cf 0%, #88d8a8 100%); }
.gradient-standard { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.gradient-premium { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }

.package-info { padding: 20px; }
.package-name { font-size: 20px; font-weight: 700; color: #303133; margin-bottom: 12px; }
.package-price { margin-bottom: 12px; }
.price-symbol { font-size: 16px; color: #e6a23c; font-weight: 600; }
.price-value { font-size: 32px; font-weight: 800; color: #e6a23c; }
.price-unit { font-size: 13px; color: #909399; margin-left: 4px; }
.package-duration { display: flex; align-items: center; gap: 6px; font-size: 14px; color: #606266; margin-bottom: 10px; }
.package-desc { font-size: 13px; color: #909399; line-height: 1.6; margin-bottom: 12px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.package-services { margin-bottom: 16px; }
.services-title { font-size: 13px; font-weight: 600; color: #606266; margin-bottom: 4px; }
.services-content { font-size: 12px; color: #909399; line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.book-btn { width: 100%; border-radius: 8px; height: 42px; font-size: 15px; font-weight: 600; }

.order-package-info {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px; padding: 20px; text-align: center; color: #fff;
}
.order-pkg-name { font-size: 20px; font-weight: 700; margin-bottom: 8px; }
.order-pkg-price { font-size: 28px; font-weight: 800; margin-bottom: 4px; }
.order-pkg-duration { font-size: 14px; opacity: 0.9; }
</style>

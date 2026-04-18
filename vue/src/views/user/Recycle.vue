<template>
  <div class="recycle-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>预约回收</h2>
      <p>选择您要回收的家电，填写信息后提交预约</p>
    </div>

    <!-- 步骤条 -->
    <el-steps :active="step" align-center class="step-bar" finish-status="success">
      <el-step title="选择品类" />
      <el-step title="填写信息" />
      <el-step title="确认提交" />
    </el-steps>

    <!-- 步骤一：选择品类 -->
    <div v-if="step === 0" class="step-content">
      <el-empty v-if="categories.length === 0" description="暂无可回收品类" />
      <div v-else class="category-grid">
        <div
          v-for="c in categories"
          :key="c.id"
          class="category-card"
          :class="{ selected: form.applianceTypeId === c.id }"
          @click="selectCategory(c)"
        >
          <div class="card-icon">{{ c.icon || '♻️' }}</div>
          <div class="card-name">{{ c.name }}</div>
          <div class="card-price">¥{{ c.priceMin }} - ¥{{ c.priceMax }}</div>
          <div class="card-points">积分：{{ c.pointsPerKg }}/kg</div>
        </div>
      </div>
      <div class="step-actions">
        <el-button type="primary" :disabled="!form.applianceTypeId" @click="step = 1">
          下一步
        </el-button>
      </div>
    </div>

    <!-- 步骤二：填写信息 -->
    <div v-if="step === 1" class="step-content">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" class="recycle-form">
        <!-- 已选品类 -->
        <el-form-item label="回收品类">
          <el-tag type="success" size="large">{{ selectedCategory?.name }}</el-tag>
        </el-form-item>

        <!-- 机况评估 -->
        <el-divider content-position="left">机况评估</el-divider>

        <el-form-item label="品牌等级" prop="brandLevel">
          <el-radio-group v-model="form.brandLevel">
            <el-radio value="一线品牌">一线品牌</el-radio>
            <el-radio value="二线品牌">二线品牌</el-radio>
            <el-radio value="杂牌">杂牌</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="使用年限" prop="usageYears">
          <el-radio-group v-model="form.usageYears">
            <el-radio value="1年内">1年内</el-radio>
            <el-radio value="2-3年">2-3年</el-radio>
            <el-radio value="5年以上">5年以上</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="功能状况" prop="functionalStatus">
          <el-radio-group v-model="form.functionalStatus">
            <el-radio value="正常使用">正常使用</el-radio>
            <el-radio value="零件损坏">零件损坏</el-radio>
            <el-radio value="无法开机">无法开机</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="外观成色" prop="appearanceLevel">
          <el-radio-group v-model="form.appearanceLevel">
            <el-radio value="完好">完好</el-radio>
            <el-radio value="轻微划痕">轻微划痕</el-radio>
            <el-radio value="破损">破损</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 预估价格展示 -->
        <el-form-item label="预估回收价">
          <span v-if="estimatedPrice !== null" class="price-text">¥{{ estimatedPrice }}</span>
          <span v-else class="price-hint">请完善机况信息后自动计算</span>
        </el-form-item>

        <!-- 家电描述 -->
        <el-divider content-position="left">家电信息</el-divider>

        <el-form-item label="家电描述" prop="applianceDesc">
          <el-input
            v-model="form.applianceDesc"
            type="textarea"
            :rows="3"
            placeholder="请描述家电的品牌、型号、尺寸等信息"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="家电照片">
          <FileUpload v-model="form.applianceImgs" :limit="3" />
        </el-form-item>

        <!-- 上门信息 -->
        <el-divider content-position="left">上门信息</el-divider>

        <el-form-item label="回收地址" prop="addressId">
          <el-select v-model="form.addressId" placeholder="请选择回收地址" style="width: 100%" @change="onAddressChange">
            <el-option
              v-for="addr in addresses"
              :key="addr.id"
              :label="`${addr.contactName} - ${addr.province}${addr.city}${addr.district}${addr.detailAddress}`"
              :value="addr.id"
            />
          </el-select>
          <el-link type="primary" :underline="false" style="margin-left: 8px" @click="$router.push('/user/addressManage')">
            管理地址
          </el-link>
        </el-form-item>

        <el-form-item label="预约日期" prop="expectDate">
          <el-date-picker
            v-model="form.expectDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            :disabled-date="disablePastDate"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="预约时段" prop="expectTimeStart">
          <el-col :span="11">
            <el-time-select
              v-model="form.expectTimeStart"
              start="08:00"
              step="00:30"
              end="20:00"
              placeholder="开始时间"
              style="width: 100%"
            />
          </el-col>
          <el-col :span="2" style="text-align: center">至</el-col>
          <el-col :span="11">
            <el-time-select
              v-model="form.expectTimeEnd"
              :start="form.expectTimeStart || '08:00'"
              step="00:30"
              end="21:00"
              placeholder="结束时间"
              style="width: 100%"
            />
          </el-col>
        </el-form-item>

        <el-form-item label="紧急程度">
          <el-radio-group v-model="form.urgencyLevel">
            <el-radio :value="1">普通</el-radio>
            <el-radio :value="2">较急</el-radio>
            <el-radio :value="3">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <div class="step-actions">
        <el-button @click="step = 0">上一步</el-button>
        <el-button type="primary" @click="goToConfirm">下一步</el-button>
      </div>
    </div>

    <!-- 步骤三：确认提交 -->
    <div v-if="step === 2" class="step-content">
      <el-card class="confirm-card" shadow="hover">
        <el-descriptions title="订单确认" :column="1" border>
          <el-descriptions-item label="回收品类">{{ selectedCategory?.name }}</el-descriptions-item>
          <el-descriptions-item label="品牌等级">{{ form.brandLevel }}</el-descriptions-item>
          <el-descriptions-item label="使用年限">{{ form.usageYears }}</el-descriptions-item>
          <el-descriptions-item label="功能状况">{{ form.functionalStatus }}</el-descriptions-item>
          <el-descriptions-item label="外观成色">{{ form.appearanceLevel }}</el-descriptions-item>
          <el-descriptions-item label="预估回收价">
            <span class="price-text">¥{{ estimatedPrice || '待计算' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="家电描述">{{ form.applianceDesc || '无' }}</el-descriptions-item>
          <el-descriptions-item label="回收地址">{{ selectedAddressText }}</el-descriptions-item>
          <el-descriptions-item label="预约时间">
            {{ form.expectDate }} {{ form.expectTimeStart }} - {{ form.expectTimeEnd }}
          </el-descriptions-item>
          <el-descriptions-item label="紧急程度">
            {{ urgencyText }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <div class="step-actions">
        <el-button @click="step = 1">上一步</el-button>
        <el-button type="primary" :loading="submitting" @click="submitOrder">提交预约</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import FileUpload from '@/components/FileUpload.vue'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')

const step = ref(0)
const formRef = ref(null)
const categories = ref([])
const addresses = ref([])
const estimatedPrice = ref(null)
const submitting = ref(false)

const form = ref({
  applianceTypeId: null,
  applianceTypeName: '',
  brandLevel: '',
  usageYears: '',
  functionalStatus: '',
  appearanceLevel: '',
  applianceDesc: '',
  applianceImgs: '',
  addressId: null,
  expectDate: '',
  expectTimeStart: '',
  expectTimeEnd: '',
  urgencyLevel: 1
})

const rules = {
  brandLevel: [{ required: true, message: '请选择品牌等级', trigger: 'change' }],
  usageYears: [{ required: true, message: '请选择使用年限', trigger: 'change' }],
  functionalStatus: [{ required: true, message: '请选择功能状况', trigger: 'change' }],
  appearanceLevel: [{ required: true, message: '请选择外观成色', trigger: 'change' }],
  addressId: [{ required: true, message: '请选择回收地址', trigger: 'change' }],
  expectDate: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
  expectTimeStart: [{ required: true, message: '请选择开始时间', trigger: 'change' }]
}

const selectedCategory = computed(() => {
  return categories.value.find(c => c.id === form.value.applianceTypeId) || null
})

const selectedAddress = computed(() => {
  return addresses.value.find(a => a.id === form.value.addressId) || null
})

const selectedAddressText = computed(() => {
  const addr = selectedAddress.value
  if (!addr) return '未选择'
  return `${addr.contactName} ${addr.contactPhone} - ${addr.province}${addr.city}${addr.district}${addr.detailAddress}`
})

const urgencyText = computed(() => {
  const map = { 1: '普通', 2: '较急', 3: '紧急' }
  return map[form.value.urgencyLevel] || '普通'
})

const disablePastDate = (date) => {
  return date.getTime() < Date.now() - 86400000
}

const selectCategory = (c) => {
  form.value.applianceTypeId = c.id
  form.value.applianceTypeName = c.name
}

const onAddressChange = () => {
  const addr = selectedAddress.value
  if (addr) {
    form.value.contactName = addr.contactName
    form.value.contactPhone = addr.contactPhone
  }
}

// 监听机况变化，自动计算预估价格
watch(
  () => [form.value.brandLevel, form.value.usageYears, form.value.functionalStatus, form.value.appearanceLevel],
  async ([brandLevel, usageYears, functionalStatus, appearanceLevel]) => {
    if (brandLevel && usageYears && functionalStatus && appearanceLevel && form.value.applianceTypeId) {
      try {
        const res = await request.get('/applianceType/calculatePrice', {
          params: {
            typeId: form.value.applianceTypeId,
            brandLevel,
            usageYears,
            functionalStatus,
            appearanceLevel
          }
        })
        if (res.code === '200') {
          estimatedPrice.value = res.data
        }
      } catch {
        // ignore
      }
    } else {
      estimatedPrice.value = null
    }
  }
)

const loadCategories = async () => {
  try {
    const res = await request.get('/applianceType/selectEnabled')
    categories.value = res?.data || []
  } catch {
    ElMessage.error('加载品类失败')
  }
}

const loadAddresses = async () => {
  try {
    const res = await request.get(`/userAddress/selectByUser/${user.id}`)
    addresses.value = res?.data || []
  } catch {
    ElMessage.error('加载地址失败')
  }
}

const goToConfirm = async () => {
  try {
    await formRef.value.validate()
    step.value = 2
  } catch {
    ElMessage.warning('请完善必填信息')
  }
}

const submitOrder = async () => {
  submitting.value = true
  try {
    const addr = selectedAddress.value
    const orderData = {
      userId: user.id,
      applianceTypeId: form.value.applianceTypeId,
      applianceTypeName: form.value.applianceTypeName,
      brandLevel: form.value.brandLevel,
      usageYears: form.value.usageYears,
      functionalStatus: form.value.functionalStatus,
      appearanceLevel: form.value.appearanceLevel,
      applianceDesc: form.value.applianceDesc,
      applianceImgs: form.value.applianceImgs,
      addressId: form.value.addressId,
      addressDetail: addr ? `${addr.province}${addr.city}${addr.district}${addr.detailAddress}` : '',
      addressLat: addr?.latitude,
      addressLng: addr?.longitude,
      buildingType: addr?.buildingType,
      floor: addr?.floor,
      contactName: addr?.contactName,
      contactPhone: addr?.contactPhone,
      estimatedWeight: selectedCategory.value?.weightEstimate,
      expectDate: form.value.expectDate,
      expectTimeStart: form.value.expectTimeStart + ':00',
      expectTimeEnd: form.value.expectTimeEnd + ':00',
      urgencyLevel: form.value.urgencyLevel,
      priceEstimate: estimatedPrice.value
    }
    const res = await request.post('/recycleOrder/create', orderData)
    if (res.code === '200') {
      ElMessage.success('预约成功！')
      router.push('/user/orderList')
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch {
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await Promise.allSettled([loadCategories(), loadAddresses()])
})
</script>

<style scoped>
.recycle-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 4px;
  font-size: 22px;
  color: #1f2937;
}

.page-header p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.step-bar {
  margin-bottom: 28px;
}

.step-content {
  margin-top: 16px;
}

/* 品类选择网格 */
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 14px;
}

.category-card {
  background: #fff;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 18px 14px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
}

.category-card:hover {
  border-color: #a7f3d0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.category-card.selected {
  border-color: #10b981;
  background: #ecfdf5;
}

.card-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.card-name {
  font-weight: 600;
  font-size: 15px;
  color: #1f2937;
}

.card-price {
  margin-top: 6px;
  color: #059669;
  font-size: 13px;
}

.card-points {
  color: #6b7280;
  font-size: 12px;
  margin-top: 2px;
}

/* 表单 */
.recycle-form {
  max-width: 680px;
}

.price-text {
  font-size: 20px;
  font-weight: 700;
  color: #059669;
}

.price-hint {
  color: #9ca3af;
  font-size: 13px;
}

/* 确认卡片 */
.confirm-card {
  margin-bottom: 20px;
}

/* 操作按钮 */
.step-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #f3f4f6;
}

@media (max-width: 768px) {
  .recycle-page {
    padding: 16px;
  }

  .category-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .recycle-form {
    max-width: 100%;
  }
}
</style>

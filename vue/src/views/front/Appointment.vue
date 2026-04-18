<template>
  <div class="appointment-container">
    <el-card shadow="hover" class="appointment-card">
      <template #header>
        <div class="card-header">
          <h2>家电回收预约</h2>
          <el-text type="info">请填写回收信息，我们将尽快安排上门服务</el-text>
        </div>
      </template>

      <el-form :model="form" ref="formRef" :rules="rules" label-width="120px" class="appointment-form">
        <!-- 家电类型选择 -->
        <el-form-item label="家电类型" prop="applianceTypeId">
          <el-select v-model="form.applianceTypeId" placeholder="请选择家电类型" style="width: 100%">
            <el-option
                v-for="type in applianceTypes"
                :key="type.id"
                :label="type.name"
                :value="type.id"
            >
              <span style="float: left">{{ type.name }}</span>
              <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px;">
                {{ type.unit }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 家电描述 -->
        <el-form-item label="家电描述" prop="applianceDesc">
          <el-input
              v-model="form.applianceDesc"
              type="textarea"
              :rows="3"
              placeholder="请描述家电品牌、型号、使用年限、外观状况等信息"
              maxlength="500"
              show-word-limit
          />
        </el-form-item>

        <!-- 上传图片 -->
        <el-form-item label="家电照片">
          <el-upload
              v-model:file-list="fileList"
              action="/api/upload/image"
              list-type="picture-card"
              :limit="6"
              :on-exceed="handleExceed"
              :on-success="handleUploadSuccess"
              :on-remove="handleRemove"
              accept="image/*"
          >
            <el-icon><Camera /></el-icon>
            <template #tip>
              <div class="el-upload__tip">
                最多上传6张照片，支持jpg、png格式，单张不超过5MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- 选择地址 -->
        <el-form-item label="回收地址" prop="addressId">
          <el-radio-group v-model="form.addressId" class="address-group">
            <el-radio
                v-for="address in addresses"
                :key="address.id"
                :label="address.id"
                border
                class="address-item"
            >
              <div class="address-info">
                <div class="address-text">
                  {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}
                </div>
                <div class="contact-info">
                  {{ address.contactName }} {{ address.contactPhone }}
                </div>
              </div>
            </el-radio>
          </el-radio-group>
          <el-button type="primary" link @click="goToAddAddress" style="margin-top: 10px">
            <el-icon><Plus /></el-icon> 添加新地址
          </el-button>
        </el-form-item>

        <!-- 期望上门时间 -->
        <el-form-item label="期望时间" prop="expectTime">
          <el-date-picker
              v-model="form.expectTime"
              type="datetime"
              placeholder="选择期望上门时间"
              :disabled-date="disabledDate"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
          />
        </el-form-item>

        <!-- 备注 -->
        <el-form-item label="备注">
          <el-input
              v-model="form.remark"
              type="textarea"
              :rows="2"
              placeholder="其他特殊要求或说明"
              maxlength="200"
              show-word-limit
          />
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" @click="submitOrder" :loading="submitting" size="large" style="width: 200px">
            提交预约申请
          </el-button>
          <el-button @click="resetForm" size="large">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预约须知 -->
    <el-card shadow="hover" class="notice-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <el-icon><Warning /></el-icon>
          <span>预约须知</span>
        </div>
      </template>
      <ul class="notice-list">
        <li>请如实填写家电信息，以便我们准确评估</li>
        <li>上传清晰照片有助于提高评估准确性</li>
        <li>回收员将在接单后1小时内联系您确认详情</li>
        <li>上门服务前会提前30分钟通知您</li>
        <li>价格以现场评估为准，可协商确定</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Camera, Plus, Warning } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const applianceTypes = ref([])
const addresses = ref([])
const fileList = ref([])

const form = ref({
  applianceTypeId: null,
  applianceDesc: '',
  addressId: null,
  expectTime: '',
  remark: '',
  applianceImg: '[]'
})

const rules = {
  applianceTypeId: [{ required: true, message: '请选择家电类型', trigger: 'change' }],
  applianceDesc: [{ required: true, message: '请描述家电信息', trigger: 'blur' }],
  addressId: [{ required: true, message: '请选择回收地址', trigger: 'change' }],
  expectTime: [{ required: true, message: '请选择期望时间', trigger: 'change' }]
}

// 加载家电类型
const loadApplianceTypes = async () => {
  const res = await request.get('/recycle/applianceTypes')
  if (res.code === '200') {
    applianceTypes.value = res.data
  }
}

// 加载用户地址
const loadAddresses = async () => {
  const res = await request.get('/recycle/addresses')
  if (res.code === '200') {
    addresses.value = res.data
    if (addresses.value.length === 0) {
      ElMessage.warning('请先添加回收地址')
      setTimeout(() => router.push('/front/address'), 1500)
    }
  }
}

// 日期禁用（不能选择过去时间）
const disabledDate = (date) => {
  return date.getTime() < Date.now() - 86400000
}

// 文件上传处理
const handleUploadSuccess = (response, file) => {
  const imgList = JSON.parse(form.value.applianceImg)
  imgList.push(response.data) // 假设返回{code:200, data: '图片url'}
  form.value.applianceImg = JSON.stringify(imgList)
}

const handleRemove = (file) => {
  const imgList = JSON.parse(form.value.applianceImg)
  const index = imgList.findIndex(img => img === file.url)
  if (index > -1) {
    imgList.splice(index, 1)
    form.value.applianceImg = JSON.stringify(imgList)
  }
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传6张照片')
}

// 提交订单
const submitOrder = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        submitting.value = true
        const res = await request.post('/recycle/order', form.value)
        if (res.code === '200') {
          ElMessage.success('预约提交成功！')
          setTimeout(() => router.push('/front/orders'), 1500)
        }
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

const resetForm = () => {
  formRef.value.resetFields()
  fileList.value = []
  form.value.applianceImg = '[]'
}

const goToAddAddress = () => {
  router.push('/front/address')
}

onMounted(() => {
  loadApplianceTypes()
  loadAddresses()
})
</script>

<style scoped>
.appointment-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.appointment-form {
  max-width: 800px;
  margin: 0 auto;
}

.address-group {
  width: 100%;
}

.address-item {
  display: block;
  margin-bottom: 10px;
  width: 100%;
}

.address-info {
  padding: 10px 0;
}

.address-text {
  font-weight: bold;
  margin-bottom: 5px;
}

.contact-info {
  font-size: 13px;
  color: #909399;
}

.notice-list {
  margin: 0;
  padding-left: 20px;
  line-height: 2;
  color: #606266;
}

.notice-list li {
  margin-bottom: 8px;
}
</style>
<template>
  <div class="status-query-page">
    <div class="page-header">
      <el-icon @click="$router.back()"><ArrowLeft /></el-icon>
      <h2>审核状态查询</h2>
      <span></span>
    </div>

    <div class="query-wrapper">
      <div class="query-form-box" v-if="!result">
        <div class="query-tips">
          <el-icon :size="48" color="#52c41a"><Search /></el-icon>
          <p>请输入申请时填写的手机号和身份证号查询审核状态</p>
        </div>

        <el-form
            :model="queryForm"
            :rules="queryRules"
            ref="queryRef"
            label-position="top"
            class="query-form"
        >
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="queryForm.phone" placeholder="请输入申请时的手机号" maxlength="11" />
          </el-form-item>

          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="queryForm.idCard" placeholder="请输入申请时的身份证号" maxlength="18" />
          </el-form-item>

          <el-button
              type="primary"
              size="large"
              class="query-button"
              @click="queryStatus"
              :loading="loading"
          >
            查询状态
          </el-button>
        </el-form>
      </div>

      <div class="result-box" v-else>
        <div class="status-icon" :class="auditStatusClass">
          <el-icon v-if="result.auditStatus === 0" :size="64" color="#faad14"><Clock /></el-icon>
          <el-icon v-else-if="result.auditStatus === 1" :size="64" color="#52c41a"><CircleCheck /></el-icon>
          <el-icon v-else :size="64" color="#f5222d"><CircleClose /></el-icon>
        </div>

        <h3 class="status-title">{{ statusText }}</h3>
        <p class="status-desc">{{ statusDesc }}</p>

        <div class="detail-box pending-box" v-if="result.auditStatus === 0">
          <div class="info-row">
            <span class="row-label">申请时间：</span>
            <span class="row-value">{{ result.createTime }}</span>
          </div>
          <div class="info-row">
            <span class="row-label">预计审核：</span>
            <span class="row-value">1-3个工作日</span>
          </div>
          <div class="progress-bar">
            <div class="step active">
              <div class="dot"></div>
              <span>提交申请</span>
            </div>
            <div class="line"></div>
            <div class="step active">
              <div class="dot"></div>
              <span>审核中</span>
            </div>
            <div class="line dashed"></div>
            <div class="step">
              <div class="dot"></div>
              <span>审核完成</span>
            </div>
          </div>
        </div>

        <div class="detail-box approved-box" v-if="result.auditStatus === 1">
          <div class="info-row">
            <span class="row-label">审核通过时间：</span>
            <span class="row-value">{{ result.createTime }}</span>
          </div>
          <div class="success-tip">
            <el-icon :size="20" color="#52c41a"><SuccessFilled /></el-icon>
            <span>您的账号已启用，现在可以登录接单了</span>
          </div>
          <el-button type="primary" class="action-btn" @click="goToLogin">
            立即登录
          </el-button>
        </div>

        <div class="detail-box rejected-box" v-if="result.auditStatus === 2">
          <div class="info-row">
            <span class="row-label">审核时间：</span>
            <span class="row-value">{{ result.createTime }}</span>
          </div>
          <div class="reject-info">
            <p class="reject-title">拒绝原因：</p>
            <p class="reject-text">{{ result.rejectReason || '信息不符合要求' }}</p>
          </div>
          <div class="resubmit-hint">
            <p>您可以修改信息后重新提交申请</p>
          </div>
          <el-button type="primary" class="action-btn" @click="goToResubmit">
            重新提交申请
          </el-button>
        </div>

        <el-button class="back-link" link @click="resetQuery">
          查询其他申请
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Search, Clock, CircleCheck, CircleClose, SuccessFilled } from '@element-plus/icons-vue'
import * as collectorApi from '@/api/collector.js'

const router = useRouter()
const loading = ref(false)
const result = ref(null)
const queryRef = ref(null)

const queryForm = reactive({
  phone: '',
  idCard: ''
})

const queryRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }]
}

const auditStatusClass = computed(() => {
  const map = { 0: 'pending', 1: 'approved', 2: 'rejected' }
  return map[result.value?.auditStatus] || ''
})

const statusText = computed(() => {
  const map = { 0: '审核中', 1: '审核通过', 2: '审核未通过' }
  return map[result.value?.auditStatus] || ''
})

const statusDesc = computed(() => {
  const map = {
    0: '您的申请正在审核中，请耐心等待',
    1: '恭喜！您的回收员申请已通过审核',
    2: '很遗憾，您的申请未通过审核'
  }
  return map[result.value?.auditStatus] || ''
})

const queryStatus = async () => {
  await queryRef.value.validate()

  loading.value = true
  try {
    const res = await collectorApi.queryStatus(queryForm.phone, queryForm.idCard)
    if (res.code === '200') {
      result.value = res.data
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '查询失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  result.value = null
  queryForm.phone = ''
  queryForm.idCard = ''
}

const goToLogin = () => {
  router.push('/login')
}

const goToResubmit = () => {
  const query = {
    resubmit: 'true',
    id: result.value.id,
    reason: result.value.rejectReason,
    data: encodeURIComponent(JSON.stringify({
      username: result.value.username,
      name: result.value.name,
      idCard: queryForm.idCard,
      phone: result.value.phone,
      serviceArea: result.value.serviceArea
    }))
  }
  router.push({ path: '/collector-register', query })
}
</script>

<style>
/* 页面容器 */
.status-query-page {
  min-height: 100vh;
  background: #f8fafc;
}

/* 头部 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.page-header .el-icon {
  font-size: 24px;
  cursor: pointer;
  color: #595959;
}

/* 查询容器 */
.query-wrapper {
  padding: 24px;
  max-width: 600px;
  margin: 0 auto;
}

/* 查询提示 */
.query-tips {
  text-align: center;
  margin-bottom: 32px;
  padding: 24px;
}

.query-tips p {
  margin-top: 16px;
  color: #8c8c8c;
  font-size: 14px;
  line-height: 1.6;
}

/* 表单样式 */
.query-form {
  margin-top: 10px;
}

/* 标签样式 */
.query-form .el-form-item__label {
  font-weight: 500 !important;
  color: #262626 !important;
  padding-bottom: 8px !important;
  font-size: 14px !important;
}

/* 输入框样式 */
.query-form .el-input__inner {
  border-radius: 8px !important;
  height: 44px !important;
  font-size: 14px !important;
  border-color: #d9d9d9;
}

/* 查询按钮 */
.query-button {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  border: none;
  margin-top: 16px;
  font-size: 16px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.2);
}

/* 结果卡片 */
.result-box {
  background: white;
  border-radius: 16px;
  padding: 32px 24px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-top: 20px;
}

.status-icon {
  margin-bottom: 16px;
}

.status-icon.pending {
  animation: pulse 2s infinite;
}

.status-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #262626;
}

.status-desc {
  color: #8c8c8c;
  margin: 0 0 24px 0;
  font-size: 14px;
}

/* 详情盒子 */
.detail-box {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  text-align: left;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
}

.row-label {
  color: #8c8c8c;
}

.row-value {
  color: #595959;
  font-weight: 500;
}

/* 进度条 */
.progress-bar {
  display: flex;
  align-items: center;
  margin-top: 20px;
  padding: 0 8px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.step .dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #bfbfbf;
}

.step span {
  font-size: 12px;
  color: #8c8c8c;
  white-space: nowrap;
}

.step.active .dot {
  background: #52c41a;
}

.step.active span {
  color: #389e0d;
  font-weight: 500;
}

.line {
  flex: 1;
  height: 2px;
  background: #52c41a;
  margin: 0 8px;
  margin-bottom: 20px;
  min-width: 30px;
}

.line.dashed {
  background: repeating-linear-gradient(
      to right,
      #bfbfbf 0,
      #bfbfbf 4px,
      transparent 4px,
      transparent 8px
  );
}

/* 通过状态 */
.approved-box .success-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #389e0d;
  font-size: 14px;
  margin: 16px 0;
  padding: 12px;
  background: #f6ffed;
  border-radius: 8px;
}

/* 拒绝状态 */
.rejected-box .reject-info {
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 8px;
  padding: 12px;
  margin: 12px 0;
}

.reject-title {
  font-size: 13px;
  color: #a8071a;
  margin: 0 0 4px 0;
  font-weight: 500;
}

.reject-text {
  font-size: 14px;
  color: #f5222d;
  margin: 0;
  line-height: 1.5;
}

.resubmit-hint {
  text-align: center;
  color: #8c8c8c;
  font-size: 13px;
  margin: 16px 0;
}

/* 操作按钮 */
.action-btn {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.2);
}

/* 返回链接 */
.back-link {
  color: #8c8c8c;
  font-size: 14px;
}

.back-link:hover {
  color: #1890ff;
}

/* 动画 */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>
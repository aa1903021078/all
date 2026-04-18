<template>
  <div class="collector-register-page">
    <div class="page-header">
      <el-icon @click="$router.back()"><ArrowLeft /></el-icon>
      <h2>{{ isResubmit ? '重新提交申请' : '回收员申请' }}</h2>
      <span></span>
    </div>

    <div class="form-wrapper">
      <div class="info-box" v-if="!isResubmit">
        <el-icon :size="32" color="#10B981"><InfoFilled /></el-icon>
        <p>提交申请后，工作人员将在1-3个工作日内审核，审核通过后即可接单。请确保信息真实有效，否则将被拒绝。</p>
      </div>

      <div class="reject-box" v-if="isResubmit && rejectReason">
        <el-icon :size="32" color="#EF4444"><WarningFilled /></el-icon>
        <div class="reject-info">
          <p class="reject-title">审核未通过</p>
          <p class="reject-reason-text">原因：{{ rejectReason }}</p>
          <p class="reject-hint">请修改信息后重新提交</p>
        </div>
      </div>

      <el-form
          :model="form"
          :rules="rules"
          ref="formRef"
          label-position="top"
          class="register-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名，用于登录系统" maxlength="20" />
        </el-form-item>

        <el-form-item label="真实姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入真实姓名，需与身份证一致" maxlength="10" />
        </el-form-item>

        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入18位身份证号" maxlength="18" />
        </el-form-item>

        <el-form-item label="身份证照片" prop="idCardImages" class="upload-section">
          <p class="upload-hint">请上传身份证正反面照片，确保清晰可辨</p>
          <div class="upload-boxes">
            <el-upload
                class="idcard-upload"
                :http-request="uploadFront"
                :show-file-list="false"
                accept="image/*"
            >
              <div v-if="form.idCardFront" class="image-preview">
                <img :src="form.idCardFront" />
                <span class="preview-mask">点击重新上传</span>
              </div>
              <div v-else class="upload-trigger">
                <el-icon><Plus /></el-icon>
                <span class="trigger-text">身份证正面</span>
                <span class="trigger-sub">人像面</span>
              </div>
            </el-upload>

            <el-upload
                class="idcard-upload"
                :http-request="uploadBack"
                :show-file-list="false"
                accept="image/*"
            >
              <div v-if="form.idCardBack" class="image-preview">
                <img :src="form.idCardBack" />
                <span class="preview-mask">点击重新上传</span>
              </div>
              <div v-else class="upload-trigger">
                <el-icon><Plus /></el-icon>
                <span class="trigger-text">身份证反面</span>
                <span class="trigger-sub">国徽面</span>
              </div>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入11位手机号" maxlength="11" />
        </el-form-item>

        <el-form-item label="服务区域" prop="serviceArea">
          <el-input
              v-model="form.serviceArea"
              type="textarea"
              :rows="3"
              placeholder="请输入您熟悉的服务区域，如：朝阳区、海淀区等。建议填写您常驻的区域，方便系统派单"
          />
        </el-form-item>

        <el-form-item label="登录密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请设置6-20位登录密码" show-password maxlength="20" />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码确认" show-password maxlength="20" />
        </el-form-item>

        <el-form-item prop="agreement" class="agreement-section">
          <el-checkbox v-model="form.agreement">
            <span class="agreement-text">
              我已阅读并同意
              <el-link type="primary" @click.stop="showAgreement">《回收员服务协议》</el-link>
              和
              <el-link type="primary" @click.stop="showPrivacy">《隐私政策》</el-link>
            </span>
          </el-checkbox>
        </el-form-item>

        <el-button
            type="primary"
            size="large"
            class="submit-button"
            @click="submit"
            :loading="loading"
            :disabled="!form.agreement"
        >
          {{ isResubmit ? '重新提交申请' : '提交申请' }}
        </el-button>

        <div class="status-link-box" v-if="!isResubmit">
          <a href="javascript:void(0)" class="link-text" @click.prevent="goToStatusQuery">
            已有申请？查询审核状态
          </a>
        </div>
      </el-form>
    </div>

    <el-dialog
        v-model="agreementVisible"
        title="回收员服务协议"
        width="90%"
        class="custom-dialog"
    >
      <div class="dialog-content">
        <p>1. 回收员需遵守平台规则，诚信经营。</p>
        <p>2. 回收员需保护用户隐私，不得泄露用户信息。</p>
        <p>3. 回收员需按约定时间上门回收，如有特殊情况需提前沟通。</p>
        <p>4. 回收价格需公平合理，不得欺诈用户。</p>
        <p>5. 平台有权对违规回收员进行处罚或清退。</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, InfoFilled, Plus, WarningFilled } from '@element-plus/icons-vue'
import  * as collectorApi from '@/api/collector.js'
import fileApi from '@/api/file.js'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const formRef = ref(null)
const agreementVisible = ref(false)

const isResubmit = ref(false)
const rejectReason = ref('')
const applicationId = ref(null)

const form = reactive({
  username: '',
  name: '',
  idCard: '',
  idCardFront: '',
  idCardBack: '',
  phone: '',
  serviceArea: '',
  password: '',
  confirmPassword: '',
  agreement: false
})

const validateIdCard = (rule, value, callback) => {
  const reg = /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/
  if (!value) {
    callback(new Error('请输入身份证号'))
  } else if (!reg.test(value)) {
    callback(new Error('身份证号格式不正确'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  const reg = /^1[3-9]\d{9}$/
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!reg.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const validateIdCardImages = (rule, value, callback) => {
  if (!form.idCardFront || !form.idCardBack) {
    callback(new Error('请上传身份证正反面照片'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20位', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  idCard: [{ required: true, validator: validateIdCard, trigger: 'blur' }],
  idCardImages: [{ required: true, validator: validateIdCardImages, trigger: 'change' }],
  phone: [{ required: true, validator: validatePhone, trigger: 'blur' }],
  serviceArea: [{ required: true, message: '请输入服务区域', trigger: 'blur' }],
  password: [{ required: true, validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
  agreement: [{ required: true, message: '请同意服务协议', trigger: 'change' }]
}

const beforeIdCardUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('请上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

// 在 script 中添加这两个包装函数
const uploadFront = (options) => customUpload(options, 'front')
const uploadBack = (options) => customUpload(options, 'back')

// 自定义上传方法
const customUpload = async (options, type) => {
  const { file, onSuccess, onError } = options

  try {
    const res = await fileApi.upload(file)

    if (res.code === '200') {
      // 调用原有的成功处理函数
      handleIdCardSuccess(res, type)
      onSuccess(res)  // 通知 el-upload 成功
    } else {
      ElMessage.error(res.msg || '上传失败')
      onError(new Error(res.msg))
    }
  } catch (error) {
    ElMessage.error('上传失败：' + (error.response?.data?.msg || error.message))
    onError(error)
  }
}

const handleIdCardSuccess = (res, type) => {
  if (res.code === '200') {
    if (type === 'front') {
      form.idCardFront = res.data
    } else {
      form.idCardBack = res.data
    }
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

const showAgreement = () => {
  agreementVisible.value = true
}

const showPrivacy = () => {
  ElMessage.info('隐私政策内容...')
}

const submit = async () => {
  await formRef.value.validate()

  if (form.password !== form.confirmPassword) {
    ElMessage.error('两次输入密码不一致')
    return
  }

  loading.value = true
  try {
    const params = {
      username: form.username,
      password: form.password,
      confirmPassword: form.confirmPassword,
      name: form.name,
      idCard: form.idCard,
      idCardFront: form.idCardFront,
      idCardBack: form.idCardBack,
      phone: form.phone,
      serviceArea: form.serviceArea
    }

    let res
    if (isResubmit.value && applicationId.value) {
      res = await collectorApi.resubmit(applicationId.value, params)
    } else {
      res = await collectorApi.register(params)
    }

    if (res.code === '200') {
      ElMessage.success(isResubmit.value ? '重新提交成功，请等待审核' : '申请提交成功，请等待审核')
      router.push('/register-success')
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '提交失败')
  } finally {
    loading.value = false
  }
}

const goToStatusQuery = () => {
  router.push('/collector-status-query').catch(err => {
    console.error('路由跳转失败:', err)
    window.location.href = '/#/collector-status-query'
  })
}

onMounted(() => {
  if (route.query.resubmit === 'true' && route.query.id) {
    isResubmit.value = true
    applicationId.value = parseInt(route.query.id)
    rejectReason.value = route.query.reason || ''

    if (route.query.data) {
      try {
        const data = JSON.parse(decodeURIComponent(route.query.data))
        Object.assign(form, {
          username: data.username || '',
          name: data.name || '',
          idCard: data.idCard || '',
          phone: data.phone || '',
          serviceArea: data.serviceArea || ''
        })
      } catch (e) {
        console.error('数据解析失败')
      }
    }
  }
})
</script>

<style>
/* 页面容器 */
.collector-register-page {
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
  position: sticky;
  top: 0;
  z-index: 100;
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

/* 表单容器 */
.form-wrapper {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

/* 信息提示框 */
.info-box {
  background: #f6ffed;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  border: 1px solid rgba(82, 196, 26, 0.3);
}

.info-box p {
  margin: 0;
  font-size: 14px;
  color: #389e0d;
  line-height: 1.6;
  flex: 1;
}

/* 拒绝提示框 */
.reject-box {
  background: #fff2f0;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  border: 1px solid #ffccc7;
}

.reject-info {
  flex: 1;
}

.reject-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #f5222d;
}

.reject-reason-text {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #a8071a;
  line-height: 1.5;
}

.reject-hint {
  margin: 0;
  font-size: 13px;
  color: #cf1322;
}

/* 表单样式 */
.register-form {
  margin-top: 10px;
}

/* 表单项标签 */
.register-form .el-form-item__label {
  font-weight: 500 !important;
  color: #262626 !important;
  padding-bottom: 8px !important;
  font-size: 14px !important;
}

/* 输入框样式 */
.register-form .el-input__wrapper {
  border-radius: 8px !important;
  box-shadow: 0 0 0 1px #d9d9d9 inset !important;
  padding: 0 12px !important;
}

.register-form .el-input__inner {
  height: 44px !important;
  font-size: 14px !important;
}

/* 文本域样式 */
.register-form .el-textarea__inner {
  border-radius: 8px !important;
  font-size: 14px !important;
  padding: 12px;
  border-color: #d9d9d9;
}

/* 上传区域 */
.upload-section {
  margin-bottom: 20px;
}

.upload-hint {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 12px;
  margin-top: 0;
}

.upload-boxes {
  display: flex;
  gap: 16px;
}

/* 上传组件样式 */
.idcard-upload .el-upload {
  width: 150px !important;
  height: 100px !important;
  border: 2px dashed #d9d9d9 !important;
  border-radius: 8px !important;
  cursor: pointer !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  background: white;
  transition: all 0.3s ease;
}

.idcard-upload .el-upload:hover {
  border-color: #52c41a !important;
}

.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c8c8c;
  text-align: center;
}

.upload-trigger .el-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.trigger-text {
  font-size: 13px;
  display: block;
}

.trigger-sub {
  font-size: 11px;
  color: #bfbfbf;
  margin-top: 2px;
  display: block;
}

/* 图片预览 */
.image-preview {
  position: relative;
  width: 150px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-mask {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  text-align: center;
  padding: 4px;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-preview:hover .preview-mask {
  opacity: 1;
}

/* 协议区域 */
.agreement-section {
  margin-top: 8px;
}

.agreement-section .el-checkbox__label {
  white-space: normal !important;
  line-height: 1.5 !important;
  font-size: 13px !important;
  color: #8c8c8c !important;
}

.agreement-text {
  font-size: 13px;
  color: #8c8c8c;
}

/* 提交按钮 */
.submit-button {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  border: none;
  margin-top: 24px;
  font-size: 16px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.2);
}

.submit-button.is-disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

/* 状态查询链接 */
.status-link-box {
  text-align: center;
  margin-top: 16px;
  padding: 8px;
}

.link-text {
  font-size: 14px;
  color: #1890ff;
  cursor: pointer;
  text-decoration: underline;
}

.link-text:hover {
  color: #389e0d;
}

/* 弹窗样式 */
.custom-dialog .el-dialog__header {
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
  margin-right: 0;
}

.custom-dialog .el-dialog__title {
  font-size: 16px;
  color: #262626;
}

.custom-dialog .el-dialog__body {
  padding: 20px;
}

.custom-dialog .el-dialog__footer {
  padding: 12px 20px;
  border-top: 1px solid #e8e8e8;
}

.dialog-content {
  max-height: 60vh;
  overflow-y: auto;
  padding: 0 4px;
}

.dialog-content p {
  margin: 12px 0;
  line-height: 1.8;
  color: #595959;
  font-size: 14px;
}
</style>
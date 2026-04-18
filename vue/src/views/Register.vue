<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <el-icon class="logo" @click="$router.push('/login')"><Refresh /></el-icon>
        <h1>用户注册</h1>
        <p>创建账户，开始您的环保之旅</p>
      </div>

      <el-steps :active="step" finish-status="success" class="steps">
        <el-step title="填写信息" />
        <el-step title="验证手机" />
        <el-step title="注册成功" />
      </el-steps>

      <!-- 步骤1：填写信息 -->
      <el-form
          v-if="step === 0"
          ref="formRef1"
          :model="form"
          :rules="rules1"
          class="register-form"
      >
        <el-form-item prop="username">
          <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
              clearable
          />
        </el-form-item>

        <el-form-item prop="realName">
          <el-input
              v-model="form.realName"
              placeholder="请输入真实姓名"
              size="large"
              :prefix-icon="UserFilled"
              clearable
          />
        </el-form-item>

        <el-form-item prop="phone">
          <el-input
              v-model="form.phone"
              placeholder="请输入手机号"
              size="large"
              :prefix-icon="Phone"
              clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码（6-20位）"
              size="large"
              :prefix-icon="Lock"
              show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请确认密码"
              size="large"
              :prefix-icon="Lock"
              show-password
          />
        </el-form-item>

        <el-form-item prop="agreement">
          <el-checkbox v-model="form.agreement">
            我已阅读并同意
            <el-link type="primary">服务条款</el-link>
            和
            <el-link type="primary">隐私政策</el-link>
          </el-checkbox>
        </el-form-item>

        <el-button
            type="primary"
            size="large"
            class="submit-btn"
            @click="handleStep1"
        >
          下一步
        </el-button>

        <div class="login-link">
          已有账号？<el-link type="primary" @click="$router.push('/login')">立即登录</el-link>
        </div>
      </el-form>

      <!-- 步骤2：验证手机 -->
      <el-form
          v-if="step === 1"
          ref="formRef2"
          :model="form"
          :rules="rules2"
          class="register-form"
      >
        <div class="phone-verify">
          <p class="verify-tip">验证码已发送至 {{ maskedPhone }}</p>

          <el-form-item prop="verifyCode">
            <el-input
                v-model="form.verifyCode"
                placeholder="请输入6位验证码"
                size="large"
                maxlength="6"
            >
              <template #append>
                <el-button
                    :disabled="countdown > 0"
                    @click="sendCode"
                >
                  {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
        </div>

        <div class="form-actions">
          <el-button size="large" @click="step = 0">上一步</el-button>
          <el-button
              type="primary"
              size="large"
              @click="handleStep2"
          >
            完成注册
          </el-button>
        </div>
      </el-form>

      <!-- 步骤3：注册成功 -->
      <div v-if="step === 2" class="success-page">
        <el-result
            icon="success"
            title="注册成功"
            sub-title="欢迎加入绿回收，让我们一起为环保贡献力量"
        >
          <template #extra>
            <el-button type="primary" size="large" @click="$router.push('/login')">
              去登录
            </el-button>
          </template>
        </el-result>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, UserFilled, Phone, Lock, Refresh } from '@element-plus/icons-vue'
import { register } from '@/api'

const router = useRouter()
const step = ref(0)
const countdown = ref(0)
const formRef1 = ref(null)
const formRef2 = ref(null)

const form = reactive({
  username: '',
  realName: '',
  phone: '',
  password: '',
  confirmPassword: '',
  agreement: false,
  verifyCode: ''
})

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules1 = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ],
  agreement: [
    { required: true, message: '请同意服务条款', trigger: 'change' }
  ]
}

const rules2 = {
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const maskedPhone = computed(() => {
  if (!form.phone) return ''
  return form.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
})

const handleStep1 = async () => {
  if (!formRef1.value) return
  await formRef1.value.validate((valid) => {
    if (valid) {
      step.value = 1
      sendCode()
    }
  })
}

const sendCode = () => {
  if (countdown.value > 0) return

  // 模拟发送验证码
  ElMessage.success('验证码已发送')
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const handleStep2 = async () => {
  if (!formRef2.value) return
  await formRef2.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await register({
          username: form.username,
          realName: form.realName,
          phone: form.phone,
          password: form.password
        })
        if (res.code === 200) {
          step.value = 2
        }
      } catch (error) {
        console.error('注册失败:', error)
      }
    }
  })
}
</script>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  padding: 40px 20px;
}

.register-container {
  width: 100%;
  max-width: 480px;
  background: #fff;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.2);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;

  .logo {
    font-size: 48px;
    color: #10B981;
    cursor: pointer;
    margin-bottom: 16px;
  }

  h1 {
    font-size: 24px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 8px;
  }

  p {
    font-size: 14px;
    color: #6b7280;
  }
}

.steps {
  margin-bottom: 30px;
}

.register-form {
  .el-input {
    --el-input-height: 48px;
  }

  .submit-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 500;
    margin-top: 10px;
  }

  .login-link {
    text-align: center;
    margin-top: 20px;
    font-size: 14px;
    color: #6b7280;
  }
}

.phone-verify {
  .verify-tip {
    text-align: center;
    color: #6b7280;
    margin-bottom: 20px;
  }
}

.form-actions {
  display: flex;
  gap: 16px;
  margin-top: 20px;

  .el-button {
    flex: 1;
    height: 48px;
  }
}

.success-page {
  padding: 40px 0;
}

@media (max-width: 768px) {
  .register-container {
    padding: 30px 20px;
  }
}
</style>
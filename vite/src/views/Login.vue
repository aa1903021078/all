<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1 class="login-title">月子中心服务管理系统</h1>
        <p class="login-subtitle">Maternity Care Center Management System</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register" class="register-link">立即注册</router-link>
      </div>
      <div class="test-accounts">
        <div class="test-accounts-title">测试账户（点击回填）</div>
        <div class="test-accounts-tags">
          <el-tag
            v-for="item in testAccounts"
            :key="item.username"
            :type="item.type"
            effect="plain"
            class="test-tag"
            @click="fillTestAccount(item)"
          >
            {{ item.label }}
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { login } from '@/api/index.js'
import { ElMessage } from 'element-plus'

const router = useRouter()
const store = useStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const testAccounts = [
  { label: '管理员', username: 'admin', password: '123456', type: 'danger' },
  { label: '月嫂', username: 'yuesao1', password: '123456', type: 'warning' },
  { label: '营养师', username: 'yingyang1', password: '123456', type: 'success' },
  { label: '护理人员', username: 'huli1', password: '123456', type: '' },
  { label: '客户', username: 'kehu1', password: '123456', type: 'info' }
]

const fillTestAccount = (account) => {
  form.username = account.username
  form.password = account.password
  ElMessage.success(`已填入${account.label}账户`)
}

const handleLogin = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await login({ username: form.username, password: form.password })
      const data = res.data
      if (data.code === 200 || data.code === 0) {
        const user = data.data || data.user
        const token = data.token || ''
        store.commit('SET_USER', user)
        store.commit('SET_TOKEN', token)
        ElMessage.success('登录成功')
        const role = user.role
        if (role === 0) {
          router.push('/home')
        } else if (role === 1) {
          router.push('/staff/life-care')
        } else if (role === 2) {
          router.push('/staff/diet-plan')
        } else if (role === 3) {
          router.push('/staff/medical-care')
        } else if (role === 4) {
          router.push('/customer/services')
        } else {
          router.push('/home')
        }
      } else {
        ElMessage.error(data.msg || data.message || '登录失败')
      }
    } catch (err) {
      ElMessage.error(err.response?.data?.msg || '登录失败，请检查网络')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f4fd 0%, #f0f7ff 50%, #ffffff 100%);
}

.login-card {
  width: 420px;
  padding: 40px 36px 30px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-title {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px;
  letter-spacing: 2px;
}

.login-subtitle {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
}

.login-footer {
  text-align: center;
  margin-top: 4px;
  font-size: 14px;
  color: #909399;
}

.register-link {
  color: #409eff;
  text-decoration: none;
  margin-left: 4px;
}

.register-link:hover {
  text-decoration: underline;
}

.test-accounts {
  margin-top: 24px;
  padding: 16px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  background: #fafbfc;
}

.test-accounts-title {
  font-size: 13px;
  color: #606266;
  margin-bottom: 12px;
  text-align: center;
  font-weight: 500;
}

.test-accounts-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.test-tag {
  cursor: pointer;
  transition: transform 0.15s;
  font-size: 13px;
}

.test-tag:hover {
  transform: scale(1.08);
}
</style>

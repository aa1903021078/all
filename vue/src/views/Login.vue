<template>
  <div class="login-page">
    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="login-header">
          <h2>社区回收系统</h2>
          <p>登录后进入对应端口</p>
        </div>
      </template>

      <el-tabs v-model="loginType" stretch>
        <el-tab-pane label="用户登录" name="user" />
        <el-tab-pane label="回收员登录" name="collector" />
      </el-tabs>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="footer-actions">
        <el-link type="primary" @click="goRegister">
          {{ loginType === 'user' ? '用户注册' : '回收员注册' }}
        </el-link>
        <el-link type="info" @click="showAdminLogin = true">管理员登录</el-link>
      </div>
    </el-card>

    <el-dialog v-model="showAdminLogin" title="管理员登录" width="420px" destroy-on-close>
      <el-form ref="adminFormRef" :model="adminForm" :rules="rules" label-position="top">
        <el-form-item label="账号" prop="username">
          <el-input v-model="adminForm.username" placeholder="请输入管理员账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="adminForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdminLogin = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleAdminLogin">登录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import userApi from '@/api/user'
import * as collectorApi from '@/api/collector'
import adminApi from '@/api/admin'

const router = useRouter()
const formRef = ref()
const adminFormRef = ref()

const loginType = ref('user')
const loading = ref(false)
const rememberMe = ref(false)
const showAdminLogin = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const adminForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const ensureLoginSuccess = (res) => {
  if (!res || `${res.code}` !== '200') {
    throw new Error(res?.msg || '登录失败')
  }
  return res.data || {}
}

const storeLogin = (account, roleKey) => {
  localStorage.setItem('token', account.token || 'token')
  localStorage.setItem('user', JSON.stringify(account))
  localStorage.setItem('role', account.role || '')
  localStorage.setItem('roleKey', roleKey)
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = loginType.value === 'user'
      ? await userApi.login(form)
      : await collectorApi.login(form)
    const account = ensureLoginSuccess(res)
    const roleKey = loginType.value === 'user' ? 'user' : 'collector'
    storeLogin(account, roleKey)

    if (rememberMe.value) {
      localStorage.setItem('remember', JSON.stringify(form))
    } else {
      localStorage.removeItem('remember')
    }

    ElMessage.success('登录成功')
    router.push(roleKey === 'user' ? '/user/home' : '/collector/workbench')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const handleAdminLogin = async () => {
  const valid = await adminFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await adminApi.login(adminForm)
    const account = ensureLoginSuccess(res)
    storeLogin(account, 'admin')
    ElMessage.success('管理员登录成功')
    showAdminLogin.value = false
    router.push('/admin/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const goRegister = () => {
  router.push(loginType.value === 'user' ? '/register' : '/collector-register')
}

onMounted(() => {
  const remember = localStorage.getItem('remember')
  if (!remember) return
  try {
    const cached = JSON.parse(remember)
    form.username = cached.username || ''
    form.password = cached.password || ''
    rememberMe.value = !!(form.username || form.password)
  } catch (_) {
    // ignore malformed cache
  }
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eff6ff, #ecfdf5);
  padding: 24px;
}

.login-card {
  width: 460px;
}

.login-header h2 {
  margin: 0;
  color: #1f2937;
}

.login-header p {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.footer-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
}
</style>

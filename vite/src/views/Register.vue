<template>
  <div class="auth-page">
    <div class="auth-card card">
      <div class="auth-banner">
        <div class="banner-inner">
          <div class="banner-logo">🍜 食光探店</div>
          <div class="banner-title">加入我们,记录每一餐美味</div>
          <ul class="banner-points">
            <li>📝 发布探店笔记,分享真实体验</li>
            <li>🛒 一键生成采购清单,照着菜谱买</li>
            <li>💬 与商家实时沟通,预约到店</li>
          </ul>
        </div>
      </div>

      <div class="auth-form">
        <h2 class="form-title">注册新账号</h2>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名(3-20位)">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="nickname">
            <el-input v-model="form.nickname" placeholder="昵称(选填)">
              <template #prefix><el-icon><Avatar /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="密码(6-20位)">
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="confirm">
            <el-input v-model="form.confirm" type="password" show-password placeholder="确认密码">
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="submit">
            注 册
          </el-button>
        </el-form>

        <div class="form-foot">
          <span class="muted">已有账号?</span>
          <el-link type="primary" :underline="false" @click="$router.push('/login')">去登录</el-link>
        </div>
        <el-alert
          class="mt-12"
          type="info"
          :closable="false"
          title="注册默认为普通用户,如需商家权限请联系管理员在后台分配角色"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const auth = useAuthStore()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', nickname: '', password: '', confirm: '' })

const validateConfirm = (rule, value, cb) => {
  if (value !== form.password) return cb(new Error('两次密码不一致'))
  cb()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3-20 位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' },
  ],
  confirm: [{ required: true, validator: validateConfirm, trigger: 'blur' }],
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await auth.register({ username: form.username, password: form.password, nickname: form.nickname })
    ElMessage.success('注册成功,请登录')
    router.push('/login')
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff6a3d 0%, #ff9800 100%);
  padding: 20px;
}
.auth-card {
  display: flex;
  width: 860px;
  max-width: 100%;
  min-height: 520px;
}
.auth-banner {
  width: 46%;
  background: linear-gradient(160deg, #ff7b4d 0%, #ff9d3d 100%);
  color: #fff;
  display: flex;
  align-items: center;
  padding: 40px;
}
.banner-logo {
  font-size: 30px;
  font-weight: 800;
  margin-bottom: 12px;
}
.banner-title {
  font-size: 16px;
  opacity: 0.95;
  margin-bottom: 28px;
}
.banner-points {
  list-style: none;
  padding: 0;
  margin: 0;
  line-height: 2.4;
  font-size: 14px;
  opacity: 0.95;
}
.auth-form {
  flex: 1;
  padding: 40px;
}
.form-title {
  margin: 0 0 22px;
  font-size: 22px;
  color: #303133;
}
.submit-btn {
  width: 100%;
  margin-top: 6px;
}
.form-foot {
  margin-top: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: flex-end;
}
@media (max-width: 720px) {
  .auth-banner {
    display: none;
  }
}
</style>

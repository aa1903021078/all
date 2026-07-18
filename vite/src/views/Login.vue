<template>
  <div class="auth-page">
    <div class="auth-card card">
      <div class="auth-banner">
        <div class="banner-inner">
          <div class="banner-logo">🍜 食光探店</div>
          <div class="banner-title">美食探店 &amp; 菜谱分享平台</div>
          <ul class="banner-points">
            <li>🗺️ 探索身边好味 · 美食地图一键直达</li>
            <li>⭐ 真实探店评分 · 点亮你的美食足迹</li>
            <li>👩‍🍳 分享居家菜谱 · 食材步骤图文并茂</li>
          </ul>
        </div>
      </div>

      <div class="auth-form">
        <h2 class="form-title">用户登录</h2>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="密码">
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="submit">
            登 录
          </el-button>
        </el-form>

        <div class="form-foot">
          <span class="muted">还没有账号?</span>
          <el-link type="primary" :underline="false" @click="$router.push('/register')">立即注册</el-link>
        </div>

        <el-divider>快速体验(密码均为 123456)</el-divider>
        <div class="demo-accounts">
          <el-tag
            v-for="d in demos"
            :key="d.username"
            :type="d.type"
            class="pointer"
            effect="plain"
            @click="fill(d.username)"
          >
            {{ d.label }}
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const demos = [
  { username: 'admin', label: '超级管理员', type: 'danger' },
  { username: 'reviewer', label: '审核员', type: 'warning' },
  { username: 'merchant1', label: '商家(老王川菜)', type: 'success' },
  { username: 'user1', label: '普通用户', type: 'info' },
]

function fill(username) {
  form.username = username
  form.password = '123456'
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const user = await auth.login({ ...form })
    ElMessage.success('欢迎回来,' + (user.nickname || user.username))
    const redirect = route.query.redirect
    router.push(redirect || '/')
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
  min-height: 480px;
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
  padding: 46px 40px;
}
.form-title {
  margin: 0 0 26px;
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
.demo-accounts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}
@media (max-width: 720px) {
  .auth-banner {
    display: none;
  }
}
</style>

<template>
  <div class="min-h-screen flex items-center justify-center"
       style="background: linear-gradient(135deg, #c20c0c 0%, #1f1f1f 100%);">
    <div class="w-[420px] bg-white rounded-lg shadow-xl p-8">
      <h1 class="text-center text-3xl font-bold mb-6" style="color: var(--yq-primary)">阅趣阁</h1>
      <p class="text-center text-sm text-gray-500 mb-6">图书阅读 · 商品购买 · 音乐欣赏 · 智能推荐</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" class="w-full" :loading="loading" @click="onSubmit">登录</el-button>
        <div class="text-center mt-3 text-sm">
          还没有账号？<router-link to="/register" class="text-red-600">立即注册</router-link>
        </div>
      </el-form>

      <el-divider>测试账号（点击一键回填）</el-divider>
      <div class="flex flex-wrap gap-2 justify-center">
        <el-button
          v-for="u in testUsers"
          :key="u.username"
          size="small"
          :type="u.role === 'ADMIN' ? 'danger' : 'default'"
          @click="fill(u)">
          {{ u.desc }}（{{ u.username }}）
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listTestUsers } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const form = ref({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }]
}
const formRef = ref()
const loading = ref(false)
const testUsers = ref([])

onMounted(async () => {
  try {
    testUsers.value = await listTestUsers()
  } catch (e) {
    // fallback static list
    testUsers.value = [
      { username: 'admin', password: '123456', role: 'ADMIN', desc: '管理员' },
      { username: 'user1', password: '123456', role: 'USER', desc: '普通用户' },
      { username: 'zzx', password: '123456', role: 'USER', desc: '测试用户 zzx' }
    ]
  }
})

function fill(u) {
  form.value.username = u.username
  form.value.password = u.password
}

async function onSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const data = await userStore.login(form.value)
    ElMessage.success('登录成功')
    router.push(data.role === 'ADMIN' ? '/admin' : '/home')
  } finally {
    loading.value = false
  }
}
</script>

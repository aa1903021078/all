<template>
  <div class="min-h-screen flex items-center justify-center"
       style="background: linear-gradient(135deg, #c20c0c 0%, #1f1f1f 100%);">
    <div class="w-[420px] bg-white rounded-lg shadow-xl p-8">
      <h1 class="text-center text-3xl font-bold mb-6" style="color: var(--yq-primary)">注册</h1>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="2-20 个字符" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="11 位手机号" />
        </el-form-item>
        <el-button type="primary" class="w-full" :loading="loading" @click="onSubmit">注册</el-button>
        <div class="text-center mt-3 text-sm">
          已有账号？<router-link to="/login" class="text-red-600">去登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const form = ref({ username: '', password: '', phone: '' })
const rules = {
  username: [{ required: true, min: 2, max: 20, message: '用户名 2-20 字符', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '请输入 11 位手机号', trigger: 'blur' }]
}
const formRef = ref()
const loading = ref(false)

async function onSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await register(form.value)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

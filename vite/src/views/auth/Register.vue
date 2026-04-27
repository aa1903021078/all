<template>
  <div class="reg-page">
    <el-card class="reg-card">
      <h2 class="text-xl font-semibold mb-1 text-center">患者注册</h2>
      <p class="text-sm text-slate-400 text-center mb-6">手机号或身份证号即可注册</p>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="登录用的用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="11 位手机号" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="可选" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-button type="primary" class="w-full" :loading="loading" @click="submit">注 册</el-button>
        <div class="text-center mt-3 text-sm text-slate-500">
          已有账号？<router-link to="/login" class="text-sky-500">返回登录</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { authApi } from '@/api'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', realName: '', phone: '', idCard: '', password: '' })
const phoneOrIdRule = (_, __, cb) => {
  if (!form.phone && !form.idCard) cb(new Error('手机号或身份证号至少填写一个'))
  else cb()
}
const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }, { min: 6, message: '至少 6 位' }],
  phone: [{ validator: phoneOrIdRule, trigger: 'blur' }],
  idCard: [{ validator: phoneOrIdRule, trigger: 'blur' }]
}
async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await authApi.register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally { loading.value = false }
}
</script>

<style scoped>
.reg-page { min-height:100vh; display:flex; align-items:center; justify-content:center;
  background: linear-gradient(135deg, #0ea5e9 0%, #6366f1 100%); padding:20px; }
.reg-card { width: 420px; max-width: 100%; }
</style>

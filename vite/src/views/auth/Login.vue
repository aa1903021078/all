<template>
  <div class="login-page">
    <div class="login-card">
      <div class="left">
        <div class="brand">
          <el-icon :size="42" color="#fff"><FirstAidKit /></el-icon>
          <h1>双向转诊医疗系统</h1>
        </div>
        <p class="slogan">连接基层与三甲，打通诊疗每一步</p>
        <ul class="features">
          <li><el-icon><CircleCheck /></el-icon> 在线挂号 · 多级机构智能排班</li>
          <li><el-icon><CircleCheck /></el-icon> 上下转诊 · 状态机全流程跟踪</li>
          <li><el-icon><CircleCheck /></el-icon> RBAC + JWT · 操作日志全留痕</li>
        </ul>
      </div>

      <div class="right">
        <h2>欢迎登录</h2>
        <p class="muted">使用您的账号进入系统</p>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名 / 手机号">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="密码">
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-button type="primary" class="w-full" :loading="loading" @click="submit">登 录</el-button>
        </el-form>
        <div class="reg-line">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>

        <el-divider><span class="muted text-xs">点击下方测试账号一键登录</span></el-divider>
        <div class="accounts">
          <div
            v-for="a in accounts"
            :key="a.username"
            class="account"
            :class="a.role"
            @click="quickLogin(a)"
          >
            <div class="role-tag">{{ a.label }}</div>
            <div class="info">
              <div class="user">{{ a.username }}</div>
              <div class="pwd">密码：{{ a.password }}</div>
            </div>
            <div class="hint">{{ a.hint }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const us = useUserStore()
const loading = ref(false)
const formRef = ref()
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const accounts = [
  { role: 'admin',   label: '管理员',  username: 'admin',    password: '123456', hint: '机构 / 用户 / 排班 / 日志' },
  { role: 'doctor',  label: '三甲医生', username: 'doctor1', password: '123456', hint: '内科 · 张主任' },
  { role: 'doctor',  label: '基层医生', username: 'doctor4', password: '123456', hint: '社区全科 · 赵医生' },
  { role: 'patient', label: '患者',    username: 'patient1', password: '123456', hint: '已有就诊记录' },
  { role: 'patient', label: '患者',    username: 'patient2', password: '123456', hint: '新患者' }
]

async function submit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await us.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.replace(us.homePath)
  } finally {
    loading.value = false
  }
}

function quickLogin(a) {
  form.username = a.username
  form.password = a.password
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0ea5e9 0%, #6366f1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.login-card {
  width: 920px;
  max-width: 100%;
  background: #fff;
  border-radius: 18px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0,0,0,0.18);
}
.left {
  background: linear-gradient(160deg, #0ea5e9 0%, #6366f1 100%);
  color: #fff;
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  .brand { display: flex; align-items: center; gap: 14px; }
  h1 { font-size: 24px; margin: 0; font-weight: 600; }
  .slogan { margin: 28px 0 36px; opacity: .9; font-size: 15px; }
  .features { list-style: none; padding: 0; margin: 0; }
  .features li {
    display: flex; align-items: center; gap: 8px;
    padding: 8px 0; opacity: .95; font-size: 14px;
  }
}
.right {
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  h2 { font-size: 22px; margin: 0 0 6px; color: #1e293b; }
  .muted { color: #94a3b8; font-size: 13px; margin: 0 0 24px; }
  .reg-line { text-align: center; margin-top: 14px; font-size: 13px; color: #64748b;
    a { color: #0ea5e9; }
  }
}
.accounts {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}
.account {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 8px 10px;
  cursor: pointer;
  background: #f8fafc;
  transition: all .15s;
  position: relative;
}
.account:hover { border-color: #0ea5e9; transform: translateY(-1px); background: #fff; }
.account .role-tag {
  display: inline-block;
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
  background: #e0f2fe;
  color: #0369a1;
  margin-bottom: 4px;
}
.account.doctor .role-tag { background: #dcfce7; color: #166534; }
.account.admin .role-tag { background: #fef3c7; color: #92400e; }
.account.patient .role-tag { background: #e0f2fe; color: #0369a1; }
.account .info { display: flex; gap: 8px; align-items: center; }
.account .user { font-weight: 600; color: #0f172a; font-size: 13px; }
.account .pwd { font-size: 11px; color: #64748b; }
.account .hint { font-size: 11px; color: #94a3b8; margin-top: 2px; }

@media (max-width: 720px) {
  .login-card { grid-template-columns: 1fr; }
  .left { display: none; }
}
</style>

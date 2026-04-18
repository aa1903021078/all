<template>
  <div class="profile-page">
    <el-row :gutter="16">
      <el-col :xs="24" :md="8">
        <el-card shadow="hover">
          <div class="user-header">
            <el-upload
              action="http://localhost:9090/files/upload"
              :show-file-list="false"
              :on-success="onAvatarUploaded"
            >
              <el-avatar :size="82" :src="user.avatar || '/default-avatar.png'" />
            </el-upload>
            <div class="name">{{ user.name || user.username || '用户' }}</div>
            <div class="sub">社区环保参与者</div>
          </div>

          <div class="stats-list">
            <div class="stat-item">
              <span>环保积分</span>
              <strong>{{ user.points || 0 }}</strong>
            </div>
            <div class="stat-item">
              <span>回收次数</span>
              <strong>{{ user.totalRecycleCount || 0 }}</strong>
            </div>
            <div class="stat-item">
              <span>回收重量</span>
              <strong>{{ user.totalRecycleWeight || 0 }} kg</strong>
            </div>
            <div class="stat-item">
              <span>减排贡献</span>
              <strong>{{ user.carbonSaved || 0 }} kg</strong>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="16">
        <el-card shadow="hover">
          <template #header>
            <span>基本信息</span>
          </template>
          <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="90px">
            <el-form-item label="账号">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="姓名" prop="name">
              <el-input v-model="profileForm.name" maxlength="20" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" maxlength="11" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="hover" style="margin-top: 16px">
          <template #header>
            <span>账号安全</span>
          </template>
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="danger" :loading="changingPwd" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 16px">
      <template #header>
        <span>最近动态（同步系统订单）</span>
      </template>
      <el-empty v-if="activities.length === 0" description="暂无动态" />
      <el-timeline v-else>
        <el-timeline-item
          v-for="(item, idx) in activities"
          :key="idx"
          :timestamp="item.time"
          :type="item.type"
        >
          {{ item.content }}
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const profileFormRef = ref()
const pwdFormRef = ref()
const saving = ref(false)
const changingPwd = ref(false)

const user = reactive({
  id: null,
  username: '',
  name: '',
  phone: '',
  avatar: '',
  points: 0,
  totalRecycleCount: 0,
  totalRecycleWeight: 0,
  carbonSaved: 0
})

const profileForm = reactive({
  id: null,
  username: '',
  name: '',
  phone: '',
  avatar: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const activities = ref([])

const profileRules = {
  name: [{ min: 2, max: 20, message: '姓名长度需在 2~20', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '至少 6 位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value !== pwdForm.newPassword) callback(new Error('两次密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

const normalizeDate = (value) => {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return '-'
  return `${d.getFullYear()}-${`${d.getMonth() + 1}`.padStart(2, '0')}-${`${d.getDate()}`.padStart(2, '0')} ${`${d.getHours()}`.padStart(2, '0')}:${`${d.getMinutes()}`.padStart(2, '0')}`
}

const loadProfile = async () => {
  const cached = JSON.parse(localStorage.getItem('user') || '{}')
  if (!cached?.id) {
    router.push('/login')
    return
  }
  const res = await request.get(`/user/selectById/${cached.id}`)
  const data = res?.data || {}
  Object.assign(user, data)
  Object.assign(profileForm, {
    id: data.id,
    username: data.username,
    name: data.name,
    phone: data.phone,
    avatar: data.avatar
  })
  localStorage.setItem('user', JSON.stringify(data))
}

const loadRecentActivities = async () => {
  const res = await request.get(`/recycleOrder/selectByUser/${user.id}`)
  const list = res?.data || []
  const rows = []

  list
    .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
    .slice(0, 12)
    .forEach((o) => {
      rows.push({
        time: normalizeDate(o.createTime),
        type: 'primary',
        content: `提交了 ${o.applianceTypeName || '回收'} 订单（${o.orderNo}）`
      })
      if (o.completeTime && o.status === 4) {
        rows.push({
          time: normalizeDate(o.completeTime),
          type: 'success',
          content: `完成订单，环保贡献 +${o.pointsEarned || 0} 积分，减排 ${o.carbonSaved || 0}kg`
        })
      }
      if (o.status === 5) {
        rows.push({
          time: normalizeDate(o.updateTime || o.cancelTime || o.createTime),
          type: 'danger',
          content: `取消了订单（${o.cancelReason || '无原因'}）`
        })
      }
    })

  activities.value = rows
}

const saveProfile = async () => {
  const valid = await profileFormRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await request.put('/user/update', profileForm)
    ElMessage.success('保存成功')
    await loadProfile()
  } catch (error) {
    ElMessage.error(error?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const onAvatarUploaded = async (res) => {
  const avatar = res?.data || ''
  if (!avatar) return
  profileForm.avatar = avatar
  await saveProfile()
}

const changePassword = async () => {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return

  changingPwd.value = true
  try {
    await request.put('/updatePassword', {
      username: user.username,
      password: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
      role: localStorage.getItem('role') || ''
    })
    ElMessage.success('密码修改成功，请重新登录')
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('role')
    localStorage.removeItem('roleKey')
    router.push('/login')
  } catch (error) {
    ElMessage.error(error?.message || '密码修改失败')
  } finally {
    changingPwd.value = false
  }
}

onMounted(async () => {
  await loadProfile()
  await loadRecentActivities()
})
</script>

<style scoped>
.profile-page {
  display: grid;
}

.user-header {
  text-align: center;
  margin-bottom: 14px;
}

.user-header .name {
  font-size: 18px;
  font-weight: 700;
  margin-top: 10px;
}

.user-header .sub {
  color: #6b7280;
  font-size: 13px;
  margin-top: 4px;
}

.stats-list {
  display: grid;
  gap: 10px;
}

.stat-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>

<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="info-card">
          <div class="avatar-section">
            <el-upload
                class="avatar-uploader"
                action="/api/files/upload"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
            >
              <img v-if="form.avatar" :src="form.avatar" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <h3>{{ form.name }}</h3>
            <p class="status" :class="form.workStatus === 1 ? 'working' : 'resting'">
              {{ form.workStatus === 1 ? '接单中' : '休息中' }}
            </p>
          </div>

          <div class="stats">
            <div class="stat">
              <div class="value">{{ form.orderCount }}</div>
              <div class="label">累计接单</div>
            </div>
            <div class="stat">
              <div class="value">{{ form.rating }}</div>
              <div class="label">评分</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <template #header>
            <span>基本信息</span>
          </template>

          <el-form :model="form" label-width="100px">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled />
            </el-form-item>

            <el-form-item label="真实姓名">
              <el-input v-model="form.name" />
            </el-form-item>

            <el-form-item label="手机号">
              <el-input v-model="form.phone" />
            </el-form-item>

            <el-form-item label="工作区域">
              <el-input v-model="form.serviceArea" type="textarea" :rows="2" placeholder="填写您的主要服务区域" />
            </el-form-item>

            <el-form-item label="每日上限">
              <el-input-number v-model="form.maxDailyOrders" :min="1" :max="50" />
              <span class="tip">单/天</span>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>修改密码</span>
          </template>

          <el-form :model="pwdForm" label-width="100px">
            <el-form-item label="原密码">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>

            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>

            <el-form-item label="确认密码">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
            </el-form-item>

            <el-form-item>
              <el-button type="danger" @click="changePassword" :loading="changingPwd">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const user = JSON.parse(localStorage.getItem('user') || '{}')
const saving = ref(false)
const changingPwd = ref(false)

const form = reactive({
  id: '',
  username: '',
  name: '',
  phone: '',
  avatar: '',
  serviceArea: '',
  maxDailyOrders: 10,
  workStatus: 0,
  orderCount: 0,
  rating: 5.0
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

onMounted(() => {
  loadProfile()
})

const loadProfile = async () => {
  try {
    const res = await request.get(`/collector/selectById/${user.id}`)
    Object.assign(form, res)
  } catch (e) {}
}

const handleAvatarSuccess = (res) => {
  form.avatar = res
  ElMessage.success('头像上传成功')
}

const saveProfile = async () => {
  saving.value = true
  try {
    await request.put('/collector/update', form)
    ElMessage.success('保存成功')
    loadProfile()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const changePassword = async () => {
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  changingPwd.value = true
  try {
    await request.put('/updatePassword', null, {
      params: {
        username: form.username,
        password: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword,
        role: '回收员'
      }
    })
    ElMessage.success('密码修改成功，请重新登录')
    localStorage.clear()
    location.href = '/login'
  } catch (error) {
    ElMessage.error(error.message || '修改失败')
  } finally {
    changingPwd.value = false
  }
}
</script>

<style scoped lang="scss">
.profile-page {
  .info-card {
    text-align: center;

    .avatar-section {
      padding: 30px 0;

      .avatar {
        width: 100px;
        height: 100px;
        border-radius: 50%;
      }

      h3 {
        margin-top: 15px;
        font-size: 20px;
      }

      .status {
        display: inline-block;
        padding: 4px 12px;
        border-radius: 12px;
        font-size: 12px;
        margin-top: 10px;

        &.working {
          background: #f6ffed;
          color: #52c41a;
        }

        &.resting {
          background: #f5f5f5;
          color: #999;
        }
      }
    }

    .stats {
      display: flex;
      justify-content: space-around;
      padding: 20px 0;
      border-top: 1px solid #eee;

      .stat {
        .value {
          font-size: 24px;
          font-weight: bold;
          color: #1890ff;
        }

        .label {
          color: #666;
          font-size: 13px;
          margin-top: 5px;
        }
      }
    }
  }

  .tip {
    margin-left: 10px;
    color: #999;
  }
}
</style>
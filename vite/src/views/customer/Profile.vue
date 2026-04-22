<template>
  <div class="app-container">
    <!-- User Info Card -->
    <el-card shadow="never" class="profile-card">
      <div class="profile-header">
        <div class="avatar-section">
          <el-avatar :size="80" :src="userInfo.avatar || ''" class="user-avatar">
            <el-icon :size="36"><UserFilled /></el-icon>
          </el-avatar>
          <div class="user-meta">
            <h2 class="user-display-name">{{ userInfo.realName || userInfo.username || '用户' }}</h2>
            <div class="user-tags">
              <el-tag type="success" size="small">{{ roleName }}</el-tag>
              <span class="user-join-date">注册时间：{{ userInfo.createTime || '-' }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- Edit Form -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <span class="section-title">编辑个人信息</span>
      </template>

      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" style="max-width: 500px">
        <el-form-item label="用户名">
          <el-input :model-value="userInfo.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            :http-request="handleUpload"
            :show-file-list="false"
            accept="image/*"
          >
            <el-image v-if="formData.avatar" :src="formData.avatar" style="width: 100px; height: 100px" fit="cover" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">点击上传头像</div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saveLoading" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Change Password Section -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <span class="section-title">修改密码</span>
      </template>

      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" style="max-width: 500px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="pwdLoading" @click="handleChangePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { UserFilled, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import { getUserInfo, updateUser, uploadFile } from '@/api/index.js'

const store = useStore()
const user = computed(() => store.getters.user)
const roleName = computed(() => {
  const role = userInfo.value.role
  return ({ 0: '管理员', 1: '客户', 2: '月嫂', 3: '营养师', 4: '护理人员' })[role] || '用户'
})

const userInfo = ref({})
const formRef = ref(null)
const formData = reactive({ realName: '', phone: '', avatar: '' })
const formRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}
const saveLoading = ref(false)

const pwdFormRef = ref(null)
const pwdForm = reactive({ newPassword: '', confirmPassword: '' })
const pwdRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) callback(new Error('两次输入密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}
const pwdLoading = ref(false)

const loadUserInfo = async () => {
  try {
    const res = await getUserInfo(user.value.id)
    userInfo.value = res.data || {}
    formData.realName = userInfo.value.realName || ''
    formData.phone = userInfo.value.phone || ''
    formData.avatar = userInfo.value.avatar || ''
  } catch {
    userInfo.value = user.value || {}
    formData.realName = userInfo.value.realName || ''
    formData.phone = userInfo.value.phone || ''
    formData.avatar = userInfo.value.avatar || ''
  }
}

const handleUpload = async (options) => {
  const fd = new FormData()
  fd.append('file', options.file)
  try {
    const res = await uploadFile(fd)
    formData.avatar = res.data
    ElMessage.success('头像上传成功')
  } catch {
    ElMessage.error('上传失败')
  }
}

const handleSave = async () => {
  await formRef.value.validate()
  saveLoading.value = true
  try {
    await updateUser({
      id: user.value.id,
      realName: formData.realName,
      phone: formData.phone,
      avatar: formData.avatar
    })
    ElMessage.success('个人信息更新成功')
    const updatedUser = { ...user.value, realName: formData.realName, phone: formData.phone, avatar: formData.avatar }
    store.commit('SET_USER', updatedUser)
    await loadUserInfo()
  } finally {
    saveLoading.value = false
  }
}

const handleChangePassword = async () => {
  await pwdFormRef.value.validate()
  pwdLoading.value = true
  try {
    await updateUser({
      id: user.value.id,
      password: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功')
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } finally {
    pwdLoading.value = false
  }
}

onMounted(() => loadUserInfo())
</script>

<style scoped>
.app-container { padding: 20px; }

.profile-header { display: flex; align-items: center; }
.avatar-section { display: flex; align-items: center; gap: 20px; }
.user-avatar { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.user-display-name { margin: 0 0 8px 0; font-size: 22px; font-weight: 700; color: #303133; }
.user-meta { display: flex; flex-direction: column; }
.user-tags { display: flex; align-items: center; gap: 12px; }
.user-join-date { font-size: 13px; color: #909399; }

.section-title { font-size: 16px; font-weight: 600; color: #303133; }

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color); border-radius: 6px; cursor: pointer;
  overflow: hidden; transition: var(--el-transition-duration-fast);
  width: 100px; height: 100px; display: flex; align-items: center; justify-content: center;
}
.avatar-uploader :deep(.el-upload:hover) { border-color: var(--el-color-primary); }
.uploader-icon { font-size: 24px; color: #8c939d; }
.upload-tip { font-size: 12px; color: #909399; margin-top: 4px; }
</style>

<template>
  <div class="yq-page max-w-[900px] mx-auto">
    <el-tabs v-model="tab">
      <el-tab-pane label="个人资料" name="profile">
        <div class="yq-card">
          <el-form label-width="100px" class="max-w-[500px]">
            <el-form-item label="头像">
              <el-avatar :size="64" :src="form.avatar || defaultAvatar" />
              <el-upload class="ml-3 inline-block" :show-file-list="false"
                         :action="uploadUrl" :headers="uploadHeaders"
                         :data="{ category: 'avatar' }"
                         :on-success="onAvatarSuccess" :on-error="onAvatarError">
                <el-button size="small">更换头像</el-button>
              </el-upload>
            </el-form-item>
            <el-form-item label="用户名"><el-input v-model="form.username" disabled /></el-form-item>
            <el-form-item label="角色"><el-input v-model="form.role" disabled /></el-form-item>
            <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
            <el-button type="primary" @click="saveProfile">保存资料</el-button>
          </el-form>
        </div>
      </el-tab-pane>
      <el-tab-pane label="修改密码" name="password">
        <div class="yq-card">
          <el-form :model="pwd" label-width="100px" class="max-w-[500px]">
            <el-form-item label="旧密码"><el-input v-model="pwd.oldPassword" type="password" show-password /></el-form-item>
            <el-form-item label="新密码"><el-input v-model="pwd.newPassword" type="password" show-password /></el-form-item>
            <el-button type="primary" @click="savePwd">修改密码</el-button>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMe, updateMe, changePassword } from '@/api/auth'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const tab = ref('profile')
const form = ref({ username: '', role: '', avatar: '', phone: '' })
const pwd = ref({ oldPassword: '', newPassword: '' })
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const uploadUrl = '/api/files/upload'
// 始终使用最新 token，避免刷新后 header 还是旧值
const uploadHeaders = computed(() => ({ Authorization: 'Bearer ' + userStore.token }))

async function load() {
  const u = await getMe()
  form.value = { username: u.username, role: u.role, avatar: u.avatar, phone: u.phone }
}
function onAvatarSuccess(res) {
  if (res.code === 200) {
    form.value.avatar = res.data.url
    ElMessage.success('头像已更新，请点保存')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}
function onAvatarError() { ElMessage.error('上传失败') }

async function saveProfile() {
  await updateMe({ avatar: form.value.avatar, phone: form.value.phone })
  await userStore.refreshMe()
  ElMessage.success('保存成功')
}
async function savePwd() {
  if (!pwd.value.oldPassword || pwd.value.newPassword.length < 6) {
    return ElMessage.warning('请正确填写旧密码与新密码（至少 6 位）')
  }
  await changePassword(pwd.value)
  pwd.value = { oldPassword: '', newPassword: '' }
  ElMessage.success('密码修改成功，请重新登录')
}
onMounted(load)
</script>

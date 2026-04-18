<template>
  <div class="admin-profile">
    <el-card>
      <template #header>
        <div class="header">
          <span>👤 管理员信息</span>
          <el-tag type="danger">只读模式</el-tag>
        </div>
      </template>

      <div class="profile-content">
        <div class="avatar-section">
          <el-avatar :size="100" :src="adminInfo.avatar" />
          <h3>{{ adminInfo.name }}</h3>
          <p class="role">{{ adminInfo.role || '系统管理员' }}</p>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="管理员ID">{{ adminInfo.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ adminInfo.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ adminInfo.name }}</el-descriptions-item>
          <el-descriptions-item label="角色权限">{{ adminInfo.role }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ adminInfo.createTime }}</el-descriptions-item>
          <el-descriptions-item label="登录时间">{{ lastLoginTime }}</el-descriptions-item>
        </el-descriptions>

        <div class="actions">
          <el-button type="danger" size="large" @click="logout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 系统信息 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>ℹ️ 系统信息</span>
      </template>
      <el-descriptions :column="2">
        <el-descriptions-item label="系统版本">v2.0.0</el-descriptions-item>
        <el-descriptions-item label="运行环境">Production</el-descriptions-item>
        <el-descriptions-item label="当前时间">{{ currentTime }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ clientIP }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const adminInfo = ref({})
const lastLoginTime = ref(localStorage.getItem('lastLoginTime') || '-')
const currentTime = ref('')
const clientIP = ref('127.0.0.1')
let timer = null

onMounted(() => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  adminInfo.value = user

  // 更新时间
  timer = setInterval(() => {
    currentTime.value = new Date().toLocaleString()
  }, 1000)
})

onUnmounted(() => {
  clearInterval(timer)
})

const logout = async () => {
  try {
    await ElMessageBox.confirm('确认退出管理系统？', '提示', {
      confirmButtonText: '确认退出',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 清除登录信息
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('role')

    ElMessage.success('已安全退出')
    router.push('/login')
  } catch (e) {}
}
</script>

<style scoped lang="scss">
.admin-profile {
  max-width: 800px;
  margin: 0 auto;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .profile-content {
    .avatar-section {
      text-align: center;
      margin-bottom: 30px;

      h3 {
        margin-top: 15px;
        font-size: 20px;
        color: #262626;
      }

      .role {
        color: #8c8c8c;
        margin-top: 5px;
      }
    }

    .actions {
      margin-top: 30px;
      text-align: center;

      .el-button {
        width: 200px;
      }
    }
  }
}
</style>
<template>
  <div class="home-container">
    <div class="welcome-banner">
      <h2>欢迎回来，{{ displayName }}！</h2>
      <p class="role-label">当前角色：{{ roleName }}</p>
    </div>

    <!-- Admin Dashboard -->
    <template v-if="role === 0">
      <el-row :gutter="20" class="stat-row">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card stat-blue">
            <div class="stat-content">
              <div class="stat-info">
                <div class="stat-num">128</div>
                <div class="stat-label">用户总数</div>
              </div>
              <el-icon class="stat-icon"><User /></el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card stat-green">
            <div class="stat-content">
              <div class="stat-info">
                <div class="stat-num">36</div>
                <div class="stat-label">服务项目数</div>
              </div>
              <el-icon class="stat-icon"><Grid /></el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card stat-orange">
            <div class="stat-content">
              <div class="stat-info">
                <div class="stat-num">12</div>
                <div class="stat-label">套餐数</div>
              </div>
              <el-icon class="stat-icon"><Box /></el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card stat-red">
            <div class="stat-content">
              <div class="stat-info">
                <div class="stat-num">256</div>
                <div class="stat-label">订单数</div>
              </div>
              <el-icon class="stat-icon"><Document /></el-icon>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header><span>快捷操作</span></template>
            <div class="quick-links">
              <el-button type="primary" plain @click="$router.push('/admin/users')">
                <el-icon><User /></el-icon>用户管理
              </el-button>
              <el-button type="success" plain @click="$router.push('/admin/orders')">
                <el-icon><Document /></el-icon>订单管理
              </el-button>
              <el-button type="warning" plain @click="$router.push('/admin/complaints')">
                <el-icon><ChatDotSquare /></el-icon>投诉处理
              </el-button>
              <el-button type="danger" plain @click="$router.push('/admin/staff-change')">
                <el-icon><Switch /></el-icon>人员更换
              </el-button>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header><span>系统信息</span></template>
            <div class="info-list">
              <div class="info-item"><span>系统名称</span><span>月子中心服务管理系统</span></div>
              <div class="info-item"><span>版本号</span><span>v1.0.0</span></div>
              <div class="info-item"><span>技术栈</span><span>Vue 3 + Element Plus + Spring Boot</span></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- Staff Dashboard (role 1, 2, 3) -->
    <template v-if="role >= 1 && role <= 3">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><Briefcase /></el-icon>
                <span>我的工作</span>
              </div>
            </template>
            <div class="work-info">
              <p v-if="role === 1">您好，月嫂！请前往 <strong>生活护理记录</strong> 页面记录和管理您的护理工作。</p>
              <p v-if="role === 2">您好，营养师！请前往 <strong>饮食方案制定</strong> 页面为客户制定饮食方案。</p>
              <p v-if="role === 3">您好，护理人员！请前往 <strong>医疗护理记录</strong> 页面记录医疗护理情况。</p>
              <el-button type="primary" class="work-btn" @click="goToWork">
                <el-icon><Right /></el-icon>
                进入工作台
              </el-button>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><InfoFilled /></el-icon>
                <span>工作提醒</span>
              </div>
            </template>
            <div class="work-info">
              <p>请及时完成护理记录的填写，确保信息准确。</p>
              <p>如有紧急情况请立即联系管理员处理。</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- Customer Dashboard (role 4) -->
    <template v-if="role === 4">
      <el-row :gutter="20" class="customer-cards">
        <el-col :span="6">
          <el-card shadow="hover" class="quick-entry" @click="$router.push('/customer/services')">
            <el-icon class="entry-icon" color="#409eff"><Grid /></el-icon>
            <div class="entry-text">服务浏览</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="quick-entry" @click="$router.push('/customer/packages')">
            <el-icon class="entry-icon" color="#67c23a"><Box /></el-icon>
            <div class="entry-text">套餐浏览</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="quick-entry" @click="$router.push('/customer/orders')">
            <el-icon class="entry-icon" color="#e6a23c"><Document /></el-icon>
            <div class="entry-text">我的订单</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="quick-entry" @click="$router.push('/customer/profile')">
            <el-icon class="entry-icon" color="#f56c6c"><Setting /></el-icon>
            <div class="entry-text">个人中心</div>
          </el-card>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header><span>温馨提示</span></template>
            <div class="work-info">
              <p>您可以浏览我们的服务项目和套餐，选择适合您的方案。</p>
              <p>如有任何问题可通过投诉通道反馈，我们会尽快处理。</p>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header><span>更多操作</span></template>
            <div class="quick-links">
              <el-button type="primary" plain @click="$router.push('/customer/care-records')">
                <el-icon><Notebook /></el-icon>护理记录
              </el-button>
              <el-button type="success" plain @click="$router.push('/customer/evaluations')">
                <el-icon><Star /></el-icon>评价
              </el-button>
              <el-button type="warning" plain @click="$router.push('/customer/complaints')">
                <el-icon><ChatDotSquare /></el-icon>投诉
              </el-button>
              <el-button type="info" plain @click="$router.push('/customer/staff-change')">
                <el-icon><Switch /></el-icon>人员更换
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'

const router = useRouter()
const store = useStore()

const user = computed(() => store.getters.user)
const role = computed(() => store.getters.userRole)

const displayName = computed(() => {
  return user.value?.realName || user.value?.username || '用户'
})

const roleMap = { 0: '管理员', 1: '月嫂', 2: '营养师', 3: '护理人员', 4: '客户' }
const roleName = computed(() => roleMap[role.value] || '未知')

const goToWork = () => {
  const paths = { 1: '/staff/life-care', 2: '/staff/diet-plan', 3: '/staff/medical-care' }
  router.push(paths[role.value] || '/home')
}
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.welcome-banner {
  background: linear-gradient(135deg, #409eff, #53a8ff);
  color: #fff;
  padding: 24px 32px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.welcome-banner h2 {
  margin: 0 0 6px;
  font-size: 22px;
}

.role-label {
  margin: 0;
  opacity: 0.85;
  font-size: 14px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-num {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.stat-icon {
  font-size: 48px;
  opacity: 0.2;
}

.stat-blue .stat-num { color: #409eff; }
.stat-blue .stat-icon { color: #409eff; }
.stat-green .stat-num { color: #67c23a; }
.stat-green .stat-icon { color: #67c23a; }
.stat-orange .stat-num { color: #e6a23c; }
.stat-orange .stat-icon { color: #e6a23c; }
.stat-red .stat-num { color: #f56c6c; }
.stat-red .stat-icon { color: #f56c6c; }

.quick-links {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #606266;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
}

.work-info p {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  margin: 0 0 8px;
}

.work-btn {
  margin-top: 12px;
}

.customer-cards {
  margin-bottom: 20px;
}

.quick-entry {
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s;
}

.quick-entry:hover {
  transform: translateY(-4px);
}

.entry-icon {
  font-size: 42px;
  margin-bottom: 10px;
}

.entry-text {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}
</style>

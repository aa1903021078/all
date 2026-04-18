<template>
  <div class="eco-platform">
    <!-- 顶部标题栏 -->
    <header class="platform-header">
      <div class="header-inner">
        <div class="brand">
          <span class="icon">🌱</span>
          <div>
            <h1>社区环保贡献榜</h1>
            <p>实时统计社区成员的环保贡献数据，共同建设绿色家园</p>
          </div>
        </div>
        <div class="header-stats">
          <div class="stat-item">
            <span class="num">{{ rankList.length }}</span>
            <span class="label">参与人数</span>
          </div>
          <div class="stat-item">
            <span class="num">{{ totalRecycle }}</span>
            <span class="label">总回收(kg)</span>
          </div>
        </div>
      </div>
    </header>

    <!-- 主体内容 -->
    <div class="platform-body">
      <div v-if="loading" class="loading-wrap">
        <el-skeleton :rows="5" animated />
      </div>

      <template v-else>
        <!-- TOP3 区域 - 缩小尺寸 -->
        <section class="top3-section">
          <div class="section-title">
            <span>本周前三名</span>
          </div>
          <div class="top3-grid">
            <div
                v-for="(item, index) in topThree"
                :key="item.id"
                class="winner-card"
                :class="{ 'first': index === 0 }"
            >
              <div class="rank-num-top">{{ index + 1 }}</div>
              <el-avatar :size="56" :src="item.avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <h3>{{ item.displayName }}</h3>
              <div class="data-row">
                <div class="item">
                  <span class="val">{{ item.weight }}</span>
                  <span class="unit">kg</span>
                </div>
                <div class="divider"></div>
                <div class="item">
                  <span class="val">{{ item.carbon }}</span>
                  <span class="unit">kg减碳</span>
                </div>
                <div class="divider"></div>
                <div class="item">
                  <span class="val">{{ item.points }}</span>
                  <span class="unit">积分</span>
                </div>
              </div>
              <div class="count-tag">回收 {{ item.count }} 次</div>
            </div>
          </div>
        </section>

        <!-- 左右分栏 -->
        <div class="content-layout">
          <!-- 左侧：完整榜单 -->
          <main class="list-panel">
            <div class="panel-header">
              <h2>完整榜单</h2>
              <el-radio-group v-model="rankType" size="small">
                <el-radio-button value="weight">按回收重量</el-radio-button>
                <el-radio-button value="carbon">按减碳量</el-radio-button>
                <el-radio-button value="points">按积分</el-radio-button>
              </el-radio-group>
            </div>

            <div class="table-container">
              <table class="rank-table">
                <thead>
                <tr>
                  <th width="80">排名</th>
                  <th>用户</th>
                  <th width="90" align="center">次数</th>
                  <th width="110" align="right">重量(kg)</th>
                  <th width="110" align="right">减碳(kg)</th>
                </tr>
                </thead>
                <tbody>
                <tr
                    v-for="(row, index) in rankList"
                    :key="row.id"
                    :class="{ 'is-me': row.isCurrentUser, 'top3': index < 3 }"
                >
                  <td>
                    <span class="rank-badge" :class="{ 'top': index < 3 }">{{ index + 1 }}</span>
                  </td>
                  <td>
                    <div class="user-cell">
                      <el-avatar :size="36" :src="row.avatar">
                        <el-icon><User /></el-icon>
                      </el-avatar>
                      <span class="username">{{ row.displayName }}</span>
                      <el-tag v-if="row.isCurrentUser" size="small" type="success" effect="light" class="me-tag">我</el-tag>
                    </div>
                  </td>
                  <td align="center">{{ row.count }}</td>
                  <td align="right" class="text-bold">{{ row.weight }}</td>
                  <td align="right" class="text-green">{{ row.carbon }}</td>
                </tr>
                </tbody>
              </table>

              <el-empty v-if="rankList.length === 0" description="暂无数据" />
            </div>
          </main>

          <!-- 右侧：我的档案（移除按钮和提示） -->
          <aside class="sidebar-panel">
            <div v-if="currentUser.id" class="profile-card">
              <div class="card-header">
                <span>我的环保档案</span>
                <el-tag v-if="myRank !== '未上榜'" type="success" effect="dark" size="small">第 {{ myRank }} 名</el-tag>
                <el-tag v-else type="info" effect="plain" size="small">未上榜</el-tag>
              </div>

              <div class="card-body">
                <div class="avatar-wrap">
                  <el-avatar :size="80" :src="processAvatar(currentUser.avatar)">
                    <el-icon :size="32"><User /></el-icon>
                  </el-avatar>
                  <div v-if="myRank !== '未上榜'" class="rank-circle">{{ myRank }}</div>
                </div>

                <h3 class="user-name">{{ displayName(currentUser) }}</h3>

                <div class="stats-list">
                  <div class="stat-line">
                    <span class="label">回收重量</span>
                    <span class="value">{{ myStats.weight }} <small>kg</small></span>
                  </div>
                  <div class="stat-line">
                    <span class="label">减碳贡献</span>
                    <span class="value green">{{ myStats.carbon }} <small>kg</small></span>
                  </div>
                  <div class="stat-line">
                    <span class="label">环保积分</span>
                    <span class="value">{{ myStats.points }} <small>分</small></span>
                  </div>
                  <div class="stat-line">
                    <span class="label">回收次数</span>
                    <span class="value">{{ myStats.count }} <small>次</small></span>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="login-card">
              <el-icon :size="48" color="#52c41a"><User /></el-icon>
              <p>登录后查看您的排名</p>
              <el-button type="success" @click="goLogin" style="width: 100%;">立即登录</el-button>
            </div>
          </aside>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const loading = ref(true)
const rankType = ref('weight')
const rankList = ref([])
const myRank = ref('未上榜')
const myStats = ref({
  weight: '0.0',
  carbon: '0.0',
  points: 0,
  count: 0
})
const currentUser = ref({})

const topThree = computed(() => rankList.value.slice(0, 3))
const totalRecycle = computed(() => {
  return rankList.value.reduce((sum, item) => sum + parseFloat(item.weight || 0), 0).toFixed(1)
})

const getCurrentUser = () => {
  try {
    const str = localStorage.getItem('user')
    if (!str || str === 'undefined') return {}
    return JSON.parse(str)
  } catch (e) {
    return {}
  }
}

const displayName = (user) => {
  if (!user) return '匿名用户'
  if (user.name && user.name !== 'null') return user.name
  if (user.username) return user.username
  return '匿名用户'
}

const checkIsMe = (id) => {
  return String(id) === String(currentUser.value.id)
}

const formatNum = (num) => {
  if (num === undefined || num === null) return '0.0'
  return parseFloat(num).toFixed(1)
}

const processAvatar = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  return `http://localhost:9090${avatar.startsWith('/') ? '' : '/'}${avatar}`
}

const goLogin = () => router.push('/login')

const normalizeData = (list) => {
  return list.map(item => {
    const isMe = checkIsMe(item.id)
    let weight = item.totalWeight || item.weight || 0
    let carbon = item.carbonSaved || item.carbon || 0
    let count = item.recycleCount || item.totalRecycleCount || 0
    let points = item.points || 0
    let avatar = processAvatar(item.avatar)
    let name = item.username || item.name || '匿名用户'

    if (isMe && currentUser.value.id) {
      weight = weight || currentUser.value.totalRecycleWeight || 0
      carbon = carbon || currentUser.value.carbonSaved || 0
      count = count || currentUser.value.totalRecycleCount || 0
      points = points || currentUser.value.points || 0
      avatar = avatar || processAvatar(currentUser.value.avatar)
    }

    return {
      id: item.id,
      displayName: name,
      avatar,
      weight: formatNum(weight),
      carbon: formatNum(carbon),
      points,
      count,
      isCurrentUser: isMe
    }
  })
}

const fetchData = async () => {
  try {
    const res = await request.get('/statistics/userRanking', {
      params: { limit: 50, type: rankType.value }
    })

    let data = []
    if (Array.isArray(res)) data = res
    else if (res.data && Array.isArray(res.data)) data = res.data

    rankList.value = normalizeData(data)
    updateMyRank()
  } catch (e) {
    ElMessage.error('加载失败')
    rankList.value = []
  }
}

const updateMyRank = () => {
  if (!currentUser.value.id) {
    myRank.value = '未上榜'
    return
  }

  const idx = rankList.value.findIndex(i => i.isCurrentUser)
  if (idx !== -1) {
    myRank.value = idx + 1
    const me = rankList.value[idx]
    myStats.value = {
      weight: me.weight,
      carbon: me.carbon,
      points: me.points,
      count: me.count
    }
  } else {
    myRank.value = '未上榜'
    myStats.value = {
      weight: formatNum(currentUser.value.totalRecycleWeight),
      carbon: formatNum(currentUser.value.carbonSaved),
      points: currentUser.value.points || 0,
      count: currentUser.value.totalRecycleCount || 0
    }
  }
}

const init = async () => {
  loading.value = true
  currentUser.value = getCurrentUser()
  await fetchData()
  loading.value = false
}

onMounted(init)
watch(rankType, init)
</script>

<style scoped>
.eco-platform {
  min-height: 100vh;
  background: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 顶部标题栏 */
.platform-header {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  color: white;
  padding: 24px 0;
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.2);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.brand {
  display: flex;
  align-items: center;
  gap: 16px;
}

.brand .icon {
  font-size: 40px;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
}

.brand h1 {
  margin: 0 0 4px 0;
  font-size: 28px;
  font-weight: 600;
  letter-spacing: -0.5px;
}

.brand p {
  margin: 0;
  opacity: 0.95;
  font-size: 14px;
}

.header-stats {
  display: flex;
  gap: 32px;
}

.header-stats .stat-item {
  text-align: center;
}

.header-stats .num {
  display: block;
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 4px;
}

.header-stats .label {
  font-size: 13px;
  opacity: 0.9;
}

/* 主体内容区 */
.platform-body {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.loading-wrap {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

/* TOP3 区域 - 缩小尺寸 */
.top3-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-left: 8px;
  border-left: 4px solid #52c41a;
}

.top3-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.winner-card {
  background: white;
  border-radius: 12px;
  padding: 20px 16px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  border: 1px solid #eaeaea;
  position: relative;
  transition: all 0.3s;
}

.winner-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.winner-card.first {
  border-color: #52c41a;
  background: linear-gradient(to bottom, #f6ffed, white);
}

.rank-num-top {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 24px;
  height: 24px;
  background: #f0f0f0;
  color: #666;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 12px;
}

.winner-card.first .rank-num-top {
  background: #52c41a;
  color: white;
}

.winner-card h3 {
  margin: 10px 0 12px 0;
  font-size: 16px;
  color: #1a1a1a;
  font-weight: 600;
}

.data-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.data-row .item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.data-row .val {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
}

.data-row .unit {
  font-size: 11px;
  color: #999;
}

.data-row .divider {
  width: 1px;
  height: 24px;
  background: #eaeaea;
}

.count-tag {
  display: inline-block;
  padding: 3px 10px;
  background: #f5f5f5;
  border-radius: 10px;
  font-size: 12px;
  color: #666;
}

/* 左右分栏布局 */
.content-layout {
  display: flex;
  gap: 24px;
}

.list-panel {
  flex: 1;
  background: white;
  border-radius: 12px;
  border: 1px solid #eaeaea;
  overflow: hidden;
}

.panel-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

/* 表格样式 */
.table-container {
  padding: 0;
}

.rank-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.rank-table th {
  text-align: left;
  padding: 16px 24px;
  font-weight: 600;
  color: #666;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
  font-size: 13px;
}

.rank-table td {
  padding: 14px 24px;
  border-bottom: 1px solid #f5f5f5;
  color: #333;
}

.rank-table tbody tr:hover {
  background: #fafafa;
}

.rank-table tr.is-me {
  background: #f6ffed !important;
}

.rank-table tr.is-me td {
  font-weight: 500;
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  background: #f0f0f0;
  color: #666;
  border-radius: 50%;
  font-size: 13px;
  font-weight: 600;
}

.rank-badge.top {
  background: #52c41a;
  color: white;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-cell .username {
  font-weight: 500;
  color: #1a1a1a;
}

.me-tag {
  margin-left: 4px;
}

.text-bold {
  font-weight: 600;
  color: #1a1a1a;
}

.text-green {
  font-weight: 600;
  color: #52c41a;
}

/* 右侧边栏 */
.sidebar-panel {
  width: 320px;
  flex-shrink: 0;
}

.profile-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #eaeaea;
  overflow: hidden;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #1a1a1a;
}

.card-body {
  padding: 24px 20px;
  text-align: center;
}

.avatar-wrap {
  position: relative;
  display: inline-block;
  margin-bottom: 12px;
}

.avatar-wrap .el-avatar {
  border: 3px solid #f6ffed;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.rank-circle {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 28px;
  height: 28px;
  background: #52c41a;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 12px;
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.user-name {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.stats-list {
  text-align: left;
}

.stat-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.stat-line:last-child {
  border-bottom: none;
}

.stat-line .label {
  color: #666;
  font-size: 14px;
}

.stat-line .value {
  font-weight: 600;
  color: #1a1a1a;
  font-size: 16px;
}

.stat-line .value small {
  font-size: 12px;
  color: #999;
  font-weight: normal;
  margin-left: 2px;
}

.stat-line .value.green {
  color: #52c41a;
}

/* 登录卡片 */
.login-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #eaeaea;
  padding: 40px 20px;
  text-align: center;
}

.login-card p {
  color: #666;
  margin: 16px 0;
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 992px) {
  .content-layout {
    flex-direction: column;
  }

  .sidebar-panel {
    width: 100%;
  }

  .top3-grid {
    grid-template-columns: 1fr;
  }

  .header-inner {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
}
</style>
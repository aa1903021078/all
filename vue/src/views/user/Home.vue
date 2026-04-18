<template>
  <div class="home-page">
    <el-card class="hero-card" shadow="hover">
      <h2>社区家电回收服务</h2>
      <p>一键预约上门回收，参与社区绿色行动，实时查看环保贡献。</p>
      <div class="hero-actions">
        <el-button type="primary" @click="$router.push('/user/recycle')">立即预约</el-button>
        <el-button @click="$router.push('/user/notice')">社区公告</el-button>
      </div>
    </el-card>

    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :md="6">
        <el-statistic title="社区总订单" :value="overview.totalOrders || 0" />
      </el-col>
      <el-col :xs="12" :md="6">
        <el-statistic title="已完成" :value="overview.completedOrders || 0" />
      </el-col>
      <el-col :xs="12" :md="6">
        <el-statistic title="回收重量(kg)" :value="Number(overview.totalWeight || 0)" />
      </el-col>
      <el-col :xs="12" :md="6">
        <el-statistic title="社区减排(kg)" :value="Number(overview.totalCarbonSaved || 0)" />
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-title">
              <span>社区公告</span>
              <el-link type="primary" @click="$router.push('/user/notice')">更多</el-link>
            </div>
          </template>
          <el-empty v-if="notices.length === 0" description="暂无社区公告" :image-size="70" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="item in notices"
              :key="item.id"
              :timestamp="formatDate(item.publishTime)"
            >
              <div class="notice-item" @click="openNotice(item)">
                <strong>{{ item.title }}</strong>
                <p>{{ shortText(item.content) }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <span>社区可回收品类</span>
          </template>
          <el-empty v-if="categories.length === 0" description="暂无品类数据" :image-size="70" />
          <div v-else class="category-grid">
            <div v-for="c in categories" :key="c.id" class="category-item">
              <div class="name">{{ c.name }}</div>
              <div class="meta">价格区间：{{ c.priceMin }} - {{ c.priceMax }}</div>
              <div class="meta">积分：{{ c.pointsPerKg }}/kg</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="noticeVisible" :title="currentNotice.title || '公告详情'" width="640px">
      <div class="notice-detail">
        <div class="meta">发布时间：{{ formatDate(currentNotice.publishTime) }}</div>
        <div class="content">{{ currentNotice.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import request from '@/utils/request'

const overview = ref({})
const notices = ref([])
const categories = ref([])
const noticeVisible = ref(false)
const currentNotice = ref({})

const loadOverview = async () => {
  const res = await request.get('/statistics/overview')
  overview.value = res?.data || {}
}

const loadNotices = async () => {
  const res = await request.get('/notice/selectPublished')
  notices.value = (res?.data || []).slice(0, 6)
}

const loadCategories = async () => {
  const res = await request.get('/applianceType/selectEnabled')
  categories.value = (res?.data || []).slice(0, 8)
}

const shortText = (text) => {
  if (!text) return ''
  return text.length > 44 ? `${text.slice(0, 44)}...` : text
}

const formatDate = (time) => {
  if (!time) return '-'
  const d = new Date(time)
  if (Number.isNaN(d.getTime())) return '-'
  return `${d.getFullYear()}-${`${d.getMonth() + 1}`.padStart(2, '0')}-${`${d.getDate()}`.padStart(2, '0')}`
}

const openNotice = async (notice) => {
  currentNotice.value = notice
  noticeVisible.value = true
  try {
    await request.put(`/notice/increaseView/${notice.id}`)
  } catch (_) {
    // ignore
  }
}

onMounted(async () => {
  await Promise.allSettled([loadOverview(), loadNotices(), loadCategories()])
})
</script>

<style scoped>
.home-page {
  display: grid;
  gap: 16px;
}

.hero-card h2 {
  margin: 0;
}

.hero-card p {
  color: #6b7280;
  margin: 8px 0 12px;
}

.hero-actions {
  display: flex;
  gap: 10px;
}

.stats-row {
  margin: 0 !important;
  background: #fff;
  border-radius: 10px;
  padding: 12px;
}

.card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.notice-item {
  cursor: pointer;
}

.notice-item p {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.category-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px;
}

.category-item .name {
  font-weight: 600;
}

.category-item .meta {
  color: #6b7280;
  font-size: 12px;
  margin-top: 4px;
}

.notice-detail .meta {
  color: #6b7280;
  font-size: 13px;
  margin-bottom: 8px;
}

.notice-detail .content {
  white-space: pre-wrap;
  line-height: 1.75;
}
</style>

<template>
  <div class="notice-page">
    <el-card shadow="never">
      <div class="top-bar">
        <el-radio-group v-model="noticeType" @change="loadNotices">
          <el-radio-button :label="null">全部</el-radio-button>
          <el-radio-button :label="1">社区活动</el-radio-button>
          <el-radio-button :label="2">政策解读</el-radio-button>
          <el-radio-button :label="3">环保知识</el-radio-button>
          <el-radio-button :label="4">系统公告</el-radio-button>
        </el-radio-group>
      </div>

      <el-empty v-if="notices.length === 0 && !loading" description="暂无公告" />
      <el-skeleton v-if="loading" :rows="6" animated />

      <div v-for="item in notices" :key="item.id" class="notice-item" @click="openDetail(item)">
        <div class="title-row">
          <div class="left">
            <el-tag :type="typeColor(item.type)" size="small">{{ typeText(item.type) }}</el-tag>
            <span class="title">{{ item.title }}</span>
            <el-tag v-if="item.isTop === 1" type="danger" effect="dark" size="small">置顶</el-tag>
          </div>
          <span class="time">{{ formatDate(item.publishTime) }}</span>
        </div>
        <p class="summary">{{ shortText(item.content) }}</p>
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" :title="currentNotice.title || '公告详情'" width="700px">
      <div class="notice-detail">
        <div class="meta">
          <el-tag :type="typeColor(currentNotice.type)">{{ typeText(currentNotice.type) }}</el-tag>
          <span>{{ formatDate(currentNotice.publishTime) }}</span>
          <span>浏览 {{ currentNotice.viewCount || 0 }}</span>
        </div>
        <div class="content">{{ currentNotice.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import request from '@/utils/request'

const noticeType = ref(null)
const notices = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const currentNotice = ref({})

const loadNotices = async () => {
  loading.value = true
  try {
    const res = await request.get('/notice/selectPublished', { params: { type: noticeType.value } })
    notices.value = res?.data || []
  } finally {
    loading.value = false
  }
}

const typeText = (type) => ({ 1: '社区活动', 2: '政策', 3: '知识', 4: '公告' }[type] || '其他')
const typeColor = (type) => ({ 1: 'success', 2: 'warning', 3: 'info', 4: 'danger' }[type] || 'info')

const shortText = (text) => {
  if (!text) return ''
  return text.length > 90 ? `${text.slice(0, 90)}...` : text
}

const formatDate = (value) => {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return '-'
  return `${d.getFullYear()}-${`${d.getMonth() + 1}`.padStart(2, '0')}-${`${d.getDate()}`.padStart(2, '0')}`
}

const openDetail = async (notice) => {
  currentNotice.value = { ...notice }
  detailVisible.value = true
  try {
    await request.put(`/notice/increaseView/${notice.id}`)
    currentNotice.value.viewCount = (currentNotice.value.viewCount || 0) + 1
  } catch (_) {
    // ignore
  }
}

onMounted(loadNotices)
</script>

<style scoped>
.notice-page {
  display: grid;
  gap: 12px;
}

.top-bar {
  margin-bottom: 14px;
}

.notice-item {
  padding: 14px 0;
  border-bottom: 1px solid #e5e7eb;
  cursor: pointer;
}

.notice-item:last-child {
  border-bottom: none;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.title-row .left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title {
  font-weight: 600;
}

.time {
  color: #6b7280;
  font-size: 13px;
}

.summary {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.6;
}

.notice-detail .meta {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #6b7280;
  margin-bottom: 12px;
  font-size: 13px;
}

.notice-detail .content {
  white-space: pre-wrap;
  line-height: 1.8;
}
</style>

<template>
  <div>
    <el-tabs v-model="activeTab" class="card content-tabs" @tab-change="reload">
      <el-tab-pane label="📝 探店笔记" name="note" />
      <el-tab-pane label="🍲 分享菜谱" name="recipe" />
    </el-tabs>

    <div class="card filter-bar mt-16">
      <el-input v-model="query.keyword" placeholder="搜索标题" style="width: 200px" clearable @keyup.enter="reload" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 130px" @change="reload">
        <el-option label="待审核" :value="0" />
        <el-option label="已发布" :value="1" />
        <el-option label="已下架" :value="2" />
        <el-option label="违规" :value="3" />
      </el-select>
      <el-button type="primary" @click="reload"><el-icon><Search /></el-icon>&nbsp;查询</el-button>
      <el-tag type="warning" class="ml-auto" v-if="pendingHint">待审核内容请及时处理</el-tag>
    </div>

    <div class="card mt-16" style="padding: 16px" v-loading="loading">
      <el-table :data="list" style="width: 100%">
        <el-table-column label="内容" min-width="260">
          <template #default="{ row }">
            <div class="flex items-center gap-8">
              <img :src="cover(row)" class="mini-cover" />
              <div>
                <div class="bold line-1">{{ row.title || '(无标题)' }}</div>
                <div class="muted text-sm line-1">{{ row.content || row.description }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="作者" width="120">
          <template #default="{ row }">{{ row.authorName }}</template>
        </el-table-column>
        <el-table-column label="数据" width="140">
          <template #default="{ row }">
            <span class="muted text-sm">❤️ {{ row.likeCount || 0 }} · 👀 {{ row.viewCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="preview(row)">查看</el-button>
            <el-button v-if="row.status !== 1" size="small" type="success" @click="review(row, 1)">通过</el-button>
            <el-button v-if="row.status !== 3" size="small" type="danger" @click="review(row, 3)">违规</el-button>
            <el-button v-if="row.status === 1" size="small" type="warning" plain @click="review(row, 2)">下架</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex justify-end mt-16">
        <el-pagination
          layout="total, prev, pager, next"
          :total="total"
          :page-size="query.size"
          :current-page="query.current"
          @current-change="pageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { noteApi, recipeApi } from '@/api'

const router = useRouter()
const activeTab = ref('note')
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ current: 1, size: 10, keyword: '', status: null })

const pendingHint = computed(() => list.value.some((r) => r.status === 0))

const statusTextMap = { 0: '待审核', 1: '已发布', 2: '已下架', 3: '违规' }
const statusTagMap = { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }
function statusText(s) {
  return statusTextMap[s] || '-'
}
function statusTag(s) {
  return statusTagMap[s] || 'info'
}
function cover(row) {
  if (activeTab.value === 'recipe') return row.cover
  try {
    const arr = JSON.parse(row.images || '[]')
    return arr[0] || row.shopCover || 'https://picsum.photos/seed/note/80'
  } catch (e) {
    return 'https://picsum.photos/seed/note/80'
  }
}

async function load() {
  loading.value = true
  try {
    const api = activeTab.value === 'note' ? noteApi : recipeApi
    const res = await api.adminPage({ ...query })
    list.value = (res.data && res.data.records) || []
    total.value = (res.data && res.data.total) || 0
  } finally {
    loading.value = false
  }
}
function reload() {
  query.current = 1
  load()
}
function pageChange(p) {
  query.current = p
  load()
}

async function review(row, status) {
  if (activeTab.value === 'note') {
    await noteApi.review(row.id, status)
  } else {
    await recipeApi.changeStatus(row.id, status)
  }
  row.status = status
  ElMessage.success('审核完成')
}

function preview(row) {
  const path = activeTab.value === 'note' ? '/notes/' : '/recipes/'
  const url = router.resolve(path + row.id).href
  window.open(url, '_blank')
}

onMounted(load)
</script>

<style scoped>
.content-tabs {
  padding: 6px 20px 0;
}
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
}
.ml-auto {
  margin-left: auto;
}
.mini-cover {
  width: 46px;
  height: 46px;
  border-radius: 6px;
  object-fit: cover;
}
</style>

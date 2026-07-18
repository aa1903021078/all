<template>
  <div v-loading="loading">
    <div class="flex items-center gap-12 mb-12">
      <span class="bold text-lg">评价管理</span>
      <el-select v-model="currentShopId" placeholder="选择店铺" style="width: 220px" @change="onShopChange">
        <el-option v-for="s in shops" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <div class="rating-summary" v-if="currentShop">
        综合评分
        <StarRating :model-value="Number(currentShop.rating)" :size="16" readonly show-score />
        <span class="muted">({{ currentShop.ratingCount || 0 }}条)</span>
      </div>
    </div>

    <!-- 评分走势 / 分布 -->
    <div class="stat-charts" v-if="notes.length">
      <div class="chart-card card">
        <div class="chart-title">⭐ 评分分布</div>
        <EChart v-if="ratingDistOption" :option="ratingDistOption" height="260px" />
        <el-empty v-else description="暂无评分数据" :image-size="60" />
      </div>
      <div class="chart-card card">
        <div class="chart-title">📈 评分走势</div>
        <EChart v-if="ratingTrendOption" :option="ratingTrendOption" height="260px" />
        <el-empty v-else description="暂无评分数据" :image-size="60" />
      </div>
    </div>

    <!-- 我的差评申诉 -->
    <div class="card appeal-card" v-if="shopAppeals.length">
      <div class="block-title">🛡️ 我的差评申诉</div>
      <div v-for="a in shopAppeals" :key="a.id" class="appeal-item">
        <div class="flex items-center justify-between">
          <div class="bold line-1">{{ a.noteTitle || a.noteContent || '差评申诉' }}</div>
          <el-tag :type="appealStatus(a.status).type" size="small">{{ appealStatus(a.status).t }}</el-tag>
        </div>
        <div class="muted text-sm mt-4">申诉理由：{{ a.reason }}</div>
        <div v-if="a.reply" class="appeal-reply mt-8">平台回复：{{ a.reply }}</div>
      </div>
    </div>

    <div class="card" style="padding: 16px">
      <div v-for="n in notes" :key="n.id" class="review-item">
        <el-avatar :size="42" :src="n.authorAvatar" />
        <div class="review-body">
          <div class="flex items-center justify-between">
            <span class="bold">{{ n.authorName }}</span>
            <StarRating v-if="n.rating" :model-value="n.rating" :size="14" readonly />
          </div>
          <div class="review-title" v-if="n.title">{{ n.title }}</div>
          <div class="muted mt-8">{{ n.content }}</div>
          <div class="review-imgs" v-if="parseImgs(n.images).length">
            <img v-for="(img, i) in parseImgs(n.images)" :key="i" :src="img" />
          </div>
          <div class="muted text-sm mt-8 review-foot">
            <span>{{ formatTime(n.createTime) }} · ❤️ {{ n.likeCount || 0 }} · 💬 {{ n.commentCount || 0 }}</span>
            <el-link type="primary" :underline="false" @click="toggleReply(n)">
              {{ expandedId === n.id ? '收起回复' : '回复评价' }}
            </el-link>
            <el-link v-if="isLowRating(n)" type="danger" :underline="false" @click="openAppeal(n)">差评申诉</el-link>
            <el-link type="info" :underline="false" @click="$router.push('/notes/' + n.id)">查看详情</el-link>
          </div>

          <div v-if="expandedId === n.id" class="review-comments">
            <CommentSection target-type="NOTE" :target-id="n.id" />
          </div>
        </div>
      </div>
      <el-empty v-if="!notes.length" description="该店铺暂无评价" />
    </div>

    <!-- 差评申诉弹窗 -->
    <el-dialog v-model="appealVisible" title="差评申诉" width="480px">
      <div class="muted mb-8">申诉笔记：{{ appealForm.noteTitle }}</div>
      <el-input
        v-model="appealForm.reason"
        type="textarea"
        :rows="4"
        placeholder="请说明该评价与事实不符的具体情况，平台核实后将做相应处理"
      />
      <template #footer>
        <el-button @click="appealVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAppeal">提交申诉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { shopApi, noteApi, appealApi } from '@/api'
import StarRating from '@/components/StarRating.vue'
import EChart from '@/components/EChart.vue'
import CommentSection from '@/components/CommentSection.vue'

const loading = ref(false)
const shops = ref([])
const notes = ref([])
const appeals = ref([])
const currentShopId = ref(null)
const expandedId = ref(null)

const currentShop = computed(() => shops.value.find((s) => s.id === currentShopId.value))
const shopAppeals = computed(() => appeals.value.filter((a) => a.shopId === currentShopId.value))

function parseImgs(s) {
  try { return JSON.parse(s || '[]') } catch (e) { return [] }
}
function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}
function isLowRating(n) {
  return n.rating && n.rating <= 2
}
function appealStatus(s) {
  const map = { 0: { t: '待处理', type: 'warning' }, 1: { t: '已受理', type: 'success' }, 2: { t: '已驳回', type: 'info' } }
  return map[s] || { t: '未知', type: 'info' }
}

// ---- 图表 ----
const ratingDistOption = computed(() => {
  const buckets = [0, 0, 0, 0, 0]
  notes.value.forEach((n) => {
    if (n.rating) {
      const idx = Math.min(4, Math.max(0, Math.round(n.rating) - 1))
      buckets[idx]++
    }
  })
  if (!buckets.some((v) => v > 0)) return null
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: ['1★', '2★', '3★', '4★', '5★'] },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar', data: buckets, barWidth: '48%',
      itemStyle: { color: '#ffa940', borderRadius: [6, 6, 0, 0] },
    }],
  }
})

const ratingTrendOption = computed(() => {
  const map = {}
  notes.value.forEach((n) => {
    if (!n.rating || !n.createTime) return
    const d = String(n.createTime).slice(0, 10)
    if (!map[d]) map[d] = { sum: 0, count: 0 }
    map[d].sum += n.rating
    map[d].count++
  })
  const dates = Object.keys(map).sort()
  if (!dates.length) return null
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 20, bottom: 44 },
    xAxis: { type: 'category', data: dates, axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', min: 0, max: 5 },
    series: [{
      type: 'line', smooth: true,
      data: dates.map((d) => +(map[d].sum / map[d].count).toFixed(2)),
      areaStyle: { color: 'rgba(255,106,61,0.12)' },
      itemStyle: { color: '#ff6a3d' },
      lineStyle: { color: '#ff6a3d' },
    }],
  }
})

// ---- 回复 ----
function toggleReply(n) {
  expandedId.value = expandedId.value === n.id ? null : n.id
}

// ---- 差评申诉 ----
const appealVisible = ref(false)
const appealForm = reactive({ noteId: null, noteTitle: '', reason: '' })

function openAppeal(n) {
  appealForm.noteId = n.id
  appealForm.noteTitle = n.title || n.content || ('#' + n.id)
  appealForm.reason = ''
  appealVisible.value = true
}
async function submitAppeal() {
  if (!appealForm.reason.trim()) return ElMessage.warning('请填写申诉理由')
  await appealApi.submit({ noteId: appealForm.noteId, reason: appealForm.reason.trim() })
  appealVisible.value = false
  ElMessage.success('申诉已提交，请等待平台处理')
  loadAppeals()
}

async function loadNotes() {
  if (!currentShopId.value) {
    notes.value = []
    return
  }
  const res = await noteApi.byShop(currentShopId.value)
  notes.value = res.data || []
}
async function loadAppeals() {
  try {
    const res = await appealApi.mine()
    appeals.value = res.data || []
  } catch (e) {
    appeals.value = []
  }
}
async function onShopChange() {
  expandedId.value = null
  await loadNotes()
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await shopApi.mine()
    shops.value = res.data || []
    if (shops.value.length) {
      currentShopId.value = shops.value[0].id
      await loadNotes()
    }
    loadAppeals()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.rating-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}
.stat-charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
.chart-card {
  padding: 16px;
}
.chart-title {
  font-weight: 700;
  margin-bottom: 8px;
}
.appeal-card {
  padding: 16px;
  margin-bottom: 16px;
}
.block-title {
  font-weight: 700;
  margin-bottom: 12px;
}
.appeal-item {
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
}
.appeal-item:last-child {
  border-bottom: none;
}
.appeal-reply {
  background: #f0f9eb;
  color: #529b2e;
  padding: 8px 10px;
  border-radius: 6px;
  font-size: 13px;
}
.review-item {
  display: flex;
  gap: 14px;
  padding: 16px 4px;
  border-bottom: 1px solid #f2f2f2;
}
.review-body {
  flex: 1;
  min-width: 0;
}
.review-title {
  font-weight: 600;
  margin-top: 4px;
}
.review-imgs {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
}
.review-imgs img {
  width: 90px;
  height: 90px;
  object-fit: cover;
  border-radius: 6px;
}
.review-foot {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.review-comments {
  margin-top: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}
.mb-8 {
  margin-bottom: 8px;
}
.mt-4 {
  margin-top: 4px;
}
</style>

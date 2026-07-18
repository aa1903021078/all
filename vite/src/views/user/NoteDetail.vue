<template>
  <div class="page-wrap" v-loading="loading">
    <template v-if="note.id">
      <div class="note-detail card">
        <!-- 图集 -->
        <div class="note-gallery" v-if="images.length">
          <img v-for="(img, i) in images" :key="i" :src="img" class="note-img" />
        </div>

        <div class="note-content">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-8">
              <el-avatar :size="42" :src="note.authorAvatar" />
              <div>
                <div class="bold">{{ note.authorName }}</div>
                <div class="muted text-sm">{{ formatTime(note.createTime) }}</div>
              </div>
            </div>
            <StarRating v-if="note.rating" :model-value="note.rating" :size="18" readonly show-score />
          </div>

          <h1 class="note-title mt-12" v-if="note.title">{{ note.title }}</h1>
          <div class="note-text">{{ note.content }}</div>

          <div v-if="note.shopId" class="shop-link card mt-16" @click="$router.push('/shops/' + note.shopId)">
            <img :src="note.shopCover" class="shop-link-cover" />
            <div class="flex-1">
              <div class="bold">📍 {{ note.shopName }}</div>
              <div class="muted text-sm">点击查看店铺详情</div>
            </div>
            <el-icon><ArrowRight /></el-icon>
          </div>

          <div class="action-bar mt-16">
            <el-button :type="note.liked ? 'danger' : 'default'" round @click="doLike">
              ❤️ {{ note.liked ? '已赞' : '点赞' }} {{ note.likeCount || 0 }}
            </el-button>
            <el-button round @click="scrollComment"><el-icon><ChatDotRound /></el-icon>&nbsp;评论 {{ note.commentCount || 0 }}</el-button>
          </div>
        </div>
      </div>

      <div ref="commentEl" class="card block mt-16" style="padding: 16px">
        <CommentSection target-type="NOTE" :target-id="note.id" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { noteApi } from '@/api'
import { useAuthStore } from '@/store/auth'
import StarRating from '@/components/StarRating.vue'
import CommentSection from '@/components/CommentSection.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const note = ref({})
const loading = ref(false)
const commentEl = ref(null)

const images = computed(() => {
  try {
    return JSON.parse(note.value.images || '[]')
  } catch (e) {
    return []
  }
})

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

async function load() {
  loading.value = true
  try {
    const res = await noteApi.detail(route.params.id)
    note.value = res.data || {}
  } finally {
    loading.value = false
  }
}

async function doLike() {
  if (!auth.isLoggedIn) {
    ElMessage.warning('请先登录')
    return router.push({ path: '/login', query: { redirect: route.fullPath } })
  }
  const res = await noteApi.like(note.value.id)
  note.value.liked = res.data
  note.value.likeCount = (note.value.likeCount || 0) + (res.data ? 1 : -1)
}

function scrollComment() {
  commentEl.value && commentEl.value.scrollIntoView({ behavior: 'smooth' })
}

onMounted(load)
</script>

<style scoped>
.note-detail {
  padding: 20px;
}
.note-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 8px;
  margin-bottom: 16px;
}
.note-img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
}
.note-title {
  margin: 0;
  font-size: 22px;
}
.note-text {
  margin-top: 12px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}
.shop-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  cursor: pointer;
  border: 1px solid var(--border);
}
.shop-link-cover {
  width: 54px;
  height: 54px;
  border-radius: 8px;
  object-fit: cover;
}
.action-bar {
  display: flex;
  gap: 12px;
}
</style>

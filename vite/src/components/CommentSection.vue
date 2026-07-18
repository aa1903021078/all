<template>
  <div class="comment-section">
    <div class="section-title">💬 评论 ({{ flatCount }})</div>
    <div class="comment-input card">
      <el-input
        v-model="text"
        type="textarea"
        :rows="2"
        :placeholder="auth.isLoggedIn ? '说点什么吧~' : '登录后参与评论'"
        :disabled="!auth.isLoggedIn"
      />
      <div class="flex justify-between mt-8">
        <span class="muted text-sm">{{ replyTo ? '回复 @' + replyTo.userName : '' }}</span>
        <div>
          <el-button v-if="replyTo" size="small" @click="cancelReply">取消回复</el-button>
          <el-button type="primary" size="small" :disabled="!auth.isLoggedIn" @click="submit">发表</el-button>
        </div>
      </div>
    </div>

    <div v-loading="loading" class="comment-list mt-12">
      <div v-for="c in comments" :key="c.id" class="comment-item">
        <el-avatar :size="38" :src="c.userAvatar" />
        <div class="comment-main">
          <div class="bold">{{ c.userName }}</div>
          <div class="comment-text">{{ c.content }}</div>
          <div class="comment-meta">
            <span>{{ formatTime(c.createTime) }}</span>
            <span class="pointer" @click="likeComment(c)">👍 {{ c.likeCount || 0 }}</span>
            <span class="pointer" @click="setReply(c)">回复</span>
            <span v-if="canDelete(c)" class="pointer danger" @click="delComment(c)">删除</span>
          </div>

          <!-- 子评论 -->
          <div v-for="child in c.children || []" :key="child.id" class="reply-item">
            <el-avatar :size="28" :src="child.userAvatar" />
            <div class="comment-main">
              <div class="bold text-sm">
                {{ child.userName }}
                <span v-if="child.replyUserName" class="muted">回复 @{{ child.replyUserName }}</span>
              </div>
              <div class="comment-text">{{ child.content }}</div>
              <div class="comment-meta">
                <span>{{ formatTime(child.createTime) }}</span>
                <span class="pointer" @click="likeComment(child)">👍 {{ child.likeCount || 0 }}</span>
                <span class="pointer" @click="setReply(c, child)">回复</span>
                <span v-if="canDelete(child)" class="pointer danger" @click="delComment(child)">删除</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && !comments.length" description="还没有评论,快来抢沙发" :image-size="70" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { commentApi } from '@/api'
import { useAuthStore } from '@/store/auth'

const props = defineProps({
  targetType: { type: String, required: true },
  targetId: { type: [Number, String], required: true },
})

const auth = useAuthStore()
const comments = ref([])
const text = ref('')
const loading = ref(false)
const replyTo = ref(null)
const replyParent = ref(null)

const flatCount = computed(() => {
  let n = comments.value.length
  comments.value.forEach((c) => (n += (c.children || []).length))
  return n
})

function canDelete(c) {
  return auth.isAdmin || (auth.user && auth.user.id === c.userId)
}

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

async function load() {
  loading.value = true
  try {
    const res = await commentApi.list(props.targetType, props.targetId)
    comments.value = res.data || []
  } finally {
    loading.value = false
  }
}

function setReply(parent, child) {
  replyParent.value = parent
  replyTo.value = child || parent
}
function cancelReply() {
  replyTo.value = null
  replyParent.value = null
}

async function submit() {
  if (!text.value.trim()) return ElMessage.warning('请输入内容')
  const payload = {
    targetType: props.targetType,
    targetId: Number(props.targetId),
    content: text.value.trim(),
    parentId: replyParent.value ? replyParent.value.id : 0,
    replyUserId: replyTo.value && replyParent.value ? replyTo.value.userId : null,
  }
  await commentApi.add(payload)
  text.value = ''
  cancelReply()
  ElMessage.success('评论成功')
  load()
}

async function likeComment(c) {
  if (!auth.isLoggedIn) return ElMessage.warning('请先登录')
  const res = await commentApi.like(c.id)
  c.likeCount = (c.likeCount || 0) + (res.data ? 1 : -1)
}

async function delComment(c) {
  await ElMessageBox.confirm('确定删除该评论?', '提示', { type: 'warning' })
  await commentApi.remove(c.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.comment-input {
  padding: 14px;
}
.comment-item {
  display: flex;
  gap: 12px;
  padding: 14px 4px;
  border-bottom: 1px solid #f2f2f2;
}
.comment-main {
  flex: 1;
  min-width: 0;
}
.comment-text {
  margin: 4px 0;
  color: #303133;
  line-height: 1.6;
}
.comment-meta {
  display: flex;
  gap: 16px;
  color: var(--text-muted);
  font-size: 12px;
}
.comment-meta .danger {
  color: #f56c6c;
}
.reply-item {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  padding: 8px;
  background: #f8f9fb;
  border-radius: 8px;
}
</style>

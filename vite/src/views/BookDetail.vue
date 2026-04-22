<template>
  <div class="yq-page max-w-[1100px] mx-auto" v-loading="loading">
    <div class="yq-card flex gap-6" v-if="book">
      <div class="w-[200px] aspect-[3/4] overflow-hidden rounded bg-gray-100 flex-shrink-0">
        <img :src="book.coverUrl" class="w-full h-full object-cover"
             @error="(e) => e.target.style.display='none'" />
      </div>
      <div class="flex-1">
        <h1 class="text-2xl font-bold mb-2">{{ book.name }}</h1>
        <div class="text-sm text-gray-500 mb-4">
          分类：{{ book.type }} · 热度 {{ book.heat || 0 }} · 评分 <span class="text-red-600">★ {{ book.rating || 0 }}</span>
        </div>
        <p class="text-gray-700 leading-relaxed mb-4">{{ book.description || '暂无简介' }}</p>
        <div class="flex gap-3">
          <el-button type="primary" @click="startReading">
            {{ chapters.length ? '开始阅读' : '暂无章节' }}
          </el-button>
          <el-button :type="isFav ? 'danger' : 'default'" @click="toggleFav">
            {{ isFav ? '已收藏 ❤' : '收藏' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 章节列表 -->
    <div class="yq-card mt-4">
      <div class="text-lg font-bold mb-3">章节目录（{{ chapters.length }}）</div>
      <div class="grid grid-cols-3 gap-2">
        <router-link v-for="c in chapters" :key="c.id"
                     :to="`/books/${book?.id}/read/${c.id}`"
                     class="p-2 hover:bg-gray-50 rounded text-sm truncate">
          {{ c.chapterNumber }}. {{ c.chapterTitle }}
        </router-link>
      </div>
    </div>

    <!-- 评论盖楼 -->
    <div class="yq-card mt-4">
      <div class="text-lg font-bold mb-3">评论（{{ commentCount }}）</div>
      <div class="flex gap-2 mb-4">
        <el-input v-model="commentText" type="textarea" :rows="2" placeholder="发表你的看法..." />
        <el-button type="primary" @click="submitComment(null)" :disabled="!commentText.trim()">发表</el-button>
      </div>
      <comment-tree :items="comments" @reply="submitComment" @delete="deleteC" :current-user-id="userStore.user?.userId" :is-admin="userStore.isAdmin" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBook, listChapters } from '@/api/book'
import { listComments, createComment, deleteComment, addFavorite, removeFavorite, checkFavorite } from '@/api/biz'
import { useUserStore } from '@/store/user'
import CommentTree from '@/components/CommentTree.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const book = ref(null)
const chapters = ref([])
const comments = ref([])
const isFav = ref(false)
const commentText = ref('')

const commentCount = computed(() => {
  let n = 0
  const count = (list) => {
    for (const c of list) {
      n++
      if (c.children?.length) count(c.children)
    }
  }
  count(comments.value)
  return n
})

async function load() {
  loading.value = true
  try {
    const id = route.params.id
    book.value = await getBook(id)
    chapters.value = await listChapters(id)
    comments.value = await listComments(id)
    try { isFav.value = await checkFavorite(id) } catch (e) { /* ignore */ }
  } finally {
    loading.value = false
  }
}

function startReading() {
  if (!chapters.value.length) return
  const first = chapters.value[0]
  router.push(`/books/${book.value.id}/read/${first.id}`)
}

async function toggleFav() {
  if (isFav.value) {
    await removeFavorite(book.value.id)
    isFav.value = false
    ElMessage.success('已取消收藏')
  } else {
    await addFavorite(book.value.id)
    isFav.value = true
    ElMessage.success('已加入收藏')
  }
}

async function submitComment(parentId, content) {
  const text = content ?? commentText.value
  if (!text?.trim()) return
  await createComment({ bookId: Number(route.params.id), content: text, parentId })
  if (!parentId) commentText.value = ''
  comments.value = await listComments(route.params.id)
  ElMessage.success('评论成功')
}

async function deleteC(id) {
  await ElMessageBox.confirm('确定删除该评论？', '提示', { type: 'warning' })
  await deleteComment(id)
  comments.value = await listComments(route.params.id)
}

onMounted(load)
</script>

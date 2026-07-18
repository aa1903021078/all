<template>
  <div class="page-wrap" v-loading="loading">
    <template v-if="recipe.id">
      <div class="recipe-head card">
        <img :src="recipe.cover" class="recipe-cover" />
        <div class="recipe-info">
          <h1 class="recipe-title">{{ recipe.title }}</h1>
          <div class="author flex items-center gap-8 mt-8">
            <el-avatar :size="34" :src="recipe.authorAvatar" />
            <span class="bold">{{ recipe.authorName }}</span>
            <span class="pill">{{ recipe.categoryName }}</span>
          </div>
          <div class="recipe-desc muted mt-12" v-html="recipe.description"></div>
          <div class="badges mt-12">
            <div class="badge-item">
              <div class="badge-num">{{ recipe.cookTime }}</div>
              <div class="muted text-sm">分钟</div>
            </div>
            <div class="badge-item">
              <div class="badge-num">{{ diffText }}</div>
              <div class="muted text-sm">难度</div>
            </div>
            <div class="badge-item">
              <div class="badge-num">{{ recipe.likeCount || 0 }}</div>
              <div class="muted text-sm">点赞</div>
            </div>
            <div class="badge-item">
              <div class="badge-num">{{ recipe.favoriteCount || 0 }}</div>
              <div class="muted text-sm">收藏</div>
            </div>
          </div>
          <div class="action-bar mt-16">
            <el-button :type="recipe.liked ? 'danger' : 'default'" @click="doLike">
              ❤️ {{ recipe.liked ? '已赞' : '点赞' }}
            </el-button>
            <el-button :type="recipe.favorited ? 'warning' : 'default'" plain @click="doFavorite">
              ⭐ {{ recipe.favorited ? '已收藏' : '收藏' }}
            </el-button>
            <el-button type="primary" @click="addToShopping">
              <el-icon><ShoppingCart /></el-icon>&nbsp;一键加入采购清单
            </el-button>
            <el-button type="success" plain @click="repostVisible = true">
              <el-icon><Camera /></el-icon>&nbsp;我复刻了
            </el-button>
          </div>
        </div>
      </div>

      <div class="recipe-body">
        <div class="left-col">
          <!-- 食材 -->
          <div class="card block">
            <div class="block-title">🧂 所需食材</div>
            <div class="ingredient" v-for="ing in recipe.ingredients" :key="ing.id">
              <span>{{ ing.name }}</span>
              <span class="muted">{{ ing.amount }}</span>
            </div>
            <el-empty v-if="!recipe.ingredients?.length" description="暂无食材" :image-size="60" />
          </div>
        </div>

        <div class="right-col">
          <!-- 步骤 -->
          <div class="card block">
            <div class="block-title">👩‍🍳 烹饪步骤</div>
            <div class="step" v-for="(s, i) in recipe.steps" :key="s.id">
              <div class="step-no">{{ i + 1 }}</div>
              <div class="step-body">
                <img v-if="s.image" :src="s.image" class="step-img" />
                <div class="step-text" v-html="s.content"></div>
              </div>
            </div>
            <el-empty v-if="!recipe.steps?.length" description="暂无步骤" :image-size="60" />
          </div>
        </div>
      </div>

      <!-- 复刻晒图 -->
      <div class="section-title mt-16">📸 大家的复刻 ({{ reposts.length }})</div>
      <div class="repost-list" v-if="reposts.length">
        <div v-for="r in reposts" :key="r.id" class="repost-card card">
          <img :src="r.image" class="repost-img" />
          <div class="repost-body">
            <div class="flex items-center gap-8">
              <el-avatar :size="24" :src="r.userAvatar" />
              <span class="text-sm bold">{{ r.userName }}</span>
            </div>
            <div class="text-sm muted mt-8 line-2">{{ r.content }}</div>
          </div>
        </div>
      </div>
      <el-empty v-else description="还没有人复刻,快来做第一个" :image-size="70" />

      <!-- 评论 -->
      <div class="card block mt-16" style="padding: 16px">
        <CommentSection target-type="RECIPE" :target-id="recipe.id" />
      </div>

      <!-- 同难度相关菜谱 -->
      <template v-if="similarRecipes.length">
        <div class="section-title mt-16">🍲 同难度相关菜谱</div>
        <div class="grid-cards">
          <RecipeCard v-for="r in similarRecipes" :key="r.id" :recipe="r" />
        </div>
      </template>
    </template>

    <!-- 复刻晒图弹窗 -->
    <el-dialog v-model="repostVisible" title="晒出我的复刻" width="460px">
      <ImageUpload v-model="repostForm.image" />
      <el-input v-model="repostForm.content" type="textarea" :rows="3" placeholder="分享你的复刻心得~" class="mt-12" />
      <template #footer>
        <el-button @click="repostVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRepost">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { recipeApi, shoppingApi } from '@/api'
import { useAuthStore } from '@/store/auth'
import CommentSection from '@/components/CommentSection.vue'
import ImageUpload from '@/components/ImageUpload.vue'
import RecipeCard from '@/components/RecipeCard.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const recipe = ref({})
const reposts = ref([])
const similarRecipes = ref([])
const loading = ref(false)
const repostVisible = ref(false)
const repostForm = reactive({ image: '', content: '' })

const diffText = computed(() => ({ 1: '简单', 2: '中等', 3: '困难' }[recipe.value.difficulty] || '简单'))

function requireLogin() {
  if (!auth.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return false
  }
  return true
}

async function load() {
  loading.value = true
  try {
    const id = route.params.id
    const res = await recipeApi.detail(id)
    recipe.value = res.data || {}
    recipeApi.reposts(id).then((r) => (reposts.value = r.data || []))
    loadSimilar(recipe.value.difficulty, recipe.value.id)
  } finally {
    loading.value = false
  }
}

// 同难度相关菜谱
async function loadSimilar(difficulty, id) {
  similarRecipes.value = []
  if (!difficulty) return
  try {
    const res = await recipeApi.page({ current: 1, size: 6, difficulty: difficulty, sort: 'hot' })
    const records = (res.data && res.data.records) || []
    similarRecipes.value = records.filter((r) => r.id !== id).slice(0, 4)
  } catch (e) {
    // 忽略推荐加载失败
  }
}

async function doLike() {
  if (!requireLogin()) return
  const res = await recipeApi.like(recipe.value.id)
  recipe.value.liked = res.data
  recipe.value.likeCount = (recipe.value.likeCount || 0) + (res.data ? 1 : -1)
}

async function doFavorite() {
  if (!requireLogin()) return
  const res = await recipeApi.favorite(recipe.value.id)
  recipe.value.favorited = res.data
  recipe.value.favoriteCount = (recipe.value.favoriteCount || 0) + (res.data ? 1 : -1)
  ElMessage.success(res.data ? '已收藏' : '已取消')
}

async function addToShopping() {
  if (!requireLogin()) return
  await shoppingApi.addFromRecipe(recipe.value.id)
  ElMessage.success('已将食材加入采购清单')
  router.push('/shopping')
}

async function submitRepost() {
  if (!repostForm.image) return ElMessage.warning('请上传复刻图片')
  await recipeApi.addRepost(recipe.value.id, { ...repostForm })
  repostVisible.value = false
  repostForm.image = ''
  repostForm.content = ''
  ElMessage.success('晒图成功')
  recipeApi.reposts(recipe.value.id).then((r) => (reposts.value = r.data || []))
}

onMounted(load)
</script>

<style scoped>
.recipe-head {
  display: flex;
  gap: 24px;
  padding: 20px;
}
.recipe-cover {
  width: 340px;
  height: 260px;
  object-fit: cover;
  border-radius: 10px;
  flex-shrink: 0;
}
.recipe-info {
  flex: 1;
  min-width: 0;
}
.recipe-title {
  margin: 0;
  font-size: 26px;
}
.badges {
  display: flex;
  gap: 26px;
}
.badge-item {
  text-align: center;
}
.badge-num {
  font-size: 20px;
  font-weight: 800;
  color: var(--brand);
}
.action-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.recipe-body {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
  margin-top: 16px;
}
.block {
  padding: 16px;
}
.block-title {
  font-weight: 700;
  font-size: 16px;
  margin-bottom: 14px;
}
.ingredient {
  display: flex;
  justify-content: space-between;
  padding: 10px 6px;
  border-bottom: 1px dashed #eee;
}
.step {
  display: flex;
  gap: 14px;
  margin-bottom: 20px;
}
.step-no {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--brand);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  flex-shrink: 0;
}
.step-body {
  flex: 1;
}
.step-img {
  width: 100%;
  max-width: 380px;
  border-radius: 8px;
  margin-bottom: 8px;
}
.step-text {
  line-height: 1.7;
}
.repost-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 14px;
}
.repost-img {
  width: 100%;
  height: 150px;
  object-fit: cover;
}
.repost-body {
  padding: 10px;
}
@media (max-width: 800px) {
  .recipe-head,
  .recipe-body {
    flex-direction: column;
    grid-template-columns: 1fr;
  }
  .recipe-cover {
    width: 100%;
  }
}
</style>

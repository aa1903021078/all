<template>
  <div class="page-wrap">
    <div class="section-title">✍️ 发布内容</div>
    <el-tabs v-model="activeTab" class="card publish-tabs">
      <!-- 探店笔记 -->
      <el-tab-pane label="📝 探店笔记" name="note">
        <el-form :model="noteForm" label-width="90px" style="max-width: 720px">
          <el-form-item label="关联店铺">
            <el-select
              v-model="noteForm.shopId"
              filterable
              remote
              :remote-method="searchShops"
              placeholder="搜索并选择店铺(选填)"
              style="width: 100%"
              clearable
            >
              <el-option v-for="s in shopOptions" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="noteForm.title" placeholder="给你的探店起个标题" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="评分" v-if="noteForm.shopId">
            <StarRating v-model="noteForm.rating" :size="26" />
          </el-form-item>
          <el-form-item label="正文">
            <RichEditor v-model="noteForm.content" placeholder="分享你的探店体验、推荐菜、避雷点...(支持图文)" />
          </el-form-item>
          <el-form-item label="图片">
            <ImageUpload v-model="noteForm.imageList" multiple :limit="9" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="submitNote">发布笔记</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 菜谱 -->
      <el-tab-pane label="🍲 分享菜谱" name="recipe">
        <el-form :model="recipeForm" label-width="90px" style="max-width: 720px">
          <el-form-item label="菜谱名称">
            <el-input v-model="recipeForm.title" placeholder="如:番茄炒蛋" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="封面图">
            <ImageUpload v-model="recipeForm.cover" />
          </el-form-item>
          <el-form-item label="菜系">
            <el-select v-model="recipeForm.categoryId" placeholder="选择菜系" style="width: 220px">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="烹饪时长">
            <el-input-number v-model="recipeForm.cookTime" :min="1" :max="600" /> <span class="muted">&nbsp;分钟</span>
          </el-form-item>
          <el-form-item label="难度">
            <el-radio-group v-model="recipeForm.difficulty">
              <el-radio-button :value="1">简单</el-radio-button>
              <el-radio-button :value="2">中等</el-radio-button>
              <el-radio-button :value="3">困难</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="简介">
            <el-input v-model="recipeForm.description" type="textarea" :rows="2" placeholder="一句话介绍这道菜" />
          </el-form-item>

          <el-form-item label="食材清单">
            <div class="dyn-list">
              <div v-for="(ing, i) in recipeForm.ingredients" :key="i" class="dyn-row">
                <el-input v-model="ing.name" placeholder="食材(如:鸡蛋)" style="width: 200px" />
                <el-input v-model="ing.amount" placeholder="用量(如:2个)" style="width: 160px" />
                <el-button circle @click="recipeForm.ingredients.splice(i, 1)"><el-icon><Delete /></el-icon></el-button>
              </div>
              <el-button type="primary" plain @click="recipeForm.ingredients.push({ name: '', amount: '' })">
                <el-icon><Plus /></el-icon>&nbsp;添加食材
              </el-button>
            </div>
          </el-form-item>

          <el-form-item label="烹饪步骤">
            <div class="dyn-list">
              <div v-for="(step, i) in recipeForm.steps" :key="i" class="step-row card">
                <div class="flex items-center justify-between mb-8">
                  <span class="bold">步骤 {{ i + 1 }}</span>
                  <el-button size="small" text type="danger" @click="recipeForm.steps.splice(i, 1)">删除</el-button>
                </div>
                <RichEditor v-model="step.content" placeholder="描述这一步的操作" min-height="110px" class="mb-8" />
                <ImageUpload v-model="step.image" />
              </div>
              <el-button type="primary" plain @click="recipeForm.steps.push({ content: '', image: '' })">
                <el-icon><Plus /></el-icon>&nbsp;添加步骤
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="submitRecipe">发布菜谱</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { shopApi, recipeApi, noteApi, categoryApi } from '@/api'
import StarRating from '@/components/StarRating.vue'
import ImageUpload from '@/components/ImageUpload.vue'
import RichEditor from '@/components/RichEditor.vue'

function isEmptyHtml(html) {
  if (!html) return true
  return !html.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').trim()
}

const route = useRoute()
const router = useRouter()

const activeTab = ref('note')
const submitting = ref(false)
const categories = ref([])
const shopOptions = ref([])

const noteForm = reactive({ shopId: null, title: '', content: '', rating: 5, imageList: [] })
const recipeForm = reactive({
  title: '', cover: '', categoryId: null, cookTime: 30, difficulty: 1, description: '',
  ingredients: [{ name: '', amount: '' }],
  steps: [{ content: '', image: '' }],
})

async function searchShops(kw) {
  const res = await shopApi.page({ current: 1, size: 20, keyword: kw })
  shopOptions.value = (res.data && res.data.records) || []
}

async function submitNote() {
  if (isEmptyHtml(noteForm.content)) return ElMessage.warning('请填写笔记正文')
  submitting.value = true
  try {
    await noteApi.create({
      shopId: noteForm.shopId || null,
      title: noteForm.title,
      content: noteForm.content,
      rating: noteForm.shopId ? noteForm.rating : null,
      images: JSON.stringify(noteForm.imageList),
    })
    ElMessage.success('笔记发布成功')
    router.push('/profile')
  } finally {
    submitting.value = false
  }
}

async function submitRecipe() {
  if (!recipeForm.title.trim()) return ElMessage.warning('请填写菜谱名称')
  if (!recipeForm.cover) return ElMessage.warning('请上传封面图')
  const ingredients = recipeForm.ingredients.filter((i) => i.name.trim())
  const steps = recipeForm.steps.filter((s) => !isEmptyHtml(s.content) || s.image)
  if (!ingredients.length) return ElMessage.warning('至少添加一个食材')
  if (!steps.length) return ElMessage.warning('至少添加一个步骤')
  submitting.value = true
  try {
    await recipeApi.create({
      title: recipeForm.title,
      cover: recipeForm.cover,
      categoryId: recipeForm.categoryId,
      cookTime: recipeForm.cookTime,
      difficulty: recipeForm.difficulty,
      description: recipeForm.description,
      ingredients: ingredients.map((it, idx) => ({ ...it, sort: idx })),
      steps: steps.map((it, idx) => ({ ...it, stepNo: idx + 1, sort: idx })),
    })
    ElMessage.success('菜谱发布成功')
    router.push('/profile')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  categoryApi.list().then((res) => (categories.value = res.data || []))
  searchShops('')
  if (route.query.type === 'recipe') activeTab.value = 'recipe'
  if (route.query.shopId) {
    noteForm.shopId = Number(route.query.shopId)
    activeTab.value = 'note'
  }
})
</script>

<style scoped>
.publish-tabs {
  padding: 16px 20px;
}
.dyn-list {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.dyn-row {
  display: flex;
  gap: 10px;
  align-items: center;
}
.step-row {
  padding: 12px;
  border: 1px solid var(--border);
  box-shadow: none;
}
</style>

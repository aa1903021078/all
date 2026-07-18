<template>
  <div>
    <el-tabs v-model="activeTab" class="card ops-tabs">
      <!-- 菜系管理 -->
      <el-tab-pane label="🍴 菜系管理" name="category">
        <div class="flex justify-end mb-12">
          <el-button type="primary" size="small" @click="openCat()"><el-icon><Plus /></el-icon>&nbsp;新增菜系</el-button>
        </div>
        <el-table :data="categories" size="default">
          <el-table-column label="图标" width="80">
            <template #default="{ row }"><span style="font-size: 22px">{{ row.icon }}</span></template>
          </el-table-column>
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="sort" label="排序" width="100" />
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button size="small" text @click="openCat(row)">编辑</el-button>
              <el-button size="small" text type="danger" @click="delCat(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 推荐位配置 -->
      <el-tab-pane label="⭐ 推荐位配置" name="recommend">
        <el-radio-group v-model="recType" class="mb-12" @change="loadRecommend">
          <el-radio-button value="shop">店铺推荐</el-radio-button>
          <el-radio-button value="recipe">菜谱推荐</el-radio-button>
        </el-radio-group>
        <el-table :data="recList" v-loading="recLoading">
          <el-table-column label="内容" min-width="240">
            <template #default="{ row }">
              <div class="flex items-center gap-8">
                <img :src="row.cover" class="mini-cover" />
                <span class="bold">{{ row.name || row.title }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="热度" width="140">
            <template #default="{ row }">
              <span class="muted text-sm">👀 {{ row.viewCount || 0 }} ❤️ {{ row.likeCount || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="首页推荐" width="120">
            <template #default="{ row }">
              <el-switch :model-value="row.recommend === 1" @change="(v) => toggleRec(row, v)" />
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 热门搜索 -->
      <el-tab-pane label="🔥 热门搜索词" name="keyword">
        <div class="keyword-cloud">
          <div v-for="k in keywords" :key="k.id" class="kw-item">
            <span class="kw-text">{{ k.keyword }}</span>
            <el-tag size="small" type="warning" effect="plain">{{ k.count }}次</el-tag>
          </div>
          <el-empty v-if="!keywords.length" description="暂无搜索数据" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 菜系表单 -->
    <el-dialog v-model="catVisible" :title="catForm.id ? '编辑菜系' : '新增菜系'" width="380px">
      <el-form :model="catForm" label-width="70px">
        <el-form-item label="名称"><el-input v-model="catForm.name" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="catForm.icon" placeholder="emoji 如 🌶️" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="catForm.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="catVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCat">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { categoryApi, shopApi, recipeApi, searchApi } from '@/api'

const activeTab = ref('category')
const categories = ref([])
const keywords = ref([])

const catVisible = ref(false)
const catForm = reactive({})

const recType = ref('shop')
const recList = ref([])
const recLoading = ref(false)

async function loadCategories() {
  categories.value = (await categoryApi.list()).data || []
}
function openCat(row) {
  if (row) Object.assign(catForm, { ...row })
  else Object.assign(catForm, { id: null, name: '', icon: '', sort: 0 })
  catVisible.value = true
}
async function saveCat() {
  if (!catForm.name) return ElMessage.warning('请填写名称')
  if (catForm.id) await categoryApi.update(catForm.id, { ...catForm })
  else await categoryApi.save({ ...catForm })
  ElMessage.success('已保存')
  catVisible.value = false
  loadCategories()
}
async function delCat(row) {
  await ElMessageBox.confirm('确定删除该菜系?', '提示', { type: 'warning' })
  await categoryApi.remove(row.id)
  ElMessage.success('已删除')
  loadCategories()
}

async function loadRecommend() {
  recLoading.value = true
  try {
    if (recType.value === 'shop') {
      const res = await shopApi.adminPage({ current: 1, size: 50, status: 1 })
      recList.value = (res.data && res.data.records) || []
    } else {
      const res = await recipeApi.adminPage({ current: 1, size: 50, status: 1 })
      recList.value = (res.data && res.data.records) || []
    }
  } finally {
    recLoading.value = false
  }
}
async function toggleRec(row, v) {
  const val = v ? 1 : 0
  if (recType.value === 'shop') await shopApi.setRecommend(row.id, val)
  else await recipeApi.setRecommend(row.id, val)
  row.recommend = val
  ElMessage.success('已更新')
}

onMounted(() => {
  loadCategories()
  loadRecommend()
  searchApi.hot(30).then((res) => (keywords.value = res.data || []))
})
</script>

<style scoped>
.ops-tabs {
  padding: 6px 20px 20px;
}
.mini-cover {
  width: 42px;
  height: 42px;
  border-radius: 6px;
  object-fit: cover;
}
.keyword-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.kw-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: #f7f8fa;
  border-radius: 20px;
}
.kw-text {
  font-weight: 600;
}
</style>

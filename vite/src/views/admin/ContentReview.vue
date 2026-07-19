<template>
  <div>
    <el-tabs v-model="activeTab" class="card content-tabs" @tab-change="reload">
      <el-tab-pane label="📝 探店笔记" name="note" />
      <el-tab-pane label="🍲 分享菜谱" name="recipe" />
      <el-tab-pane label="🛡️ 差评申诉" name="appeal" />
    </el-tabs>

    <template v-if="activeTab !== 'appeal'">
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
        <el-table-column label="审核操作" width="430" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="preview(row)">查看</el-button>
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status !== 1" size="small" type="success" @click="review(row, 1)">通过</el-button>
            <el-button v-if="row.status !== 3" size="small" type="danger" @click="review(row, 3)">违规</el-button>
            <el-button v-if="row.status === 1" size="small" type="warning" plain @click="review(row, 2)">下架</el-button>
            <el-button size="small" type="danger" plain @click="removeItem(row)">删除</el-button>
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
    </template>

    <!-- 差评申诉处理 -->
    <template v-if="activeTab === 'appeal'">
      <div class="card filter-bar mt-16">
        <el-select v-model="appealStatus" placeholder="申诉状态" clearable style="width: 150px" @change="loadAppeals">
          <el-option label="待处理" :value="0" />
          <el-option label="已受理" :value="1" />
          <el-option label="已驳回" :value="2" />
        </el-select>
        <el-button type="primary" @click="loadAppeals"><el-icon><Search /></el-icon>&nbsp;查询</el-button>
        <el-tag type="warning" class="ml-auto" v-if="appeals.some((a) => a.status === 0)">有待处理申诉</el-tag>
      </div>
      <div class="card mt-16" style="padding: 16px" v-loading="appealLoading">
        <el-table :data="appeals" style="width: 100%">
          <el-table-column label="申诉店铺" min-width="130">
            <template #default="{ row }">
              <div class="bold">{{ row.shopName }}</div>
              <div class="muted text-sm">商家: {{ row.merchantName }}</div>
            </template>
          </el-table-column>
          <el-table-column label="被申诉评价" min-width="240">
            <template #default="{ row }">
              <div class="bold line-1">{{ row.noteTitle || '(无标题)' }}</div>
              <div class="muted text-sm line-2">{{ row.noteContent }}</div>
              <StarRating v-if="row.noteRating" :model-value="row.noteRating" :size="13" readonly />
            </template>
          </el-table-column>
          <el-table-column label="申诉理由" min-width="200">
            <template #default="{ row }">
              <div class="line-2">{{ row.reason }}</div>
              <div v-if="row.reply" class="muted text-sm mt-4">回复: {{ row.reply }}</div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="appealTag(row.status).type">{{ appealTag(row.status).t }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="处理" width="170" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 0">
                <el-button size="small" type="success" @click="openHandle(row, 1)">受理</el-button>
                <el-button size="small" type="info" plain @click="openHandle(row, 2)">驳回</el-button>
              </template>
              <span v-else class="muted text-sm">已处理</span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!appealLoading && !appeals.length" description="暂无申诉" />
      </div>
    </template>

    <!-- 申诉处理弹窗 -->
    <el-dialog v-model="handleVisible" :title="handleForm.status === 1 ? '受理申诉' : '驳回申诉'" width="440px">
      <el-input v-model="handleForm.reply" type="textarea" :rows="3" placeholder="填写处理说明(可选), 将反馈给商家" />
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确定</el-button>
      </template>
    </el-dialog>

    <!-- 内容编辑弹窗 -->
    <el-dialog
      v-model="editVisible"
      :title="editForm.type === 'note' ? '编辑探店笔记' : '编辑分享菜谱'"
      width="600px"
    >
      <el-form label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" placeholder="请输入标题" maxlength="60" show-word-limit />
        </el-form-item>

        <template v-if="editForm.type === 'note'">
          <el-form-item label="正文">
            <el-input v-model="editForm.content" type="textarea" :rows="5" placeholder="请输入正文内容" />
          </el-form-item>
          <el-form-item label="评分">
            <StarRating v-model="editForm.rating" :size="24" show-score />
          </el-form-item>
          <el-form-item label="图集">
            <ImageUpload v-model="editForm.imageList" multiple />
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="封面图">
            <ImageUpload v-model="editForm.cover" />
          </el-form-item>
          <el-form-item label="简介">
            <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入菜谱简介" />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="editForm.categoryId" placeholder="选择分类" style="width: 100%">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="烹饪时长">
            <el-input-number v-model="editForm.cookTime" :min="1" :max="600" />
            <span class="muted text-sm">&nbsp;分钟</span>
          </el-form-item>
          <el-form-item label="难度">
            <el-select v-model="editForm.difficulty" style="width: 140px">
              <el-option label="简单" :value="1" />
              <el-option label="中等" :value="2" />
              <el-option label="困难" :value="3" />
            </el-select>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { noteApi, recipeApi, appealApi, categoryApi } from '@/api'
import StarRating from '@/components/StarRating.vue'
import ImageUpload from '@/components/ImageUpload.vue'

const router = useRouter()
const activeTab = ref('note')
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ current: 1, size: 10, keyword: '', status: null })

// ---- 内容编辑 ----
const categories = ref([])
const editVisible = ref(false)
const editSaving = ref(false)
const editForm = reactive({
  id: null,
  type: 'note',
  title: '',
  content: '',
  rating: 5,
  imageList: [],
  cover: '',
  description: '',
  categoryId: null,
  cookTime: null,
  difficulty: 1,
})
function openEdit(row) {
  editForm.id = row.id
  editForm.type = activeTab.value
  editForm.title = row.title || ''
  if (activeTab.value === 'note') {
    editForm.content = row.content || ''
    editForm.rating = row.rating || 5
    try {
      editForm.imageList = JSON.parse(row.images || '[]')
    } catch (e) {
      editForm.imageList = []
    }
  } else {
    editForm.cover = row.cover || ''
    editForm.description = row.description || ''
    editForm.categoryId = row.categoryId || null
    editForm.cookTime = row.cookTime || null
    editForm.difficulty = row.difficulty || 1
  }
  editVisible.value = true
}
async function saveEdit() {
  if (!editForm.title.trim()) {
    ElMessage.warning('标题不能为空')
    return
  }
  editSaving.value = true
  try {
    if (editForm.type === 'note') {
      await noteApi.adminUpdate(editForm.id, {
        title: editForm.title,
        content: editForm.content,
        rating: editForm.rating,
        images: JSON.stringify(editForm.imageList || []),
      })
    } else {
      await recipeApi.adminUpdate(editForm.id, {
        title: editForm.title,
        cover: editForm.cover,
        description: editForm.description,
        categoryId: editForm.categoryId,
        cookTime: editForm.cookTime,
        difficulty: editForm.difficulty,
      })
    }
    editVisible.value = false
    ElMessage.success('保存成功')
    load()
  } finally {
    editSaving.value = false
  }
}

// ---- 差评申诉 ----
const appeals = ref([])
const appealLoading = ref(false)
const appealStatus = ref(null)
const handleVisible = ref(false)
const handleForm = reactive({ id: null, status: 1, reply: '' })

function appealTag(s) {
  const map = { 0: { t: '待处理', type: 'warning' }, 1: { t: '已受理', type: 'success' }, 2: { t: '已驳回', type: 'info' } }
  return map[s] || { t: '未知', type: 'info' }
}
async function loadAppeals() {
  appealLoading.value = true
  try {
    const res = await appealApi.adminList(appealStatus.value)
    appeals.value = res.data || []
  } finally {
    appealLoading.value = false
  }
}
function openHandle(row, status) {
  handleForm.id = row.id
  handleForm.status = status
  handleForm.reply = ''
  handleVisible.value = true
}
async function submitHandle() {
  await appealApi.handle(handleForm.id, handleForm.status, handleForm.reply)
  handleVisible.value = false
  ElMessage.success('处理完成')
  loadAppeals()
}

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
  if (activeTab.value === 'appeal') {
    loadAppeals()
    return
  }
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

async function removeItem(row) {
  const label = activeTab.value === 'note' ? '笔记' : '菜谱'
  await ElMessageBox.confirm(`确定删除该${label}「${row.title || '无标题'}」吗? 此操作不可恢复`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })
  if (activeTab.value === 'note') {
    await noteApi.adminRemove(row.id)
  } else {
    await recipeApi.adminRemove(row.id)
  }
  ElMessage.success('删除成功')
  load()
}

function preview(row) {
  const path = activeTab.value === 'note' ? '/notes/' : '/recipes/'
  const url = router.resolve(path + row.id).href
  window.open(url, '_blank')
}

onMounted(() => {
  load()
  categoryApi.list().then((res) => (categories.value = res.data || []))
})
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

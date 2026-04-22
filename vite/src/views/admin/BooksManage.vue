<template>
  <div class="yq-card">
    <div class="flex items-center justify-between mb-4">
      <el-input v-model="keyword" placeholder="搜索书名" clearable class="max-w-[260px]" @keyup.enter="reload" />
      <el-button type="primary" @click="openEdit(null)">+ 新增图书</el-button>
    </div>
    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <img v-if="row.coverUrl" :src="row.coverUrl" class="w-10 h-14 object-cover" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="书名" />
      <el-table-column prop="type" label="分类" width="120" />
      <el-table-column prop="heat" label="热度" width="100" />
      <el-table-column prop="rating" label="评分" width="100" />
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="primary" @click="openChapters(row)">章节</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="flex justify-center mt-4">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total"
                     layout="prev, pager, next" @current-change="load" />
    </div>

    <!-- 编辑图书 -->
    <el-dialog v-model="editVisible" :title="form.id ? '编辑图书' : '新增图书'" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="书名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="form.type" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="封面URL"><el-input v-model="form.coverUrl" /></el-form-item>
        <el-form-item label="热度"><el-input-number v-model="form.heat" :min="0" /></el-form-item>
        <el-form-item label="评分"><el-input-number v-model="form.rating" :precision="1" :step="0.1" :min="0" :max="10" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 章节管理 -->
    <el-dialog v-model="chapterVisible" :title="`章节管理 - ${current?.name}`" width="800px">
      <div class="mb-3">
        <el-button type="primary" @click="openChapterEdit(null)">+ 新增章节</el-button>
      </div>
      <el-table :data="chapters" border max-height="400">
        <el-table-column prop="chapterNumber" label="序号" width="80" />
        <el-table-column prop="chapterTitle" label="标题" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="openChapterEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="removeChapterRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="chapterEditVisible" :title="chapterForm.id ? '编辑章节' : '新增章节'" width="700px">
      <el-form :model="chapterForm" label-width="80px">
        <el-form-item label="序号"><el-input-number v-model="chapterForm.chapterNumber" :min="1" /></el-form-item>
        <el-form-item label="标题"><el-input v-model="chapterForm.chapterTitle" /></el-form-item>
        <el-form-item label="正文"><el-input v-model="chapterForm.content" type="textarea" :rows="10" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="chapterEditVisible = false">取消</el-button>
        <el-button type="primary" @click="saveChapterAction">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageBooks, saveBook, removeBook, listChapters, saveChapter, removeChapter } from '@/api/book'

const list = ref([]); const total = ref(0); const page = ref(1); const size = 10
const keyword = ref('')
function reload() { page.value = 1; load() }
async function load() {
  const d = await pageBooks({ page: page.value, size, keyword: keyword.value })
  list.value = d.records; total.value = d.total
}

const editVisible = ref(false)
const form = ref({})
function openEdit(row) {
  form.value = row ? { ...row } : { name: '', type: '', description: '', coverUrl: '', heat: 0, rating: 0 }
  editVisible.value = true
}
async function save() {
  await saveBook(form.value)
  editVisible.value = false
  ElMessage.success('保存成功')
  load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确认删除「${row.name}」？`, '警告', { type: 'warning' })
  await removeBook(row.id)
  ElMessage.success('已删除')
  load()
}

const chapterVisible = ref(false)
const current = ref(null)
const chapters = ref([])
async function openChapters(row) {
  current.value = row
  chapters.value = await listChapters(row.id)
  chapterVisible.value = true
}

const chapterEditVisible = ref(false)
const chapterForm = ref({})
function openChapterEdit(row) {
  chapterForm.value = row
    ? { ...row }
    : { bookId: current.value.id, chapterNumber: (chapters.value.length + 1), chapterTitle: '', content: '' }
  chapterEditVisible.value = true
}
async function saveChapterAction() {
  await saveChapter(chapterForm.value)
  chapterEditVisible.value = false
  chapters.value = await listChapters(current.value.id)
  ElMessage.success('保存成功')
}
async function removeChapterRow(row) {
  await ElMessageBox.confirm('确定删除该章节？', '警告', { type: 'warning' })
  await removeChapter(row.id)
  chapters.value = await listChapters(current.value.id)
  ElMessage.success('已删除')
}

onMounted(load)
</script>

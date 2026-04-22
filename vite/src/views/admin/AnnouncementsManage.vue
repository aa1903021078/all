<template>
  <div class="yq-card">
    <div class="mb-4"><el-button type="primary" @click="openEdit(null)">+ 新增公告</el-button></div>
    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="author" label="作者" width="120" />
      <el-table-column prop="priority" label="优先级" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success">已发布</el-tag>
          <el-tag v-else-if="row.status === 2" type="info">下架</el-tag>
          <el-tag v-else>草稿</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="flex justify-center mt-4">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total"
                     layout="prev, pager, next" @current-change="load" />
    </div>

    <el-dialog v-model="editVisible" :title="form.id ? '编辑公告' : '新增公告'" width="560px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="作者"><el-input v-model="form.author" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="优先级"><el-input-number v-model="form.priority" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option :value="0" label="草稿" />
            <el-option :value="1" label="已发布" />
            <el-option :value="2" label="下架" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminPageAnnouncements, saveAnnouncement, removeAnnouncement } from '@/api/biz'

const list = ref([]); const total = ref(0); const page = ref(1); const size = 10
async function load() {
  const d = await adminPageAnnouncements({ page: page.value, size })
  list.value = d.records; total.value = d.total
}
const editVisible = ref(false); const form = ref({})
function openEdit(row) {
  form.value = row ? { ...row } : { title: '', content: '', author: 'admin', status: 1, priority: 0 }
  editVisible.value = true
}
async function save() {
  await saveAnnouncement(form.value); editVisible.value = false; ElMessage.success('保存成功'); load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确认删除「${row.title}」？`, '警告', { type: 'warning' })
  await removeAnnouncement(row.id); ElMessage.success('已删除'); load()
}
onMounted(load)
</script>

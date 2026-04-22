<template>
  <div class="yq-card">
    <div class="mb-4"><el-button type="primary" @click="openEdit(null)">+ 新增音乐</el-button></div>
    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <img v-if="row.coverUrl" :src="row.coverUrl" class="w-12 h-12 rounded" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="歌曲" />
      <el-table-column prop="artist" label="歌手" width="160" />
      <el-table-column prop="album" label="专辑" width="160" />
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

    <el-dialog v-model="editVisible" :title="form.id ? '编辑' : '新增'" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="歌名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="歌手"><el-input v-model="form.artist" /></el-form-item>
        <el-form-item label="专辑"><el-input v-model="form.album" /></el-form-item>
        <el-form-item label="封面URL"><el-input v-model="form.coverUrl" /></el-form-item>
        <el-form-item label="文件URL"><el-input v-model="form.fileUrl" /></el-form-item>
        <el-form-item label="时长(秒)"><el-input-number v-model="form.duration" :min="0" /></el-form-item>
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
import { adminPageSongs, saveSong, removeSong } from '@/api/biz'
const list = ref([]); const total = ref(0); const page = ref(1); const size = 10
async function load() {
  const d = await adminPageSongs({ page: page.value, size }); list.value = d.records; total.value = d.total
}
const editVisible = ref(false); const form = ref({})
function openEdit(row) {
  form.value = row ? { ...row } : { name: '', artist: '', album: '', coverUrl: '', fileUrl: '', duration: 0 }
  editVisible.value = true
}
async function save() {
  await saveSong(form.value); editVisible.value = false; ElMessage.success('保存成功'); load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确认删除「${row.name}」？`, '警告', { type: 'warning' })
  await removeSong(row.id); ElMessage.success('已删除'); load()
}
onMounted(load)
</script>

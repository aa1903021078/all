<template>
  <div>
    <el-button type="primary" @click="showDialog()" style="margin-bottom:16px">发布公告</el-button>
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" width="200" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="createTime" label="发布时间" width="160" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="showDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '发布公告'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAnnouncementList, addAnnouncement, updateAnnouncement, deleteAnnouncement } from '../../api'

const tableData = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, title: '', content: '' })

const loadData = async () => {
  const res = await getAnnouncementList()
  if (res.code === 200) tableData.value = res.data
}

const showDialog = (row) => {
  if (row) {
    form.id = row.id; form.title = row.title; form.content = row.content
  } else {
    form.id = null; form.title = ''; form.content = ''
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (form.id) {
    await updateAnnouncement(form)
    ElMessage.success('修改成功')
  } else {
    await addAnnouncement(form)
    ElMessage.success('发布成功')
  }
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该公告？')
  await deleteAnnouncement(row.id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

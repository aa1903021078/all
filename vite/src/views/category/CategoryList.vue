<template>
  <div>
    <el-button type="primary" @click="showDialog()" style="margin-bottom:16px">添加分类</el-button>
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="showDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '添加分类'" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
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
import { getCategoryList, addCategory, updateCategory, deleteCategory } from '../../api'

const tableData = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, name: '', sort: 0 })

const loadData = async () => {
  const res = await getCategoryList()
  if (res.code === 200) tableData.value = res.data
}

const showDialog = (row) => {
  if (row) {
    form.id = row.id; form.name = row.name; form.sort = row.sort
  } else {
    form.id = null; form.name = ''; form.sort = 0
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (form.id) {
    await updateCategory(form)
    ElMessage.success('修改成功')
  } else {
    await addCategory(form)
    ElMessage.success('添加成功')
  }
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该分类？')
  await deleteCategory(row.id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

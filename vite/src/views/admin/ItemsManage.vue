<template>
  <div class="yq-card">
    <div class="mb-4"><el-button type="primary" @click="openEdit(null)">+ 新增商品</el-button></div>
    <el-table :data="list" border>
      <el-table-column prop="itemId" label="ID" width="70" />
      <el-table-column label="图片" width="80">
        <template #default="{ row }">
          <img v-if="row.photo" :src="row.photo" class="w-14 h-14 object-cover" />
        </template>
      </el-table-column>
      <el-table-column prop="itemName" label="名称" />
      <el-table-column prop="price" label="价格" width="120">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="100" />
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

    <el-dialog v-model="editVisible" :title="form.itemId ? '编辑商品' : '新增商品'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.itemName" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :precision="2" :step="1" :min="0" /></el-form-item>
        <el-form-item label="库存"><el-input v-model="form.stock" /></el-form-item>
        <el-form-item label="图片URL"><el-input v-model="form.photo" /></el-form-item>
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
import { pageItems, saveItem, removeItem } from '@/api/biz'

const list = ref([]); const total = ref(0); const page = ref(1); const size = 10
async function load() {
  const d = await pageItems({ page: page.value, size }); list.value = d.records; total.value = d.total
}
const editVisible = ref(false); const form = ref({})
function openEdit(row) {
  form.value = row ? { ...row } : { itemName: '', price: 0, stock: '0', photo: '' }
  editVisible.value = true
}
async function save() {
  await saveItem(form.value); editVisible.value = false; ElMessage.success('保存成功'); load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确认删除「${row.itemName}」？`, '警告', { type: 'warning' })
  await removeItem(row.itemId); ElMessage.success('已删除'); load()
}
onMounted(load)
</script>

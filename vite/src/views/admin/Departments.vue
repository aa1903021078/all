<template>
  <el-card class="card-shadow !border-0">
    <template #header>
      <div class="flex justify-between items-center">
        <span class="font-medium">科室管理</span>
        <div class="space-x-2">
          <el-select v-model="filter" placeholder="筛选医院" clearable @change="load" style="width:200px">
            <el-option v-for="h in hospitals" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
          <el-button type="primary" :icon="Plus" @click="openEdit(null)">新增科室</el-button>
        </div>
      </div>
    </template>
    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="所属医院" width="220">
        <template #default="{ row }">{{ hMap[row.hospitalId]?.name }}</template>
      </el-table-column>
      <el-table-column prop="name" label="科室名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id ? '编辑科室' : '新增科室'" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="所属医院">
          <el-select v-model="form.hospitalId" style="width:100%">
            <el-option v-for="h in hospitals" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { adminApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const hospitals = ref([])
const list = ref([])
const filter = ref(null)
const visible = ref(false)
const form = reactive({ id: null, hospitalId: null, name: '', description: '' })
const hMap = computed(() => Object.fromEntries(hospitals.value.map(x => [x.id, x])))

async function load() {
  if (!hospitals.value.length) hospitals.value = (await adminApi.hospitals()).data
  list.value = (await adminApi.departments(filter.value)).data
}
function openEdit(row) {
  Object.assign(form, row ? { ...row } : { id: null, hospitalId: filter.value, name: '', description: '' })
  visible.value = true
}
async function submit() {
  if (!form.hospitalId || !form.name) return ElMessage.warning('请填写完整')
  if (form.id) await adminApi.updateDept(form.id, form)
  else await adminApi.createDept(form)
  visible.value = false; ElMessage.success('已保存'); load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确认删除「${row.name}」？`, '提示', { type: 'warning' })
  await adminApi.deleteDept(row.id); ElMessage.success('已删除'); load()
}
onMounted(load)
</script>

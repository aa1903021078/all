<template>
  <el-card class="card-shadow !border-0">
    <template #header>
      <div class="flex justify-between items-center">
        <span class="font-medium">医疗机构管理</span>
        <el-button type="primary" :icon="Plus" @click="openEdit(null)">新增机构</el-button>
      </div>
    </template>
    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="level" label="等级" width="100" />
      <el-table-column prop="parentId" label="上级机构" width="120">
        <template #default="{ row }">{{ map[row.parentId]?.name || '-' }}</template>
      </el-table-column>
      <el-table-column prop="address" label="地址" />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id ? '编辑机构' : '新增机构'" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="等级">
          <el-select v-model="form.level" style="width:100%">
            <el-option label="三级" value="三级" />
            <el-option label="二级" value="二级" />
            <el-option label="一级" value="一级" />
          </el-select>
        </el-form-item>
        <el-form-item label="上级机构">
          <el-select v-model="form.parentId" clearable style="width:100%">
            <el-option v-for="h in list.filter(x => x.id !== form.id)" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
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

const list = ref([])
const visible = ref(false)
const form = reactive({ id: null, name: '', level: '三级', parentId: null, address: '', phone: '' })
const map = computed(() => Object.fromEntries(list.value.map(x => [x.id, x])))

async function load() { list.value = (await adminApi.hospitals()).data }
function openEdit(row) {
  Object.assign(form, row ? { ...row } : { id: null, name: '', level: '三级', parentId: null, address: '', phone: '' })
  visible.value = true
}
async function submit() {
  if (!form.name) return ElMessage.warning('请填写名称')
  if (form.id) await adminApi.updateHospital(form.id, form)
  else await adminApi.createHospital(form)
  visible.value = false; ElMessage.success('已保存'); load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确认删除「${row.name}」？`, '提示', { type: 'warning' })
  await adminApi.deleteHospital(row.id); ElMessage.success('已删除'); load()
}
onMounted(load)
</script>
